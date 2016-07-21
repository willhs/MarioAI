package will.neat.neuroph;

import ch.idsia.benchmark.mario.MarioSimulator;
import ch.idsia.benchmark.mario.options.FastOpts;
import ch.idsia.benchmark.mario.options.MarioOptions;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.stage.Stage;
import org.neuroph.contrib.neat.gen.NeuronType;
import org.neuroph.contrib.neat.gen.operations.FitnessFunction;
import org.neuroph.contrib.neat.gen.operations.OrganismFitnessScore;
import org.neuroph.core.NeuralNetwork;
import will.mario.agent.NEATAgent;
import will.mario.agent.neuroph.NeurophAgent;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by Will on 29/06/2016.
 */
public class NeurophFitnessFunction implements FitnessFunction {

    private static final int TRIALS = 5;
    public static String LEVEL = FastOpts.LEVEL_06_GOOMBA;
    public static String TIME_LIMIT = FastOpts.S_TIME_LIMIT_200;
    public static String DIFFICULTY = FastOpts.L_DIFFICULTY(0);
    public static String MARIO_TYPE = FastOpts.S_MARIO_SMALL;
    public static String LEVEL_LENGTH = FastOpts.L_LENGTH_512;

    private static Logger logger = Logger.getLogger(NeurophFitnessFunction.class
            .getSimpleName());

    public static String DEFAULT_SIM_OPTIONS = ""
            + FastOpts.VIS_OFF
            + LEVEL
            + DIFFICULTY
            + MARIO_TYPE
            + TIME_LIMIT
            + LEVEL_LENGTH
            ;

    private static double bestFitness = 0;

    private final boolean RUNNING_PSO = true;
    private boolean headless = true;

    public NeurophFitnessFunction() {
        if (RUNNING_PSO) {
            headless = true;
            Logger.getGlobal().setLevel(Level.OFF);
            LogManager.getLogManager().reset();
        }
    }

    @Override
    public void evaluate(List<OrganismFitnessScore> fitnesses) {
        fitnesses.stream().forEach(ofs -> {
            NeuralNetwork nn = ofs.getNeuralNetwork();

            double fitnessSum = 0;

            for (int t = 0; t < TRIALS; t++) {
                NEATAgent agent = new NeurophAgent(nn);

                float trialFitness = testRun(agent, ofs);
                fitnessSum += trialFitness;
            }

            double averageFitness = fitnessSum / TRIALS;
            double fitnessVal = averageFitness;

            ofs.setFitness(fitnessVal);
        });
    }

    private float testRun(NEATAgent agent, OrganismFitnessScore ofs) {

        int seed = new Random().nextInt();
        String trialSimOptions = DEFAULT_SIM_OPTIONS
                + " " + MarioOptions.IntOption.LEVEL_RANDOM_SEED.getParam() + " " + seed;

        MarioSimulator simulator = new MarioSimulator(trialSimOptions);
        simulator.run(agent);

        float trialFitness = agent.getFitness();

        if (trialFitness > bestFitness) {
            reportBest(ofs, trialFitness);
            bestFitness = trialFitness;
        }

        boolean shouldVisualise = trialFitness == bestFitness && trialFitness > 6000;

        if (shouldVisualise) {
            if (!headless) {
                visualise((NeuralNetwork)agent.getNN(), trialSimOptions);
            }
        }

        return trialFitness;
    }

    private void reportBest(OrganismFitnessScore ofs, float fitness) {
        logger.info("Fitness function saw new best fitness! = " + fitness);
        logger.info("hidden neurons: " + ofs.getOrganism().getNeurons(NeuronType.HIDDEN).size());
        logger.info("connections: " + ofs.getOrganism().getConnections().size());
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
    }*/
}
