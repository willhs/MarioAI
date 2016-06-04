package will.agent;

import ch.idsia.agents.controllers.MarioAIBase2;
import com.anji.integration.Activator;

/**
 * Created by Will on 17/05/2016.
 */
public abstract class NEATAgent extends MarioAIBase2 {
    /**
     used to activate the neural network
     */
    protected final Activator activator;

    public NEATAgent(Activator activator) {
        this.activator = activator;
    }
}
