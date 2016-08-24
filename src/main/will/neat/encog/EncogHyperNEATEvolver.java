package will.neat.encog;

import org.encog.engine.network.activation.ActivationBipolarSteepenedSigmoid;
import org.encog.engine.network.activation.ActivationFunction;
import org.encog.ml.CalculateScore;
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

import java.util.List;
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
    private static final int ACTIVATION_CYCLES = 4;
    private static final double NN_WEIGHT_RANGE = 1.0; //
    private static final double CPPN_MIN_WEIGHT = 0.4;
    private static final double INIT_CONNECTION_DENSITY = 0.4; // 1 for fully connected!
    private static final ActivationFunction ACTIVATION_FUNCTION = new ActivationBipolarSteepenedSigmoid();

    // evolution
    public static final int POP_SIZE = 200;
    public static final double MAX_FITNESS = 15000;

    private static final double SELECTION_PROP = 0.4;
    private static final double ELITE_RATE = 0.1;
    private static final double CROSSOVER_PROB = 1;

    // speciation
    public static final int MIN_PER_SPECIE = 10;
    public static final int MAX_SPECIES = 10;//POP_SIZE / MIN_PER_SPECIE;
    private static final int MAX_GENS_SPECIES = 20;
    private static final double COMPAT_THRESHOLD = 10; // 6

    // mutation probs
    private static final double ADD_CONN_PROB = 0.5;
    private static final double ADD_NEURON_PROB = 0.9;
    private static final double PERTURB_PROB = 0.5;
    private static final double REMOVE_CONN_PROB = 0.1;

    public enum WeightMutType { PROPORTIONAL, ONCE }

    private static WeightMutType WEIGHT_MUT_TYPE = WeightMutType.ONCE;
    private static final double WEIGHT_PERTURB_PROP = 0.1;
    private static final double PERTURB_SD = 0.92; // perturb standard deviation
    private static final double RESET_WEIGHT_PROB = 0.2;

    public EncogHyperNEATEvolver() {
    }

/*    public void start() {
        start();
    }*/

    public void start() {

        Substrate substrate = new SandwichHiddenLayer().makeSubstrate();

        NEATPopulation population = new NEATPopulation(substrate, POP_SIZE);
        population.setActivationCycles(ACTIVATION_CYCLES);
        population.setInitialConnectionDensity(INIT_CONNECTION_DENSITY);
        population.setWeightRange(NN_WEIGHT_RANGE);
        population.setCPPNMinWeight(CPPN_MIN_WEIGHT);
        population.setActivationFunction(ACTIVATION_FUNCTION);

        population.reset();
        // must reset before changing the codec or it won't be kept...
//        population.setCODEC(new HyperNEATCODEC());

        CalculateScore fitnessFunction = new EncogMarioFitnessFunction();

        OriginalNEATSpeciation speciation = new OriginalNEATSpeciation();
        speciation.setCompatibilityThreshold(COMPAT_THRESHOLD);
        speciation.setMaxNumberOfSpecies(MAX_SPECIES);
        speciation.setNumGensAllowedNoImprovement(MAX_GENS_SPECIES);

        final TrainEA neat = new TrainEA(population, fitnessFunction);
        neat.setSpeciation(speciation);
        neat.setSelection(new TruncationSelection(neat, SELECTION_PROP));
        neat.setEliteRate(ELITE_RATE);
        neat.setCODEC(new HyperNEATCODEC());

        double perturbProp = WEIGHT_PERTURB_PROP;
        double weightRange = NN_WEIGHT_RANGE;
        double perturbSD = PERTURB_SD * weightRange;
        double resetWeightProb = RESET_WEIGHT_PROB;
        // either perturb a proportion of all weights or just one weight
        NEATMutateWeights weightMutation = new NEATMutateWeights(
                WEIGHT_MUT_TYPE == WeightMutType.PROPORTIONAL
                        ? new SelectProportion(perturbProp)
                        : new SelectFixed(1),
                new MutatePerturbOrResetLinkWeight(resetWeightProb, perturbSD)
        );

        neat.addOperation(CROSSOVER_PROB, new NEATCrossover());
        neat.addOperation(PERTURB_PROB, weightMutation);
        neat.addOperation(ADD_NEURON_PROB, new NEATMutateAddNode());
        neat.addOperation(ADD_CONN_PROB, new NEATMutateAddLink());
        neat.addOperation(REMOVE_CONN_PROB, new NEATMutateRemoveLink());
        neat.getOperators().finalizeStructure();
        neat.setThreadCount(1);

        PrintStrategy printStrategy = new PrintStrategy();
        neat.addStrategy(printStrategy);

        // evolve til done
        int gen = 0;

        while (!neat.isTrainingDone() ||
                (population.getBestGenome() != null && population.getBestGenome().getScore() < MAX_FITNESS)) {

            neat.iteration();

            // report
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


            logger.info("Generation:\t" + gen);
            logger.info("Best fitness:\t" + bestFitness);
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

