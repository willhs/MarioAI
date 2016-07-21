package will.neat.encog;

import ch.idsia.benchmark.mario.MarioSimulator;
import ch.idsia.benchmark.mario.options.FastOpts;
import ch.idsia.benchmark.mario.options.MarioOptions;
import org.encog.ml.CalculateScore;
import org.encog.ml.MLMethod;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;
import org.encog.neural.neat.NEATNetwork;
import will.mario.agent.NEATAgent;
import will.mario.agent.encog.EncogAgent;
import will.mario.agent.neuroph.NeurophAgent;
import will.neat.AbstractFitnessFunction;

import java.util.Random;
import java.util.logging.Logger;

/**
 * Created by Will on 17/07/2016.
 */
public class EncogMarioFitnessFunction extends AbstractFitnessFunction implements CalculateScore {

    private static Logger logger = Logger.getLogger(EncogMarioFitnessFunction.class
            .getSimpleName());

    @Override
    public double calculateScore(MLMethod mlMethod) {
        NEATNetwork nn = (NEATNetwork) mlMethod;

        double fitnessSum = 0;

        for (int t = 0; t < TRIALS; t++) {
            NEATAgent agent = new EncogAgent(nn);

            float trialFitness = testRun(agent, nn);
            fitnessSum += trialFitness;
        }

        double averageFitness = fitnessSum / TRIALS;
        double fitnessVal = averageFitness;

        return fitnessVal;
    }

    private float testRun(NEATAgent agent, NEATNetwork nn) {

        int seed = new Random().nextInt();
        String trialSimOptions = DEFAULT_SIM_OPTIONS
                + " " + MarioOptions.IntOption.LEVEL_RANDOM_SEED.getParam() + " " + seed;

        MarioSimulator simulator = new MarioSimulator(trialSimOptions);
        simulator.run(agent);

        float trialFitness = agent.getFitness();

        if (trialFitness > bestFitness) {
            reportBest(nn, trialFitness);
            bestFitness = trialFitness;
        }

        boolean shouldVisualise = trialFitness == bestFitness && trialFitness > 6000;

        if (shouldVisualise) {
            if (!headless) {
                visualise((NEATNetwork)agent.getNN(), trialSimOptions);
            }
        }

        return trialFitness;
    }

    private void reportBest(NEATNetwork nn, float fitness) {
        logger.info("Fitness function saw new best fitness! = " + fitness);
        logger.info("connections: " + nn.getLinks().length);
    }

    private void visualise(NEATNetwork nn, String options) {
        String vizOnOptions = options
                .replace(FastOpts.VIS_OFF, FastOpts.VIS_ON_2X);

        MarioSimulator rerun = new MarioSimulator(vizOnOptions);
        NEATAgent reagent = new EncogAgent(nn);
        rerun.run(reagent);

/*        this.nn = nn;
        System.out.println("assigned nn: " + nn.getOutputNeurons().size());
        this.fitnessVal = fitnessVal;

        Application.launch();*/
    }

    @Override
    public boolean shouldMinimize() {
        return false;
    }

    @Override
    public boolean requireSingleThreaded() {
        return false;
    }
}
