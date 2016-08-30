package will.neat;

import org.encog.engine.network.activation.ActivationClippedLinear;
import org.encog.engine.network.activation.ActivationFunction;

/**
 * Created by Will on 26/08/2016.
 * Members are public and not final only for convenience. They shouldn't be changed though..
 */
public class HyperNEATParameters extends NEATParameters {

    // defaults
    public double CPPN_WEIGHT_RANGE = 3.0; //
    public double CPPN_MIN_WEIGHT = 0.2;

    public HyperNEATParameters() {

        // nn
        ACTIVATION_CYCLES = 3;
        NN_WEIGHT_RANGE = 3.0;
        INIT_CONNECTION_DENSITY = 0.3; // 1 for fully connected!
        ACTIVATION_FUNCTION = new ActivationClippedLinear();

        // evolution
        POP_SIZE = 200;

        SELECTION_PROP = 0.4;
        ELITE_RATE = 0.1;
        CROSSOVER_PROB = 0;

        // speciation
        MIN_PER_SPECIE = 10;
        MAX_SPECIES = 15;//POP_SIZE / MIN_PER_SPECIE;
        MAX_GENS_SPECIES = 50;
        COMPAT_THRESHOLD = 8;

        // mutation probs
        ADD_CONN_PROB = 0.5;
        ADD_NEURON_PROB = 0.9;
        PERTURB_PROB = 0.5;
        REMOVE_CONN_PROB = 0.1;

        WEIGHT_MUT_TYPE = WeightMutType.PROPORTIONAL;
        WEIGHT_PERTURB_PROP = 0.1;
        PERTURB_SD = 0.92; // perturb standard deviation
        RESET_WEIGHT_PROB = 0.2;
    }
}
