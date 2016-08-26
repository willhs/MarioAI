package will.neat.encog;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import org.encog.engine.network.activation.ActivationBipolarSteepenedSigmoid;
import org.encog.engine.network.activation.ActivationFunction;
import org.encog.ml.CalculateScore;
import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.opp.CompoundOperator;
import org.encog.ml.ea.opp.selection.TruncationSelection;
import org.encog.ml.ea.train.basic.TrainEA;
import org.encog.ml.train.MLTrain;
import org.encog.ml.train.strategy.Strategy;
import org.encog.neural.hyperneat.HyperNEATCODEC;
import org.encog.neural.hyperneat.HyperNEATGenome;
import org.encog.neural.hyperneat.substrate.Substrate;
import org.encog.neural.neat.NEATNeuronType;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.training.NEATNeuronGene;
import org.encog.neural.neat.training.opp.*;
import org.encog.neural.neat.training.opp.links.MutatePerturbLinkWeight;
import org.encog.neural.neat.training.opp.links.MutateResetLinkWeight;
import org.encog.neural.neat.training.opp.links.SelectFixed;
import org.encog.neural.neat.training.opp.links.SelectProportion;
import org.encog.neural.neat.training.species.OriginalNEATSpeciation;
import org.neuroph.contrib.neat.gen.operations.fitness.AbstractFitnessFunction;
import will.neat.AbstractMarioFitnessFunction;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Will on 17/07/2016.
 */
public class EncogHyperNEATEvolver extends Application {

    private static Logger logger = Logger.getLogger(EncogHyperNEATEvolver.class
            .getSimpleName());

    private NEATParameters params = new NEATParameters();

    public EncogHyperNEATEvolver() {
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Mario AI NEAT experiment");

        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 900, 900);
        primaryStage.setScene(scene);

        // headless checkbox
        CheckBox checkbox = new CheckBox("Headless");
        checkbox.setSelected(true);
        checkbox.selectedProperty().addListener((obs, old, nw) ->
            AbstractMarioFitnessFunction.headless = nw
        );
        root.setLeft(checkbox);

        final double CANVAS_HEIGHT = 800;
        final double CANVAS_WIDTH = 800;

        Canvas canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        root.setCenter(canvas);

        // define neat
        TrainEA neat = setupNEAT();
        DrawNNStrategy draw = new DrawNNStrategy(canvas);
        neat.addStrategy(draw);

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

        double averageCPPNLinks = population.getSpecies().stream()
                .map(s -> s.getMembers())
                .flatMap(genomes -> genomes.stream())
                .mapToInt(genome -> ((HyperNEATGenome)genome).getLinksChromosome().size())
                .average()
                .getAsDouble();

        double averageCPPNNodes = population.getSpecies().stream()
                .map(s -> s.getMembers())
                .flatMap(genomes -> genomes.stream())
                .mapToInt(genome -> ((HyperNEATGenome)genome).getNeuronsChromosome().size())
                .average()
                .getAsDouble();


        logger.info("Generation:\t" + neat.getIteration());
        logger.info("Best fitness:\t" + bestFitness);
        logger.info("Num species:\t" + numSpecies);
        logger.info("Ave CPPN conns:\t" + averageCPPNLinks);
        logger.info("Ave CPPN nodes:\t" + averageCPPNNodes);
    }

    private TrainEA setupNEAT() {
        Substrate substrate = new SandwichHiddenLayer().makeSubstrate();

        NEATPopulation population = new NEATPopulation(substrate, params.POP_SIZE);
        population.setActivationCycles(params.ACTIVATION_CYCLES);
        population.setInitialConnectionDensity(params.INIT_CONNECTION_DENSITY);
        population.setWeightRange(params.NN_WEIGHT_RANGE);
        population.setCPPNMinWeight(params.CPPN_MIN_WEIGHT);
        population.setActivationFunction(params.ACTIVATION_FUNCTION);

        population.reset();
        // must reset before changing the codec or it won't be kept...
//        population.setCODEC(new HyperNEATCODEC());

        CalculateScore fitnessFunction = new EncogMarioFitnessFunction();

        OriginalNEATSpeciation speciation = new OriginalNEATSpeciation();
        speciation.setCompatibilityThreshold(params.COMPAT_THRESHOLD);
        speciation.setMaxNumberOfSpecies(params.MAX_SPECIES);
        speciation.setNumGensAllowedNoImprovement(params.MAX_GENS_SPECIES);

        final TrainEA neat = new TrainEA(population, fitnessFunction);
        neat.setSpeciation(speciation);
        neat.setSelection(new TruncationSelection(neat, params.SELECTION_PROP));
        neat.setEliteRate(params.ELITE_RATE);
        neat.setCODEC(new HyperNEATCODEC());

        double perturbProp = params.WEIGHT_PERTURB_PROP;
        double weightRange = params.NN_WEIGHT_RANGE;
        double perturbSD = params.PERTURB_SD * weightRange;
        double resetWeightProb = params.RESET_WEIGHT_PROB;
        // either perturb a proportion of all weights or just one weight
        NEATMutateWeights weightMutation = new NEATMutateWeights(
                params.WEIGHT_MUT_TYPE == NEATParameters.WeightMutType.PROPORTIONAL
                        ? new SelectProportion(perturbProp)
                        : new SelectFixed(1),
                new MutatePerturbOrResetLinkWeight(resetWeightProb, perturbSD)
        );

        neat.addOperation(params.CROSSOVER_PROB, new NEATCrossover());
        neat.addOperation(params.PERTURB_PROB, weightMutation);
        neat.addOperation(params.ADD_NEURON_PROB, new NEATMutateAddNode());
        neat.addOperation(params.ADD_CONN_PROB, new NEATMutateAddLink());
        neat.addOperation(params.REMOVE_CONN_PROB, new NEATMutateRemoveLink());
        neat.getOperators().finalizeStructure();
        neat.setThreadCount(1);

        return neat;
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

}

