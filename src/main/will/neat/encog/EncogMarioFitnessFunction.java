package will.neat.encog;

import ch.idsia.benchmark.mario.MarioSimulator;
import ch.idsia.benchmark.mario.options.FastOpts;
import ch.idsia.benchmark.mario.options.MarioOptions;
import javafx.concurrent.Task;
import org.encog.ml.CalculateScore;
import org.encog.ml.MLMethod;
import org.encog.neural.neat.NEATNetwork;
import will.mario.agent.NEATAgent;
import will.mario.agent.encog.AgentFactory;
import will.mario.agent.encog.EncogAgent;
import will.neat.AbstractMarioFitnessFunction;
import will.neat.neuroph.Visualiser;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Created by Will on 17/07/2016.
 */
public class EncogMarioFitnessFunction extends AbstractMarioFitnessFunction<NEATNetwork> implements CalculateScore {

    private static Logger logger = Logger.getLogger(EncogMarioFitnessFunction.class
            .getSimpleName());

    private static double total = 0;
    private static int runs = 0;

    private final AgentFactory DEFAULT_AGENT_SUPPLIER = network -> new EncogAgent(network);
    private AgentFactory agentFactory = DEFAULT_AGENT_SUPPLIER;

    public EncogMarioFitnessFunction() {
        super();
    }

    public EncogMarioFitnessFunction(boolean pso) {
        super(pso);
    }

    public EncogMarioFitnessFunction(String marioOptions, AgentFactory agentFactory) {
        super(marioOptions);
        this.agentFactory = agentFactory;
    }


    @Override
    public double calculateScore(MLMethod mlMethod) {
        NEATNetwork nn = (NEATNetwork) mlMethod;
//        System.out.println(nn == null);

        NEATAgent agent = agentFactory.create(nn);

        double score = evaluate(agent, nn, logger);
        runs++;
        total += score;
        return score;
    }
    @Override
    protected boolean shouldPlayBack(double fitness) {
        boolean should = super.shouldPlayBack(fitness)
                && fitness > (total/runs) * 2
//                && Math.random() < 0.02;
                ;
        if (should) logger.info("playing run with score: " + fitness);
        return should;
    }

    @Override
    protected void logRun(Logger logger, double fitness, NEATNetwork nn) {
        super.logRun(logger, fitness, nn);

        int numConnections = nn.getLinks().length;

        String weights = Arrays.toString(
                Arrays.stream(nn.getLinks())
                        .mapToDouble(l -> l.getWeight())
                        .toArray()
        );

        logger.info("connections: " + numConnections);
//        logger.info("connection weights: " + weights);
//        logger.info("connections: " + Arrays.toString(nn.getLinks()));
    }

    @Override
    public boolean shouldMinimize() {
        return false;
    }

    @Override
    public boolean requireSingleThreaded() {
        return true;
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


}
