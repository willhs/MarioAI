package will.mario.agent.encog;

import ch.idsia.agents.AgentOptions;
import ch.idsia.benchmark.mario.engine.input.MarioInput;
import ch.idsia.benchmark.mario.options.MarioOptions;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.neat.NEATNetwork;
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
        MLData data = new BasicMLData(inputs.length);
        data.setData(inputs);

        return network.compute(data).getData();
    }

    @Override
    public Object getNN() {
        return network;
    }
}
