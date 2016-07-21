package will.neat;

import ch.idsia.benchmark.mario.options.FastOpts;

import java.util.logging.Logger;

/**
 * Created by hardwiwill on 21/07/16.
 */
public abstract class AbstractFitnessFunction {

    protected static final int TRIALS = 5;
    public static String LEVEL = FastOpts.LEVEL_06_GOOMBA;
    public static String TIME_LIMIT = FastOpts.S_TIME_LIMIT_200;
    public static String DIFFICULTY = FastOpts.L_DIFFICULTY(0);
    public static String MARIO_TYPE = FastOpts.S_MARIO_SMALL;
    public static String LEVEL_LENGTH = FastOpts.L_LENGTH_512;

    public static String DEFAULT_SIM_OPTIONS = ""
            + FastOpts.VIS_OFF
            + LEVEL
            + DIFFICULTY
            + MARIO_TYPE
            + TIME_LIMIT
            + LEVEL_LENGTH
            ;

    protected static double bestFitness = 0;

    protected final boolean RUNNING_PSO = true;
    protected boolean headless = true;
}

