package will.neat.encog;

import org.encog.ml.CalculateScore;
import org.encog.ml.ea.opp.CompoundOperator;
import org.encog.ml.ea.opp.selection.TruncationSelection;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;
import org.encog.ml.ea.train.basic.TrainEA;
import org.encog.neural.hyperneat.HyperNEATCODEC;
import org.encog.neural.hyperneat.substrate.Substrate;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.training.opp.*;
import org.encog.neural.neat.training.opp.links.MutatePerturbLinkWeight;
import org.encog.neural.neat.training.opp.links.MutateResetLinkWeight;
import org.encog.neural.neat.training.opp.links.SelectFixed;
import org.encog.neural.neat.training.opp.links.SelectProportion;
import org.encog.neural.neat.training.species.OriginalNEATSpeciation;

/**
 * Created by Will on 17/07/2016.
 */
public class EncogEvolver {

    // io
    public static final int NUM_INPUT_NEURONS = 728; //1089; // 19*19 grid = 361
    public static final int NUM_OUTPUT_NEURONS = 4;

    // evolution
    public static final int POP_SIZE = 300;
    public static final double MAX_FITNESS = 15000;
    public static final long MAX_GENERATIONS = 400;

    public static final int MIN_PER_SPECIE = 10;
    public static final int MAX_SPECIES = POP_SIZE / MIN_PER_SPECIE;
    private static final int MAX_GENS_SPECIES = 50;
    public static final double SURVIVAL_RATIO = 0.0;
    public static final boolean KILL_UNPRODUCTIVE_SPECIES = true;

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
    private static final double MAX_PERTURB = 0.92;

    public static void main(String[] args) {
        Substrate substrate = new Substrate(2);
        NEATPopulation population = new NEATPopulation(substrate, POP_SIZE);

        CalculateScore fitnessFunction = new EncogMarioFitnessFunction();

        final TrainEA result = new TrainEA(population, fitnessFunction);
        result.setSpeciation(new OriginalNEATSpeciation());

        result.setSelection(new TruncationSelection(result, 0.3));
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

        result.setChampMutation(weightMutation);
        result.addOperation(0.5, new NEATCrossover());
        result.addOperation(0.494, weightMutation);
        result.addOperation(0.0005, new NEATMutateAddNode());
        result.addOperation(0.005, new NEATMutateAddLink());
        result.addOperation(0.0005, new NEATMutateRemoveLink());
        result.getOperators().finalizeStructure();

        result.setCODEC(new HyperNEATCODEC());
    }
}
