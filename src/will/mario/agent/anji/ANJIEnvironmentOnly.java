package will.mario.agent.anji;

import ch.idsia.benchmark.mario.engine.input.MarioInput;
import ch.idsia.benchmark.mario.engine.input.MarioKey;
import will.mario.environment.BinaryGridEnvironment;
import will.mario.environment.GameEnvironment;
import com.anji.integration.Activator;

/**
 * Created by Will Hardwick-Smith on 17/04/2016.
 */
public class ANJIEnvironmentOnly extends ANJINEATAgent {

    public ANJIEnvironmentOnly(Activator activator) {
        super(activator);
    }

    @Override
    public MarioInput actionSelection() {

        // put relevant environment into form for neural net
        GameEnvironment env = new BinaryGridEnvironment();
//        GameEnvironment env = new ValueGridEnvironment();
//        GameEnvironment env = new MultiGridEnvironment();
        double[] inputs = env.getInputNeurons(environment, lastInput);

        // put tiles through the neural network to receive game inputs
        // 1 or 0 for each of the game inputs: [left,right,down,jump,speed/attack,up(useless)]
        double[] networkOutput = activator.next(inputs);

        MarioInput action = new MarioInput();

        for (int i = 0; i < networkOutput.length; i++) {
            // output >= 0 == press key, <= 0 == don't press
            action.set(MarioKey.getMarioKey(i), networkOutput[i] >= 0.5);
        }

        // todo: put this in parent class somehow
        lastInput = action;

        return action;
    }

}
