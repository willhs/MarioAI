package will.neat.encog;

import com.anji.neat.RemoveConnectionMutationOperator;
import org.encog.ml.CalculateScore;
import org.encog.ml.ea.opp.CompoundOperator;
import org.encog.ml.ea.opp.selection.TruncationSelection;
import org.encog.ml.ea.train.basic.TrainEA;
import org.encog.ml.train.MLTrain;
import org.encog.ml.train.strategy.Strategy;
import org.encog.neural.hyperneat.HyperNEATGenome;
import org.encog.neural.hyperneat.substrate.Substrate;
import org.encog.neural.hyperneat.substrate.SubstrateFactory;
import org.encog.neural.hyperneat.substrate.SubstrateNode;
import org.encog.neural.neat.NEATNeuronType;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.training.NEATNeuronGene;
import org.encog.neural.neat.training.opp.*;
import org.encog.neural.neat.training.opp.links.MutatePerturbLinkWeight;
import org.encog.neural.neat.training.opp.links.MutateResetLinkWeight;
import org.encog.neural.neat.training.opp.links.SelectFixed;
import org.encog.neural.neat.training.opp.links.SelectProportion;
import org.encog.neural.neat.training.species.OriginalNEATSpeciation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Will on 17/07/2016.
 */
public class EncogHyperNEATEvolver {

    public static long START_VIEWING_TIME = Long.MIN_VALUE;

    private static Logger logger = Logger.getLogger(EncogHyperNEATEvolver.class
            .getSimpleName());
    // nn
    public static final int NUM_INPUT_NEURONS = 361; //1089; // 19*19 grid = 361
    public static final int NUM_OUTPUT_NEURONS = 4;
    private static final double INIT_CONNECTION_DENSITY = 0.1; // 1 for fully connected!
    private static final int ACTIVATION_CYCLES = 5;
    private static final int NN_WEIGHT_RANGE = 3; //
    private static final int CPPN_WEIGHT_RANGE = 3; // useful?

    // evolution
    public static final int POP_SIZE = 100;
    public static final double MAX_FITNESS = 15000;
    public static final long MAX_GENERATIONS = 200;

    public static final double SURVIVAL_RATIO = 0.1;
    public static final boolean KILL_UNPRODUCTIVE_SPECIES = true;
    private static final double COMPAT_THRESHOLD = 6;
    private static final double ELITE_RATE = 0.1;
    private static final double CROSSOVER_PROB = 0.5;

    // speciation
    public static final int MIN_PER_SPECIE = 10;
    public static final int MAX_SPECIES = POP_SIZE / MIN_PER_SPECIE;
    private static final int MAX_GENS_SPECIES = 50;

    // mutation probs
    private static final double ADD_CONN_PROB = 1;
    private static final double ADD_NEURON_PROB = 0.8;
    private static final double PERTURB_PROB = 1;
    private static final double REMOVE_CONN_PROB = 0.2;

    //  other config?
//    private static final double ADD_CONN_PROB = 0.8;
//    private static final double REMOVE_CONN_PROB = 0.2;
//    private static final double ADD_NEURON_PROB = 0.5;
//    private static final double PERTURB_PROB = 0.9;

    // start no connections (FS-NEAT)

    private static final double PERTURB_SD = 0.92; // perturb standard deviation

    public EncogHyperNEATEvolver() {
    }

/*    public void start() {
        start();
    }*/

    public void start() {

        Substrate substrate = new SandwichHiddenLayer().makeSubstrate();

        NEATPopulation population = new NEATPopulation(substrate, POP_SIZE);
        population.setActivationCycles(ACTIVATION_CYCLES);
        population.setSurvivalRate(SURVIVAL_RATIO);
        population.setInitialConnectionDensity(INIT_CONNECTION_DENSITY);
        population.setWeightRange(NN_WEIGHT_RANGE);

        // must reset before changing the codec or it won't be kept...
        population.reset();
        population.setCODEC(new HyperNEATCODECWill());

        CalculateScore fitnessFunction = new EncogMarioFitnessFunction();

        OriginalNEATSpeciation speciation = new OriginalNEATSpeciation();
        speciation.setCompatibilityThreshold(COMPAT_THRESHOLD);
        speciation.setMaxNumberOfSpecies(MAX_SPECIES);
        speciation.setNumGensAllowedNoImprovement(MAX_GENS_SPECIES);

        final TrainEA neat = new TrainEA(population, fitnessFunction);
        neat.setSpeciation(speciation);
        neat.setSelection(new TruncationSelection(neat, 0.2));
        neat.setEliteRate(ELITE_RATE);

        CompoundOperator compoundWeightMutation = composeWeightMutation();
        NEATMutateWeights weightMutation = new NEATMutateWeights(
                new SelectFixed(1),
                new MutatePerturbLinkWeight(PERTURB_SD)
        );

        CompoundOperator crossover = new CompoundOperator();
        crossover.getComponents().add(CROSSOVER_PROB, new NEATCrossover());

        neat.addOperation(0.5, new NEATCrossover());
        neat.addOperation(PERTURB_PROB, compoundWeightMutation);
        neat.addOperation(ADD_NEURON_PROB, new NEATMutateAddNode());
        neat.addOperation(ADD_CONN_PROB, new NEATMutateAddLink());
        neat.addOperation(REMOVE_CONN_PROB, new NEATMutateRemoveLink());
        neat.getOperators().finalizeStructure();
        neat.setThreadCount(1);
        // not sure if needed
        neat.setCODEC(new HyperNEATCODECWill());

        PrintStrategy printStrategy = new PrintStrategy();
        neat.addStrategy(printStrategy);

        // evolve til done
        int gen = 0;

        while (!neat.isTrainingDone() ||
                (population.getBestGenome() != null && population.getBestGenome().getScore() < MAX_FITNESS)) {

            neat.iteration();

            // report
            double bestFitness = population.getBestGenome().getScore();
            double bestFitnessGen = population.determineBestSpecies().getLeader().getScore();

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


            logger.info("Generation:\t" + gen);
            logger.info("Best fitness:\t" + bestFitnessGen);
            logger.info("Num species:\t" + numSpecies);
            logger.info("Ave CPPN conns:\t" + averageCPPNLinks);
            logger.info("Ave CPPN nodes:\t" + averageCPPNNodes);

            gen++;
        }

        logger.info("Evolving done");
        logger.info("Winning fitness: " + population.getBestGenome().getScore());
    }

    private static CompoundOperator composeWeightMutation() {
        final CompoundOperator weightMutation = new CompoundOperator();

        weightMutation.getComponents().add(
                0.1125,
                new NEATMutateWeights(new SelectFixed(1),
                        new MutatePerturbLinkWeight(0.02)));
        weightMutation.getComponents().add(
                0.1125,
                new NEATMutateWeights(new SelectFixed(2),
                        new MutatePerturbLinkWeight(0.02)));
        weightMutation.getComponents().add(
                0.1125,
                new NEATMutateWeights(new SelectFixed(3),
                        new MutatePerturbLinkWeight(0.02)));
        weightMutation.getComponents().add(
                0.1125,
                new NEATMutateWeights(new SelectProportion(0.02),
                        new MutatePerturbLinkWeight(0.02)));
        weightMutation.getComponents().add(
                0.1125,
                new NEATMutateWeights(new SelectFixed(1),
                        new MutatePerturbLinkWeight(1)));
        weightMutation.getComponents().add(
                0.1125,
                new NEATMutateWeights(new SelectFixed(2),
                        new MutatePerturbLinkWeight(1)));
        weightMutation.getComponents().add(
                0.1125,
                new NEATMutateWeights(new SelectFixed(3),
                        new MutatePerturbLinkWeight(1)));
        weightMutation.getComponents().add(
                0.1125,
                new NEATMutateWeights(new SelectProportion(0.02),
                        new MutatePerturbLinkWeight(1)));
        weightMutation.getComponents().add(
                0.03,
                new NEATMutateWeights(new SelectFixed(1),
                        new MutateResetLinkWeight()));
        weightMutation.getComponents().add(
                0.03,
                new NEATMutateWeights(new SelectFixed(2),
                        new MutateResetLinkWeight()));
        weightMutation.getComponents().add(
                0.03,
                new NEATMutateWeights(new SelectFixed(3),
                        new MutateResetLinkWeight()));
        weightMutation.getComponents().add(
                0.01,
                new NEATMutateWeights(new SelectProportion(0.02),
                        new MutateResetLinkWeight()));

        weightMutation.getComponents().finalizeStructure();
        return weightMutation;
    }

    public static void main(String[] args) {
        double hours = 9;
        START_VIEWING_TIME = System.currentTimeMillis() + (long)(hours * 60 * 60 * 1000);

        new EncogHyperNEATEvolver().start();
    }

    private class ChangeStrategy implements Strategy {
        TrainEA train;

        @Override
        public void init(MLTrain train) {
            this.train = (TrainEA) train;
        }

        @Override
        public void preIteration() {

        }

        @Override
        public void postIteration() {
//            train.getPopulation().getSpecies().stream().flatMap(s -> s.getMembers().stream()).();
            // TODO
        }

    }

    private class PrintStrategy implements Strategy {
        TrainEA train;

        @Override
        public void init(MLTrain train) {
            this.train = (TrainEA) train;
        }

        @Override
        public void preIteration() {

        }

        @Override
        public void postIteration() {
            HyperNEATGenome genome = (HyperNEATGenome) train.getBestGenome();
            List<NEATNeuronGene> inputs = genome.getNeuronsChromosome().stream()
                    .filter(c -> c.getNeuronType() == NEATNeuronType.Input)
                    .collect(Collectors.toList());

            inputs.forEach(i -> {
//                System.out.println(i.getId() + ": " + i.get);
            });
        }

    }
}

