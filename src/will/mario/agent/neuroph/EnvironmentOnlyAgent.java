package will.mario.agent.neuroph;

import ch.idsia.benchmark.mario.engine.input.MarioInput;
import ch.idsia.benchmark.mario.engine.input.MarioKey;
import org.neuroph.core.NeuralNetwork;
import will.mario.environment.BinaryGridEnvironment;
import will.mario.environment.GameEnvironment;

import java.util.Arrays;
import java.util.Random;

/**
 * Created by Will on 29/06/2016.
 */
public class EnvironmentOnlyAgent extends NEATAgent {

    public EnvironmentOnlyAgent(NeuralNetwork nn) {
        super(nn);
    }

    @Override
    public MarioInput actionSelection() {

        GameEnvironment env = new BinaryGridEnvironment();
        return actionSelection(env);
    }
}
