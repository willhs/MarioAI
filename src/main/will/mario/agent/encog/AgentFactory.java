package will.mario.agent.encog;

import org.encog.neural.neat.NEATNetwork;
import will.mario.agent.NEATAgent;

/**
 * Created by Will on 8/10/2016.
 */
public interface AgentFactory {
    NEATAgent create(NEATNetwork nn);
}
