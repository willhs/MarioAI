package will.mario.agent;

import ch.idsia.agents.AgentOptions;
import ch.idsia.benchmark.mario.engine.input.MarioInput;
import ch.idsia.benchmark.mario.engine.input.MarioKey;
import will.rf.action.ActionStrategy;
import will.rf.action.StandardHoldActionStrat;
import will.rf.environment.BinaryEnvGridEnvironment;
import will.rf.environment.EnvEnemyGrid;
import will.rf.environment.GameEnvironment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

/**
 * Created by Will on 29/06/2016.
 */
public abstract class NEATAgent extends MarioAIBase2 {

    private static Logger logger = Logger.getLogger(NEATAgent.class
            .getSimpleName());

    // how many frames should each key be held for
    private Map<MarioKey, Integer> keysHeld = new HashMap<>();

    private boolean shouldPrint = false;
    private double[] lastFrame;

    public NEATAgent(){}

    public NEATAgent(boolean shouldPrint) {
        this.shouldPrint = shouldPrint;
    }

    protected abstract double[] activateNetwork(double[] inputs);

    @Override
    public MarioInput actionSelection() {
        // one grid for environment + separate grid for each enemy
/*        GameEnvironment env = new EnvEnemyEnvironment(
                EntityType.GOOMBA,
                EntityType.SPIKY
        );*/

        GameEnvironment env = new BinaryEnvGridEnvironment();
//        GameEnvironment env = new EnvEnemyGrid();

        return actionSelection(env);
    }

    protected MarioInput actionSelection(GameEnvironment env) {
        updateActionsHeld();

        double[] environment = env.asInputNeurons(this.environment, lastInput);
//        if (lastFrame == null) {
//            lastFrame = environment;
//        }
//        double[] percepts = DoubleStream.concat(Arrays.stream(environment), Arrays.stream(lastFrame)).toArray();

        // put tiles through the neural network to receive game inputs
        // 1 or 0 for each of the game inputs: [left,right,down,jump,speed/attack,up(useless)]
        double[] networkOutput = activateNetwork(environment);
        lastFrame = environment;

        if (shouldPrint) {
            // print environment grid
/*            System.out.println("-----------------------------------------------------------");
            for (int r = 0; r < 19; r++) {
                double[] col = Arrays.copyOfRange(environment, r * 19, (r + 1) * 19);
                System.out.println(Arrays.toString(col));
            }
            System.out.println("-----------------------------------------------------------");*/

            System.out.println("Network output: " + Arrays.toString(networkOutput));
        }

        MarioInput action = mapNeuronsToAction(networkOutput);

        lastInput = action;

        return action;
    }

    private MarioInput mapNeuronsToAction(double[] outputNeurons) {

        ActionStrategy actionStrat = new StandardHoldActionStrat();
        MarioInput action = actionStrat.makeAction(outputNeurons, lastInput, keysHeld);

        return action;
    }

    protected void updateActionsHeld() {
        keysHeld.forEach((key, frames) -> keysHeld.put(key, frames - 1));
    }

    public abstract Object getNN();

    @Override
    public void reset(AgentOptions options) {
        super.reset(options);
        keysHeld = new HashMap<>();
    }

    public void shouldPrint(boolean shouldPrint) {
        this.shouldPrint = shouldPrint;
    }
}
