package will.neat.encog;

import org.encog.ml.CalculateScore;
import org.encog.ml.ea.opp.selection.TruncationSelection;
import org.encog.ml.ea.train.basic.TrainEA;
import org.encog.ml.train.strategy.end.EndIterationsStrategy;
import org.encog.neural.hyperneat.substrate.Substrate;
import org.encog.neural.neat.NEATCODEC;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.training.NEATGenome;
import org.encog.neural.neat.training.opp.*;
import org.encog.neural.neat.training.opp.links.SelectFixed;
import org.encog.neural.neat.training.opp.links.SelectProportion;
import org.encog.neural.neat.training.species.OriginalNEATSpeciation;
import will.mario.agent.encog.AgentFactory;
import will.mario.agent.encog.EncogAgent;
import will.neat.encog.substrate.MultiHiddenLayerSubstrate;
import will.neat.params.HyperNEATParameters;
import will.neat.params.NEATParameters;
import will.rf.action.ActionStratFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by Will on 8/10/2016.
 */
public class NEATMarioEvolver {

    private Path path;
    private String simOptions;
    private NEATParameters params;
    private ActionStratFactory stratFactory;
    private String name;
    private boolean fileOutput = true;

    public NEATMarioEvolver(NEATParameters params, ActionStratFactory actionStratFactory,
                            Path path, String name) {
        this.params = params;
        this.stratFactory = actionStratFactory;
        this.name = name;
        this.path = path;
        this.fileOutput = true;
    }

    public NEATMarioEvolver(NEATParameters params, ActionStratFactory actionStratFactory) {
        this.params = params;
        this.stratFactory = actionStratFactory;
        this.fileOutput = false;
    }

    public TrainEA run() throws IOException {
        // initialise neat
        TrainEA neat = setupNEAT(params, simOptions,
                setupAgent(stratFactory));

        while (!neat.isTrainingDone()) {
            neat.iteration();
            if (fileOutput) {
                logIteration(neat, path);
            }
        }

        return neat;
    }

    protected AgentFactory setupAgent(ActionStratFactory stratFactory) {
        return (nn) -> new EncogAgent(nn, stratFactory);
    }

    protected TrainEA setupNEAT(NEATParameters params, String marioOptions, AgentFactory agentFactory) {
        NEATPopulation population = new NEATPopulation(params.NUM_INPUTS, params.NUM_OUTPUTS, params.POP_SIZE);
        population.setActivationCycles(params.ACTIVATION_CYCLES);
        population.setInitialConnectionDensity(params.INIT_CONNECTION_DENSITY);
        population.setWeightRange(params.NN_WEIGHT_RANGE);
        population.setNEATActivationFunction(params.NN_ACTIVATION_FUNCTION);
        population.reset();

        CalculateScore fitnessFunction = new EncogMarioFitnessFunction(marioOptions, agentFactory);

        OriginalNEATSpeciation speciation = new OriginalNEATSpeciation();
        speciation.setCompatibilityThreshold(params.INIT_COMPAT_THRESHOLD);
        speciation.setMaxNumberOfSpecies(params.MAX_SPECIES);
        speciation.setNumGensAllowedNoImprovement(params.MAX_GENS_SPECIES);

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
        neat.getOperators().finalizeStructure();

        // phased search (each phase has unique set of mutations)
        if (params.PHASED_SEARCH) {
            PhasedSearch phasedSearch = new PhasedSearch(10);
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

        neat.setThreadCount(1);

        neat.addStrategy(new EndIterationsStrategy(params.MAX_GENERATIONS));

        return neat;
    }


    protected Substrate setupSubstrate() {
        return new MultiHiddenLayerSubstrate().makeSubstrate();
    }

    private void logIteration(TrainEA neat, Path output) throws IOException {
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

        StringBuilder sb = new StringBuilder();
        sb.append(name + ",");
        sb.append(neat.getIteration() + ",");
        sb.append(bestFitness + ",");
        sb.append(averageLinks + ",");
        sb.append(averageNodes + ",");
        sb.append(numSpecies + ",");
        Files.write(output, sb.toString().getBytes());
    }

    public void setSimOptions(String simOptions) {
        this.simOptions = simOptions;
    }
}
