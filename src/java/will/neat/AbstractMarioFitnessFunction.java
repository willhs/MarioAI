package will.neat;

import ch.idsia.benchmark.mario.MarioSimulator;
import ch.idsia.benchmark.mario.options.FastOpts;
import ch.idsia.benchmark.mario.options.MarioOptions;
import javafx.concurrent.Task;
import will.mario.agent.NEATAgent;
import will.neat.neuroph.Visualiser;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by hardwiwill on 21/07/16.
 * Generic 'N' represents the kind of neural network that is being evaluated
 */
public abstract class AbstractMarioFitnessFunction<N> {

    public static String LEVEL = FastOpts.LEVEL_05_GAPS;
    public static String TIME_LIMIT = MarioOptions.IntOption.SIMULATION_TIME_LIMIT.getParam() + " 100";
    public static String DIFFICULTY = FastOpts.L_DIFFICULTY(2);
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

    protected final int TRIALS = 5;

    protected final boolean RUNNING_PSO = false;

    protected static double bestFitness = 0;

    protected boolean headless = false;

    public AbstractMarioFitnessFunction() {
        if (RUNNING_PSO) {
            headless = true;
            Logger.getGlobal().setLevel(Level.OFF);
            LogManager.getLogManager().reset();
        }
    }

    protected double evaluate(NEATAgent agent, N nn, Logger logger) {
        double fitnessSum = 0;

        for (int t = 0; t < TRIALS; t++) {
            int seed = new Random().nextInt();
            String simOptions = getSimOptions(seed);

            float trialFitness = playMario(agent, simOptions);

            if (shouldPlayback(trialFitness)) {
                String vizSimOptions = simOptions.replace(FastOpts.VIS_OFF, FastOpts.VIS_ON_2X);
                playMario(agent, vizSimOptions);
            }

            if (trialFitness > bestFitness) {
                logRun(logger, trialFitness, nn);
            }

            updateBestFitness(trialFitness);

            fitnessSum += trialFitness;
        }

        double averageFitness = fitnessSum / TRIALS;
        double fitnessVal = averageFitness;

        return fitnessVal;
    }

    protected float playMario(NEATAgent agent, String simOptions) {
        MarioSimulator simulator = new MarioSimulator(simOptions);
        simulator.run(agent);

        return agent.getFitness();
    }

    protected boolean shouldPlayback(double fitness) {
        return !headless && fitness > bestFitness && fitness > 6000;
    }

    private void updateBestFitness(double fitness) {
        if (fitness > bestFitness) {
            bestFitness = fitness;
        }
    }

    protected String getSimOptions(int seed) {
        return DEFAULT_SIM_OPTIONS
                + " " + MarioOptions.IntOption.LEVEL_RANDOM_SEED.getParam() + " " + seed;
    }

    protected void logRun(Logger logger, double fitness, N n) {
        logger.info("Fitness function saw new best fitness! = " + fitness);
    }

}
