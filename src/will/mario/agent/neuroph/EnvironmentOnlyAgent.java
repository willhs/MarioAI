package will.mario.agent.neuroph;

import ch.idsia.benchmark.mario.engine.input.MarioInput;
import org.neuroph.core.NeuralNetwork;
import will.rf.environment.BinaryEnvGridEnvironment;
import will.rf.environment.GameEnvironment;

/**
 * Created by Will on 29/06/2016.
 */
public class EnvironmentOnlyAgent extends NEATAgent {

    public EnvironmentOnlyAgent(NeuralNetwork nn) {
        super(nn);
    }

    @Override
    public MarioInput actionSelection() {
        GameEnvironment env = new BinaryEnvGridEnvironment();
        return actionSelection(env);
    }
}
