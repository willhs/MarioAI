package will.mario.agent.encog;

import org.encog.neural.neat.NEATNetwork;
import will.rf.action.ActionStratFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Will on 9/10/2016.
 */
public class EncogAgentFS extends EncogAgent {
    private Point[] inputs;
    public EncogAgentFS(NEATNetwork nn, ActionStratFactory stratFactory, Point[] inputs) {
        super(nn, stratFactory);
        this.inputs = inputs;
    }

    @Override
    protected double[] selectFeatures(double[] environment) {
        double[] networkInput = new double[inputs.length];
        int inputIndex = 0;
        for (int env = 0; env < environment.length; env++) {
            for (int i = 0; i < inputs.length; i++) {
                Point input = inputs[i];
                if (env == this.environment.getTileField()[0].length * input.y + input.x) {
                    networkInput[inputIndex++] = environment[env];
                    break;
                }
            }
        }
        return networkInput;
    }
}
