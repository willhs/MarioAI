package will.mario.agent.neuroph;

import ch.idsia.agents.controllers.MarioAIBase2;
import ch.idsia.benchmark.mario.engine.input.MarioInput;
import ch.idsia.benchmark.mario.engine.input.MarioKey;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.Neuron;
import will.mario.environment.BinaryGridEnvironment;
import will.mario.environment.GameEnvironment;

import java.util.Arrays;

/**
 * Created by Will on 29/06/2016.
 */
public abstract class NEATAgent extends MarioAIBase2 {
    protected NeuralNetwork nn;

    private int[] keysPressed = new int[4];

    public NEATAgent(NeuralNetwork nn) {
        this.nn = nn;
//        System.out.println("==============================================");
//        System.out.println("                 START                        ");
//        System.out.println("==============================================");
    }

    @Override
    public MarioInput actionSelection() {

        GameEnvironment env = new BinaryGridEnvironment();
//        GameEnvironment env = new ValueGridEnvironment();
//        GameEnvironment env = new MultiGridEnvironment();

        return actionSelection(env);
    }

    protected MarioInput actionSelection(GameEnvironment env) {
        double[] inputs = env.getInputNeurons(environment, lastInput);
        nn.setInput(inputs);
        nn.calculate();

        // put tiles through the neural network to receive game inputs
        // 1 or 0 for each of the game inputs: [left,right,down,jump,speed/attack,up(useless)]
        double[] networkOutput = nn.getOutputAsArray();

        MarioInput action = mapNeuronsToAction(networkOutput);

        if (action.getPressed().size() >= 2) {
//            System.out.println("Pressed " + action.getPressed());
//            System.out.println(Arrays.toString(networkOutput));
        }

        if (Math.random() < 0.001) {
//            System.out.println(Arrays.toString(networkOutput));
        }

        lastInput = action;

        return action;
    }

    private MarioInput mapNeuronsToAction(double[] outputNeurons) {
        double threshold = 0.2;
        MarioInput action = new MarioInput();

        action.set(MarioKey.RIGHT, outputNeurons[0] > threshold);
        action.set(MarioKey.LEFT, outputNeurons[1] > threshold);
        action.set(MarioKey.JUMP, outputNeurons[2] > threshold);
        action.set(MarioKey.SPEED, outputNeurons[3] > threshold);

        action.getPressed().forEach(k -> {
            if (k == MarioKey.RIGHT) {
                keysPressed[0]++;
            } else if (k == MarioKey.LEFT) {
                keysPressed[1]++;
            } else if (k == MarioKey.JUMP) {
                keysPressed[2]++;
            } else if (k == MarioKey.SPEED) {
                keysPressed[3]++;
            }
        });

        return action;
    }

    public int[] getKeysPressed() {
        return keysPressed;
    }

}
