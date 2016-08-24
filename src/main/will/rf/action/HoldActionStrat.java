package will.rf.action;

import ch.idsia.benchmark.mario.engine.input.MarioKey;

import java.util.Map;

/**
 * Created by Will on 14/07/2016.
 */
public interface HoldActionStrat extends ActionStrategy {
    public Map<MarioKey, Integer> getActionsToHold();
}
