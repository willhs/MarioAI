package will.rf.action;

import ch.idsia.benchmark.mario.engine.input.MarioInput;
import ch.idsia.benchmark.mario.engine.input.MarioKey;
import will.util.Algorithms;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Will on 14/07/2016.
 */
public class StandardHoldActionStrat extends AbstractHoldActionStrat{

    private static final int MIN_HOLD_FOR = 1;
    private static final int MAX_HOLD_FOR = 24;

    private Map<MarioKey, Integer> toHold;

    public StandardHoldActionStrat() {
        toHold = new HashMap<>();
    }

    @Override
    public MarioInput makeAction(double[] inputs, MarioInput currentAction, Map<MarioKey, Integer> keysHeld) {
        for (int i = 0; i < marioKeys.length; i++) {
            double input = inputs[i];
            MarioKey key = marioKeys[i];

            // if key is already held, skip it
            if (keysHeld.get(key) != null && keysHeld.get(key) >= 0) {
                continue;
            }

            if (input > threshold) {
                int holdFor = (int) Algorithms.scaleToRange(
                        input, threshold, 1, MIN_HOLD_FOR, MAX_HOLD_FOR
                );
                toHold.put(key, holdFor);
            }
        }

        // update keysHeld
        toHold.keySet().forEach(key -> {
            keysHeld.put(key, toHold.get(key));
        });

        // held keys should be pressed for this frame, otherwise released
        keysHeld.forEach((key, frames) -> {
            if (frames > 0) {
                action.press(key);
            } else {
                action.release(key);
            }
        });
        return action;
    }

    @Override
    public Map<MarioKey, Integer> getActionsToHold() {
        return toHold;
    }
}
