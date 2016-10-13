package will.neat.encog.experiment;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.encog.ml.CalculateScore;
import org.encog.ml.ea.opp.selection.TruncationSelection;
import org.encog.ml.ea.train.basic.TrainEA;
import org.encog.ml.train.strategy.end.EndIterationsStrategy;
import org.encog.neural.neat.NEATCODEC;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.training.NEATGenome;
import org.encog.neural.neat.training.opp.*;
import org.encog.neural.neat.training.opp.links.SelectFixed;
import org.encog.neural.neat.training.opp.links.SelectProportion;
import org.encog.neural.neat.training.species.OriginalNEATSpeciation;
import will.neat.AbstractMarioFitnessFunction;
import will.neat.encog.EncogMarioFitnessFunction;
import will.neat.encog.MutatePerturbOrResetLinkWeight;
import will.neat.encog.PhasedSearch;
import will.neat.encog.gui.HyperNEATGUI;
import will.neat.params.HyperNEATParameters;
import will.neat.params.NEATParameters;
import will.neat.params.SpikeyNEATParameters;

import java.util.logging.Logger;

/**
 * Created by Will on 4/08/2016.
 */
public class NEATEvolverGUI extends Application {
    // io
    private static Logger logger = Logger.getLogger(HyperNEATGUI.class
            .getSimpleName());

    private static final int NUM_INPUTS = 169;
    private static final int NUM_OUTPUTS = 4;
    private static final int POP_SIZE = 200;

    private NEATParameters params = new SpikeyNEATParameters();

    private String level;

    public NEATEvolverGUI() {
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Mario AI NEAT experiment");

        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 400, 400);
        primaryStage.setScene(scene);

        // headless checkbox
        CheckBox checkbox = new CheckBox("Headless");
        checkbox.setSelected(true);
        checkbox.selectedProperty().addListener((obs, old, newVal) ->
                AbstractMarioFitnessFunction.headless = newVal
        );
        root.setLeft(checkbox);

        // define neat
        TrainEA neat = setupNEAT();

        Task<Void> evolve = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // evolve til done
                while (!neat.isTrainingDone()) {
                    neat.iteration();
                    logIteration(neat);
                }

                logger.info("Evolving done");
                logger.info("Winning fitness: " + neat.getPopulation().getBestGenome().getScore());

                return null;
            }
        };
        Thread thread = new Thread(evolve);
        thread.setDaemon(true);
        thread.start();

        primaryStage.show();
    }

    private void logIteration(TrainEA neat) {
        NEATPopulation population = (NEATPopulation) neat.getPopulation();
        double bestFitness = population.getBestGenome().getScore();
//            double bestFitnessGen = population.determineBestSpecies().getLeader().getScore();

        int numSpecies = population.getSpecies().size();

        double averageLinks = population.getSpecies().stream()
                .map(s -> s.getMembers())
                .flatMap(genomes -> genomes.stream())
                .mapToInt(genome -> ((NEATGenome)genome).getLinksChromosome().size())
                .average()
                .getAsDouble();

        double averageNodes = population.getSpecies().stream()
                .map(s -> s.getMembers())
                .flatMap(genomes -> genomes.stream())
                .mapToInt(genome -> ((NEATGenome)genome).getNeuronsChromosome().size())
                .average()
                .getAsDouble();


        logger.info("Generation:\t" + neat.getIteration());
        logger.info("Best fitness:\t" + bestFitness);
        logger.info("Num species:\t" + numSpecies);
        logger.info("Ave conns:\t" + averageLinks);
        logger.info("Ave nodes:\t" + averageNodes);
    }

    protected TrainEA setupNEAT() {
        NEATPopulation population = new NEATPopulation(NUM_INPUTS, NUM_OUTPUTS, POP_SIZE);
        population.setActivationCycles(params.ACTIVATION_CYCLES);
        population.setInitialConnectionDensity(params.INIT_CONNECTION_DENSITY);
        population.setWeightRange(params.NN_WEIGHT_RANGE);
        population.setNEATActivationFunction(params.NN_ACTIVATION_FUNCTION);
        population.reset();

        CalculateScore fitnessFunction = new EncogMarioFitnessFunction();

        OriginalNEATSpeciation speciation = new OriginalNEATSpeciation();
        speciation.setCompatibilityThreshold(params.INIT_COMPAT_THRESHOLD);
        speciation.setMaxNumberOfSpecies(params.MAX_SPECIES);
        speciation.setNumGensAllowedNoImprovement(params.SPECIES_DROPOFF);

        final TrainEA neat = new TrainEA(population, fitnessFunction);
        neat.setSpeciation(speciation);
        neat.setSelection(new TruncationSelection(neat, params.SELECTION_PROP));
        neat.setEliteRate(params.ELITE_RATE);
        neat.setCODEC(new NEATCODEC());

        double perturbProp = params.WEIGHT_PERTURB_PROP;
        double perturbSD = params.PERTURB_SD;
        double resetWeightProb = params.RESET_WEIGHT_PROB;
        // either perturb a proportion of all weights or just one weight
        NEATMutateWeights weightMutation = new NEATMutateWeights(
                params.WEIGHT_MUT_TYPE == HyperNEATParameters.WeightMutType.PROPORTIONAL
                        ? new SelectProportion(perturbProp)
                        : new SelectFixed(1),
                new MutatePerturbOrResetLinkWeight(resetWeightProb, perturbSD)
        );

        neat.addOperation(params.CROSSOVER_PROB, new NEATCrossover());
        neat.addOperation(params.PERTURB_PROB, weightMutation);

        // phased search (each phase has unique set of mutations)
        if (params.PHASED_SEARCH) {
            PhasedSearch phasedSearch = new PhasedSearch(
                    params.PHASE_A_LENGTH, params.PHASE_B_LENGTH);
            neat.addStrategy(phasedSearch);

            // additive mutations
            phasedSearch.addPhaseOp(0, params.ADD_CONN_PROB, new NEATMutateAddLink());
            phasedSearch.addPhaseOp(0, params.ADD_NEURON_PROB, new NEATMutateAddNode());

            // subtractive mutations
            phasedSearch.addPhaseOp(1, params.REMOVE_CONN_PROB, new NEATMutateRemoveLink());
            phasedSearch.addPhaseOp(1, params.REMOVE_NEURON_PROB, new NEATMutateRemoveNeuron());
        } else { // blended search
            neat.addOperation(params.ADD_CONN_PROB, new NEATMutateAddLink());
            neat.addOperation(params.ADD_NEURON_PROB, new NEATMutateAddNode());
            neat.addOperation(params.REMOVE_CONN_PROB, new NEATMutateRemoveLink());
            neat.addOperation(params.REMOVE_NEURON_PROB, new NEATMutateRemoveNeuron());
        }
        neat.getOperators().finalizeStructure();

        neat.setThreadCount(1);

        // ?
        neat.addStrategy(new EndIterationsStrategy(params.MAX_GENERATIONS));

        return neat;
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
