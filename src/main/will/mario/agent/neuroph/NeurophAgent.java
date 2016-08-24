package will.mario.agent.neuroph;

import org.neuroph.core.NeuralNetwork;
import will.mario.agent.NEATAgent;

/**
 * Created by Will on 17/07/2016.
 */
public class NeurophAgent extends NEATAgent {
    protected NeuralNetwork nn;

    public NeurophAgent(NeuralNetwork nn) {
        this.nn = nn;
    }

    @Override
    protected double[] activateNetwork(double[] inputs) {
        nn.setInput(inputs);
        nn.calculate();
        return nn.getOutputAsArray();
    }

    @Override
    public Object getNN() {
        return nn;
    }
}
