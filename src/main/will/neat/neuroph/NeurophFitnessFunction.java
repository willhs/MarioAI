package will.neat.neuroph;

import ch.idsia.benchmark.mario.MarioSimulator;
import ch.idsia.benchmark.mario.options.FastOpts;
import ch.idsia.benchmark.mario.options.MarioOptions;
import org.neuroph.contrib.neat.gen.operations.FitnessFunction;
import org.neuroph.contrib.neat.gen.operations.OrganismFitnessScore;
import org.neuroph.core.NeuralNetwork;
import will.mario.agent.NEATAgent;
import will.mario.agent.neuroph.NeurophAgent;
import will.neat.AbstractMarioFitnessFunction;

import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Will on 29/06/2016.
 */
public class NeurophFitnessFunction extends AbstractMarioFitnessFunction<NeuralNetwork> implements FitnessFunction {

    private static Logger logger = Logger.getLogger(NeurophFitnessFunction.class
            .getSimpleName());

    public NeurophFitnessFunction() {
        super();
    }

    @Override
    public void evaluate(List<OrganismFitnessScore> fitnesses) {
        fitnesses.stream().forEach(ofs -> {
            NeuralNetwork nn = ofs.getNeuralNetwork();

            NEATAgent agent = new NeurophAgent(nn);
            double fitnessVal = evaluate(agent, nn, logger);

            ofs.setFitness(fitnessVal);
        });
    }

    @Override
    protected void logRun(Logger logger, double fitness, NeuralNetwork nn) {
        logger.info("Fitness function saw new best fitness! = " + fitness);
    }

    @Override
    protected boolean shouldPlayBack(double fitness) {
        return !headless && fitness > bestFitness && fitness > 8000;
    }

    private void visualise(NeuralNetwork nn, String options) {
        String vizOnOptions = options
                .replace(FastOpts.VIS_OFF, FastOpts.VIS_ON_2X);

        MarioSimulator rerun = new MarioSimulator(vizOnOptions);
        NEATAgent reagent = new NeurophAgent(nn);
        rerun.run(reagent);

/*        this.nn = nn;
        System.out.println("assigned nn: " + nn.getOutputNeurons().size());
        this.fitnessVal = fitnessVal;

        Application.launch();*/
    }


    private static final int SCREEN_HEIGHT = 400;
    private static final int SCREEN_WIDTH = 700;
    private static final int CANVAS_WIDTH = 700;

/*    @Override
    public void start(Stage primaryStage) throws Exception {

        Group root = new Group();
        Scene scene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);
        Canvas canvas = new Canvas(CANVAS_WIDTH,SCREEN_HEIGHT);

        System.out.println(nn);
        Visualiser.drawNeuralNet(canvas, nn, fitnessVal);

        root.getChildren().add(canvas);
        primaryStage.setScene(scene);
        primaryStage.show();

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                MarioSimulator rerun = new MarioSimulator(VIZ_ON_OPTIONS);
                NEATAgent reagent = new NeurophAgent(nn);
                rerun.run(reagent);

                // once done, stop gui thread
                NeurophFitnessFunction.this.stop();
                return null;
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
        Group root = new Group();
        Scene scene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);
        Canvas canvas = new Canvas(CANVAS_WIDTH,SCREEN_HEIGHT);

        System.out.println(nn);
        Visualiser.drawNeuralNet(canvas, nn, fitnessVal);

        root.getChildren().add(canvas);
        primaryStage.setScene(scene);
        primaryStage.show();

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                MarioSimulator rerun = new MarioSimulator(VIZ_ON_OPTIONS);
                NEATAgent reagent = new NeurophAgent(nn);
                rerun.run(reagent);

                // once done, stop gui thread
                NeurophFitnessFunction.this.stop();
                return null;
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }*/
}
