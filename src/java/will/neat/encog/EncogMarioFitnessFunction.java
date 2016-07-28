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
import will.neat.AbstractMarioFitnessFunction;

import java.util.Random;
import java.util.logging.Logger;

/**
 * Created by Will on 17/07/2016.
 */
public class EncogMarioFitnessFunction extends AbstractMarioFitnessFunction implements CalculateScore {

    private static Logger logger = Logger.getLogger(EncogMarioFitnessFunction.class
            .getSimpleName());

    public EncogMarioFitnessFunction() {
        super();
    }

    @Override
    public double calculateScore(MLMethod mlMethod) {
        NEATNetwork nn = (NEATNetwork) mlMethod;

        NEATAgent agent = new EncogAgent(nn);

        return evaluate(agent, logger);
    }

    @Override
    public boolean shouldMinimize() {
        return false;
    }

    @Override
    public boolean requireSingleThreaded() {
        return true;
    }
}
