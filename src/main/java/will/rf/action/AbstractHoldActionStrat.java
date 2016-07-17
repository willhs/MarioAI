package will.rf.action;

import ch.idsia.benchmark.mario.engine.input.MarioInput;
import ch.idsia.benchmark.mario.engine.input.MarioKey;

/**
 * Created by Will on 14/07/2016.
 */
public abstract class AbstractHoldActionStrat implements HoldActionStrat {

    protected MarioKey[] marioKeys = new MarioKey[]{
            MarioKey.RIGHT,
            MarioKey.LEFT,
            MarioKey.JUMP,
            MarioKey.SPEED
    };

    protected double threshold = 0.50;

    protected MarioInput action = new MarioInput();
}
