package ch.idsia.agents.impl.neat;

import ch.idsia.benchmark.mario.MarioSimulator;
import ch.idsia.benchmark.mario.options.FastOpts;
import com.anji.neat.Evolver;
import org.jgap.BulkFitnessFunction;
import org.jgap.Chromosome;

import java.util.List;

/**
 * Created by Will on 17/04/2016.
 */
public class MarioFitnessFunction implements BulkFitnessFunction {

    private float highScore;

    @Override
    public void evaluate(List chromosomes) {
        for (Object chr : chromosomes) {
            Chromosome chromosome = (Chromosome) chr;
            // play mario
            int fitnessValue = evaluate(chromosome);
            chromosome.setFitnessValue(fitnessValue);

            // update high reward
            if (fitnessValue > highScore) {
                highScore = fitnessValue;
            }
        }
    }

    private int evaluate(Chromosome chromosome) {
        String options = FastOpts.VIS_OFF + FastOpts.LEVEL_02_JUMPING /* + FastOpts.L_ENEMY(Enemy.GOOMBA) */;

        NEATAgent agent = new NEATAgent(chromosome);

        MarioSimulator simulator = new MarioSimulator(options);
        simulator.run(agent);

        float fitness = agent.getFitness();
        return (int)fitness;
    }

    @Override
    public int getMaxFitnessValue() {
        return (int)highScore;
    }

    public static void main(String[] args) throws Throwable {
        String propertiesFilename = "mario.properties";
        Evolver.main(new String[]{ propertiesFilename });
    }

}
