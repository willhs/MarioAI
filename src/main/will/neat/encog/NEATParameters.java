package will.neat.encog;

import org.encog.engine.network.activation.ActivationBipolarSteepenedSigmoid;
import org.encog.engine.network.activation.ActivationClippedLinear;
import org.encog.engine.network.activation.ActivationFunction;

/**
 * Created by Will on 26/08/2016.
 * Members are public and not final only for convenience. They shouldn't be changed though..
 */
public class NEATParameters {

    // defaults

    // nn
    public int ACTIVATION_CYCLES = 3;
    public double NN_WEIGHT_RANGE = 1.0; //
    public double CPPN_MIN_WEIGHT = 0.5;
    public double INIT_CONNECTION_DENSITY = 1; // 1 for fully connected!
    public ActivationFunction ACTIVATION_FUNCTION = new ActivationClippedLinear();

    // evolution
    public int POP_SIZE = 200;

    public double SELECTION_PROP = 0.4;
    public double ELITE_RATE = 0.1;
    public double CROSSOVER_PROB = 1;

    // speciation
    public int MIN_PER_SPECIE = 10;
    public int MAX_SPECIES = 15;//POP_SIZE / MIN_PER_SPECIE;
    public int MAX_GENS_SPECIES = 20;
    public double COMPAT_THRESHOLD = 6; // 6

    // mutation probs
    public double ADD_CONN_PROB = 0.5;
    public double ADD_NEURON_PROB = 0.9;
    public double PERTURB_PROB = 0.5;
    public double REMOVE_CONN_PROB = 0.1;

    public enum WeightMutType { PROPORTIONAL, ONCE }

    public WeightMutType WEIGHT_MUT_TYPE = WeightMutType.ONCE;
    public double WEIGHT_PERTURB_PROP = 0.1;
    public double PERTURB_SD = 0.92; // perturb standard deviation
    public double RESET_WEIGHT_PROB = 0.2;
}
