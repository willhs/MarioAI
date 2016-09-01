package will.neat.params;

import org.encog.engine.network.activation.ActivationBipolarSteepenedSigmoid;
import org.encog.engine.network.activation.ActivationClippedLinear;
import org.encog.engine.network.activation.ActivationFunction;
import org.encog.engine.network.activation.ActivationSteepenedSigmoid;

/**
 * Created by Will on 30/08/2016.
 */
public class NEATParameters {

    public int POP_SIZE = 200;

    // nn
    public int ACTIVATION_CYCLES = 5;
    public double NN_WEIGHT_RANGE = 1.0;
    public double INIT_CONNECTION_DENSITY = 0.0; // 1 for fully connected!
    public ActivationFunction ACTIVATION_FUNCTION = new ActivationClippedLinear();

    public double SELECTION_PROP = 0.4;
    public double ELITE_RATE = 0.1;
    public double CROSSOVER_PROB = 0;

    // speciation
    public int MIN_PER_SPECIE = 10;
    public int MAX_SPECIES = 15;//POP_SIZE / MIN_PER_SPECIE;
    public int MAX_GENS_SPECIES = 50;
    public double COMPAT_THRESHOLD = 8;

    // mutation probs
    public enum WeightMutType { PROPORTIONAL, ONCE }

    public double ADD_CONN_PROB = 0.5;
    public double ADD_NEURON_PROB = 0.9;
    public double PERTURB_PROB = 0.5;
    public double REMOVE_CONN_PROB = 0.1;

    public WeightMutType WEIGHT_MUT_TYPE = WeightMutType.PROPORTIONAL;
    public double WEIGHT_PERTURB_PROP = 0.1;
    public double PERTURB_SD = 0.92; // perturb standard deviation
    public double RESET_WEIGHT_PROB = 0.2;
}
