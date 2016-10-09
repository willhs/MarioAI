package will.neat.encog;

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
import will.mario.agent.encog.AgentFactory;
import will.neat.params.HyperNEATParameters;
import will.neat.params.HyperNEATParametersPSO;
import will.neat.params.NEATParameters;
import will.rf.action.ActionStratFactory;
import will.rf.action.StandardHoldStrat;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by Will on 9/10/2016.
 */
public class HyperNEATMarioEvolver extends NEATMarioEvolver {

    public HyperNEATMarioEvolver(HyperNEATParameters params, ActionStratFactory actionStratFactory,
                                 Path path, String name) {
        super(params, actionStratFactory, path, name);
    }

    public HyperNEATMarioEvolver(HyperNEATParameters params, ActionStratFactory actionStratFactory) {
        super(params, actionStratFactory);
    }

    @Override
    protected TrainEA setupNEAT(NEATParameters params, String marioOptions, AgentFactory factory) {
        HyperNEATParameters hyperParams = (HyperNEATParameters) params;
        Substrate substrate = setupSubstrate();

        NEATPopulation population = new NEATPopulation(substrate, hyperParams.POP_SIZE);
        population.setActivationCycles(hyperParams.ACTIVATION_CYCLES);
        population.setInitialConnectionDensity(hyperParams.INIT_CONNECTION_DENSITY);
        population.setWeightRange(hyperParams.CPPN_WEIGHT_RANGE);
        population.setCPPNMinWeight(hyperParams.CPPN_MIN_WEIGHT);
        population.setHyperNEATNNWeightRange(hyperParams.NN_WEIGHT_RANGE);
        population.setHyperNEATNNActivationFunction(hyperParams.NN_ACTIVATION_FUNCTION);

        population.reset();

        CalculateScore fitnessFunction = new EncogMarioFitnessFunction(marioOptions, factory);

        OriginalNEATSpeciation speciation = new OriginalNEATSpeciation();
        speciation.setCompatibilityThreshold(hyperParams.INIT_COMPAT_THRESHOLD);
        speciation.setMaxNumberOfSpecies(hyperParams.MAX_SPECIES);
        speciation.setNumGensAllowedNoImprovement(hyperParams.MAX_GENS_SPECIES);

        TrainEA neat = new TrainEA(population, fitnessFunction);
        neat.setSpeciation(speciation);
        neat.setSelection(new TruncationSelection(neat, hyperParams.SELECTION_PROP));
        neat.setEliteRate(hyperParams.ELITE_RATE);
        neat.setCODEC(new HyperNEATCODEC());

        double perturbProp = hyperParams.WEIGHT_PERTURB_PROP;
        double perturbSD = hyperParams.PERTURB_SD;
        double resetWeightProb = hyperParams.RESET_WEIGHT_PROB;
        // either perturb a proportion of all weights or just one weight
        NEATMutateWeights weightMutation = new NEATMutateWeights(
                hyperParams.WEIGHT_MUT_TYPE == HyperNEATParameters.WeightMutType.PROPORTIONAL
                        ? new SelectProportion(perturbProp)
                        : new SelectFixed(1),
                new MutatePerturbOrResetLinkWeight(resetWeightProb, perturbSD)
        );

        neat.addOperation(hyperParams.CROSSOVER_PROB, new NEATCrossover());
        neat.addOperation(hyperParams.PERTURB_PROB, weightMutation);
        neat.addOperation(hyperParams.ADD_NEURON_PROB, new NEATMutateAddNode());
        neat.addOperation(hyperParams.ADD_CONN_PROB, new NEATMutateAddLink());
        neat.addOperation(hyperParams.REMOVE_CONN_PROB, new NEATMutateRemoveLink());
        neat.addOperation(hyperParams.REMOVE_NEURON_PROB, new NEATMutateRemoveNeuron());
        neat.getOperators().finalizeStructure();
        neat.setThreadCount(1);

        return neat;
    }
}
