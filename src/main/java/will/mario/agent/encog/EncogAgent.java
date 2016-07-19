package will.mario.agent.encog;

import ch.idsia.benchmark.mario.engine.input.MarioInput;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.neat.NEATNetwork;
import will.mario.agent.MarioAIBase2;
import will.mario.agent.NEATAgent;

/**
 * Created by Will on 17/07/2016.
 */
public class EncogAgent extends NEATAgent {

    protected NEATNetwork network;

    public EncogAgent(NEATNetwork network) {
        this.network = network;
    }

    @Override
    protected double[] activateNetwork(double[] inputs) {
        return network.compute(new BasicMLData(inputs)).getData();
    }

    @Override
    public MarioInput actionSelection() {
        return null;
    }
}
