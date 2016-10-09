package will.neat.encog.experiment;

import org.encog.ml.CalculateScore;
import org.encog.ml.ea.opp.selection.TruncationSelection;
import org.encog.ml.ea.train.basic.TrainEA;
import org.encog.neural.hyperneat.HyperNEATCODEC;
import org.encog.neural.hyperneat.substrate.Substrate;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.training.opp.*;
import org.encog.neural.neat.training.opp.links.SelectFixed;
import org.encog.neural.neat.training.opp.links.SelectProportion;
import org.encog.neural.neat.training.species.OriginalNEATSpeciation;
import will.neat.encog.EncogMarioFitnessFunction;
import will.neat.encog.MutatePerturbOrResetLinkWeight;
import will.neat.encog.substrate.MultiHiddenLayerSubstrate;
import will.neat.encog.substrate.SimpleMarioSubstrate;
import will.neat.params.HyperNEATParameters;
import will.neat.params.HyperNEATParametersPSO;

/**
 * Created by Will on 2/09/2016.
 */
public class HyperNEATEvolver {

    private TrainEA neat;
    private HyperNEATParameters params = new HyperNEATParametersPSO();

    public HyperNEATEvolver() {
        Substrate substrate = new MultiHiddenLayerSubstrate().makeSubstrate();

        NEATPopulation population = new NEATPopulation(substrate, params.POP_SIZE);
        population.setActivationCycles(params.ACTIVATION_CYCLES);
        population.setInitialConnectionDensity(params.INIT_CONNECTION_DENSITY);
        population.setWeightRange(params.CPPN_WEIGHT_RANGE);
        population.setCPPNMinWeight(params.CPPN_MIN_WEIGHT);
        population.setHyperNEATNNWeightRange(params.NN_WEIGHT_RANGE);
        population.setHyperNEATNNActivationFunction(params.NN_ACTIVATION_FUNCTION);

        population.reset();

        CalculateScore fitnessFunction = new EncogMarioFitnessFunction();

        OriginalNEATSpeciation speciation = new OriginalNEATSpeciation();
        speciation.setCompatibilityThreshold(params.INIT_COMPAT_THRESHOLD);
        speciation.setMaxNumberOfSpecies(params.MAX_SPECIES);
        speciation.setNumGensAllowedNoImprovement(params.MAX_GENS_SPECIES);

        neat = new TrainEA(population, fitnessFunction);
        neat.setSpeciation(speciation);
        neat.setSelection(new TruncationSelection(neat, params.SELECTION_PROP));
        neat.setEliteRate(params.ELITE_RATE);
        neat.setCODEC(new HyperNEATCODEC());

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
        neat.addOperation(params.ADD_NEURON_PROB, new NEATMutateAddNode());
        neat.addOperation(params.ADD_CONN_PROB, new NEATMutateAddLink());
        neat.addOperation(params.REMOVE_CONN_PROB, new NEATMutateRemoveLink());
        neat.addOperation(params.REMOVE_NEURON_PROB, new NEATMutateRemoveNeuron());
        neat.getOperators().finalizeStructure();
        neat.setThreadCount(1);
    }

    public TrainEA getNEAT() {
        return neat;
    }

    public HyperNEATParameters getParams() {
        return params;
    }
}
