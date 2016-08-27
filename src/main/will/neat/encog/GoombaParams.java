package will.neat.encog;

import org.encog.engine.network.activation.ActivationBipolarSteepenedSigmoid;

/**
 * Created by Will on 26/08/2016.
 */
public class GoombaParams extends NEATParameters {

    public GoombaParams() {
        // nn
        ACTIVATION_CYCLES = 4;
        CPPN_WEIGHT_RANGE = 4.99; //
        CPPN_MIN_WEIGHT = 0.5;
        INIT_CONNECTION_DENSITY = 0.0; // 1 for fully connected!
        ACTIVATION_FUNCTION = new ActivationBipolarSteepenedSigmoid();

        // evolution
        POP_SIZE = 200;

        SELECTION_PROP = 0.87;
        ELITE_RATE = 0.01;
        CROSSOVER_PROB = 1;

        // speciation
        MIN_PER_SPECIE = 10;
        MAX_SPECIES = (int)(POP_SIZE * 0.08);
        MAX_GENS_SPECIES = 12;
        COMPAT_THRESHOLD = 10; // 6

        // mutation probs
        ADD_CONN_PROB = 0.36;
        ADD_NEURON_PROB = 0.86;
        PERTURB_PROB = 1.0;
        REMOVE_CONN_PROB = 0.11;

        WEIGHT_PERTURB_PROP = 0.0;
        PERTURB_SD = 0.92; // perturb standard deviation
        RESET_WEIGHT_PROB = 0.13;
    }
}
