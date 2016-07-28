package will.neat.encog;

import ch.idsia.benchmark.mario.MarioSimulator;
import ch.idsia.benchmark.mario.options.FastOpts;
import ch.idsia.benchmark.mario.options.MarioOptions;
import javafx.concurrent.Task;
import org.encog.ml.CalculateScore;
import org.encog.ml.MLMethod;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;
import org.encog.neural.neat.NEATNetwork;
import will.mario.agent.NEATAgent;
import will.mario.agent.encog.EncogAgent;
import will.mario.agent.neuroph.NeurophAgent;
import will.neat.AbstractMarioFitnessFunction;
import will.neat.neuroph.Visualiser;

import java.util.Random;
import java.util.logging.Logger;

/**
 * Created by Will on 17/07/2016.
 */
public class EncogMarioFitnessFunction extends AbstractMarioFitnessFunction<NEATNetwork> implements CalculateScore {

    private static Logger logger = Logger.getLogger(EncogMarioFitnessFunction.class
            .getSimpleName());

    public EncogMarioFitnessFunction() {
        super();
    }

    @Override
    public double calculateScore(MLMethod mlMethod) {
        NEATNetwork nn = (NEATNetwork) mlMethod;

        NEATAgent agent = new EncogAgent(nn);

        return evaluate(agent, nn, logger);
    }

    protected void visualise(MarioSimulator sim, NEATAgent agent, NEATNetwork nn, double fitness) {
        Task<Void> runMarioTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                sim.run(agent);
                return null;
            }
        };
        Visualiser viz = new Visualiser(runMarioTask, nn, fitness);

        viz.launch();
    }

    @Override
    protected boolean shouldPlayback(double fitness) {
        return !headless
//                && fitness > 5000
                && Math.random() < 0.001;
    }

    @Override
    protected void logRun(Logger logger, double fitness, NEATNetwork nn) {
        super.logRun(logger, fitness, nn);

//        logger.info("Num connections: " + nn.getLinks().length);
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
