package will.mario.agent.anji;

import com.anji.integration.Activator;
import will.mario.agent.NEATAgent;

/**
 * Created by Will on 17/05/2016.
 */
public class ANJINEATAgent extends NEATAgent {
    /**
     used to activate the neural network
     */
    protected final Activator activator;

    public ANJINEATAgent(Activator activator) {
        this.activator = activator;
    }

    @Override
    protected double[] activateNetwork(double[] inputs) {
        return activator.next(inputs);
    }

    @Override
    public Object getNN() {
        return activator;
    }
}
