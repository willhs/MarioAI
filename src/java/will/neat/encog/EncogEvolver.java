package will.neat.encog;

import org.encog.ml.CalculateScore;
import org.encog.ml.ea.opp.CompoundOperator;
import org.encog.ml.ea.opp.selection.TruncationSelection;
import org.encog.ml.ea.train.basic.TrainEA;
import org.encog.neural.hyperneat.HyperNEATCODEC;
import org.encog.neural.hyperneat.HyperNEATGenome;
import org.encog.neural.hyperneat.substrate.Substrate;
import org.encog.neural.hyperneat.substrate.SubstrateNode;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.training.opp.*;
import org.encog.neural.neat.training.opp.links.MutatePerturbLinkWeight;
import org.encog.neural.neat.training.opp.links.MutateResetLinkWeight;
import org.encog.neural.neat.training.opp.links.SelectFixed;
import org.encog.neural.neat.training.opp.links.SelectProportion;
import org.encog.neural.neat.training.species.OriginalNEATSpeciation;

import java.util.logging.Logger;

/**
 * Created by Will on 17/07/2016.
 */
public class EncogEvolver {

    // io
    public static final int NUM_INPUT_NEURONS = 728; //1089; // 19*19 grid = 361
    public static final int NUM_OUTPUT_NEURONS = 4;

    // evolution
    public static final int POP_SIZE = 200;
    public static final double MAX_FITNESS = 15000;
    public static final long MAX_GENERATIONS = 200;

    public static final int MIN_PER_SPECIE = 10;
    public static final int MAX_SPECIES = POP_SIZE / MIN_PER_SPECIE;
    private static final int MAX_GENS_SPECIES = 50;
    public static final double SURVIVAL_RATIO = 0.1;
    public static final boolean KILL_UNPRODUCTIVE_SPECIES = true;

    private static Logger logger = Logger.getLogger(EncogEvolver.class
            .getSimpleName());

    // mutations

    // fully connected
//    private static final double ADD_CONN_PROB = 0.2;
//    private static final double REMOVE_CONN_PROB = 0.8;
//    private static final double ADD_NEURON_PROB = 0.5;
//    private static final double PERTURB_PROB = 0.9;

    // start no connections (FS-NEAT)
    private static final double ADD_CONN_PROB = 1;
    private static final double ADD_NEURON_PROB = 1;
    private static final double PERTURB_PROB = 1;
    private static final double REMOVE_CONN_PROB = 0.2;
    private static final double PERTURB_SD = 0.92; // perturb standard deviation

    public static void main(String[] args) {

        Substrate substrate = makeSubstrate();

        NEATPopulation population = new NEATPopulation(substrate, POP_SIZE);
        population.setActivationCycles(2);
        population.setSurvivalRate(SURVIVAL_RATIO);
        population.reset();

        CalculateScore fitnessFunction = new EncogMarioFitnessFunction();

        final TrainEA neat = new TrainEA(population, fitnessFunction);
        neat.setSpeciation(new OriginalNEATSpeciation());

        neat.setSelection(new TruncationSelection(neat, 0.2));

//        CompoundOperator weightMutation = composeWeightMutation();
        NEATMutateWeights weightMutation = new NEATMutateWeights(
                new SelectFixed(1),
                new MutatePerturbLinkWeight(PERTURB_SD)
        );

        neat.setChampMutation(weightMutation);
        neat.addOperation(0.5, new NEATCrossover());
        neat.addOperation(PERTURB_PROB, weightMutation);
        neat.addOperation(ADD_NEURON_PROB, new NEATMutateAddNode());
        neat.addOperation(ADD_CONN_PROB, new NEATMutateAddLink());
        neat.addOperation(REMOVE_CONN_PROB, new NEATMutateRemoveLink());
        neat.getOperators().finalizeStructure();

        neat.setCODEC(new HyperNEATCODEC());

        // evolve til done
        while (neat.isTrainingDone() || population.getBestGenome() != null ?
                population.getBestGenome().getScore() < MAX_FITNESS
                : true) {

            neat.iteration();

            // report
            double bestFitness = population.getBestGenome().getScore();
            int numSpecies = population.getSpecies().size();

            double averageCPPNLinks = population.getSpecies().stream()
                    .map(s -> s.getMembers())
                    .mapToInt(genome -> ((HyperNEATGenome)genome).getLinksChromosome().size())
                    .average()
                    .getAsDouble();

            double averageCPPNNodes = population.getSpecies().stream()
                    .map(s -> s.getMembers())
                    .mapToInt(genome -> ((HyperNEATGenome)genome).getNeuronsChromosome().size())
                    .average()
                    .getAsDouble();


            logger.info("Best fitness:\t" + bestFitness);
            logger.info("Num species:\t" + numSpecies);
            logger.info("Ave CPPN conns:\t" + averageCPPNLinks);
            logger.info("Ave CPPN nodes:\t" + averageCPPNNodes);
        }

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

    private static Substrate makeSubstrate() {
//        Substrate substrate = SubstrateFactory.factorSandwichSubstrate();
        Substrate substrate = new Substrate(2);

        int gridWidth = 19;
        int gridHeight = 19;

        // make inputs
        for (int r = 0; r < gridWidth; r++ ) {
            for (int c = 0; c < gridHeight; c++) {
                SubstrateNode input = substrate.createInputNode();
                input.getLocation()[0] = r;
                input.getLocation()[1] = c;
            }
        }

        // make hidden nodes (same as input positions)
        for (int r = 0; r < gridWidth; r++ ) {
            for (int c = 0; c < gridHeight; c++) {
                SubstrateNode input = substrate.createHiddenNode();
                input.getLocation()[0] = r;
                input.getLocation()[1] = c;
            }
        }

        // make outputs

        // left
        SubstrateNode left = substrate.createOutputNode();
        left.getLocation()[0] = -1;
        left.getLocation()[1] = 0;

        // right
        SubstrateNode right = substrate.createOutputNode();
        right.getLocation()[0] = 1;
        right.getLocation()[1] = 0;

        // speed
        SubstrateNode speed = substrate.createOutputNode();
        speed.getLocation()[0] = 2;
        speed.getLocation()[1] = 0;

        // up
        SubstrateNode up = substrate.createOutputNode();
        up.getLocation()[0] = 0;
        up.getLocation()[1] = 1;

        return substrate;
    }
}
