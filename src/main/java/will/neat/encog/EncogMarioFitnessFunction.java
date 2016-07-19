package will.neat.encog;

import org.encog.ml.CalculateScore;
import org.encog.ml.MLMethod;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;

/**
 * Created by Will on 17/07/2016.
 */
public class EncogMarioFitnessFunction implements CalculateScore {
    @Override
    public double calculateScore(MLMethod mlMethod) {
        return -1;
//        return ((EvolutionaryAlgorithm) mlMethod).;
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
