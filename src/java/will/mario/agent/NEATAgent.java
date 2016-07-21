package will.mario.agent;

import ch.idsia.benchmark.mario.engine.input.MarioInput;
import ch.idsia.benchmark.mario.engine.input.MarioKey;
import org.neuroph.core.NeuralNetwork;
import will.rf.action.ActionStrategy;
import will.rf.action.StandardHoldActionStrat;
import will.rf.environment.EnvEntEnvironment;
import will.rf.environment.GameEnvironment;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Will on 29/06/2016.
 */
public abstract class NEATAgent extends MarioAIBase2 {
    private int[] keysPressed = new int[4];

    private Map<MarioKey, Integer> keysHeld;

    public NEATAgent() {
         keysHeld = new HashMap<>();
    }

    protected abstract double[] activateNetwork(double[] inputs);

    @Override
    public MarioInput actionSelection() {
        // one grid for environment + separate grid for each enemy
/*        GameEnvironment env = new EnvEnemyEnvironment(
                EntityType.GOOMBA,
                EntityType.SPIKY
        );*/

        GameEnvironment env = new EnvEntEnvironment();

        return actionSelection(env);
    }

    protected MarioInput actionSelection(GameEnvironment env) {
        updateActionsHeld();

        double[] inputs = env.asInputNeurons(environment, lastInput);

        // put tiles through the neural network to receive game inputs
        // 1 or 0 for each of the game inputs: [left,right,down,jump,speed/attack,up(useless)]
        double[] networkOutput = activateNetwork(inputs);

        MarioInput action = mapNeuronsToAction(networkOutput);

        if (action.getPressed().size() >= 2) {
//            System.out.println("Pressed " + action.getPressed());
//            System.out.println(Arrays.toString(networkOutput));
        }

        if (Math.random() < 0.0001) {
//            System.out.println(Arrays.toString(networkOutput));
        }

        lastInput = action;

        return action;
    }

    private MarioInput mapNeuronsToAction(double[] outputNeurons) {

        ActionStrategy actionStrat = new StandardHoldActionStrat();
        MarioInput action = actionStrat.makeAction(outputNeurons, lastInput, keysHeld);

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

    protected void updateActionsHeld() {
        keysHeld.forEach((key, frames) -> {
//            if (frames > 0) {
                keysHeld.put(key, frames - 1);
//            }
        });
    }

    public abstract Object getNN();
}
