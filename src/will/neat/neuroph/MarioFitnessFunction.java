package will.neat.neuroph;

import ch.idsia.benchmark.mario.MarioSimulator;
import ch.idsia.benchmark.mario.engine.SimulatorOptions;
import ch.idsia.benchmark.mario.options.FastOpts;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.Stage;
import org.neuroph.contrib.neat.gen.Evolver;
import org.neuroph.contrib.neat.gen.NeuronType;
import org.neuroph.contrib.neat.gen.operations.FitnessFunction;
import org.neuroph.contrib.neat.gen.operations.OrganismFitnessScore;
import org.neuroph.core.NeuralNetwork;
import will.mario.agent.neuroph.EnvironmentOnlyAgent;
import will.mario.agent.neuroph.NEATAgent;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

/**
 * Created by Will on 29/06/2016.
 */
public class MarioFitnessFunction extends Application implements FitnessFunction {
    public static String LEVEL = FastOpts.LEVEL_02_JUMPING;

    private static Logger logger = Logger.getLogger(MarioFitnessFunction.class
            .getSimpleName());

    public static String VIZ_OFF_OPTIONS = ""
            + FastOpts.VIS_OFF
            + LEVEL
            + FastOpts.S_TIME_LIMIT_200;

    public static String VIZ_ON_OPTIONS = ""
            + FastOpts.VIS_ON_2X
//            + " " + MarioOptions.IntOption.SIMULATION_TIME_LIMIT.getParam() + " 50"
//                + " " + MarioOptions.IntOption.VISUALIZATION_FPS.getParam() + " 30"
            + FastOpts.VIS_FIELD(SimulatorOptions.ReceptiveFieldMode.GRID)
            + LEVEL
            + FastOpts.S_TIME_LIMIT_200;

    private static double bestFitness = 0;

    private final boolean HEADLESS = true;

    // temporary
    private NeuralNetwork nn;
    private double fitnessVal;

    @Override
    public void evaluate(List<OrganismFitnessScore> fitnesses) {
        fitnesses.stream().forEach(ofs -> {
            NeuralNetwork nn = ofs.getNeuralNetwork();

            NEATAgent agent = new EnvironmentOnlyAgent(nn);

            String simulatorOptions = VIZ_OFF_OPTIONS;

            if (ofs.getOrganism().getConnections().size() >= 9) {
//                simulatorOptions = VIZ_ON_OPTIONS;
            }
            MarioSimulator simulator = new MarioSimulator(simulatorOptions);
            simulator.run(agent);

            float fitnessVal = agent.getFitness();
            ofs.setFitness(fitnessVal);

            if (fitnessVal > bestFitness) {
                bestFitness = fitnessVal;
                logger.info("hidden neurons: " + ofs.getOrganism().getNeurons(NeuronType.HIDDEN).size());
                logger.info("connections: " + ofs.getOrganism().getNeurons().size());
                if (!HEADLESS) {
                    visualise(nn, fitnessVal);
                }
            }

            // if went right and jumped then play it back
            if (agent.getKeysPressed()[0] > 0 && agent.getKeysPressed()[2] > 0) {
            }

//            System.out.println("connections: " + ofs.getOrganism().getConnections().size());
        });
    }

    private void visualise(NeuralNetwork nn, double fitnessVal) {
        MarioSimulator rerun = new MarioSimulator(VIZ_ON_OPTIONS);
        NEATAgent reagent = new EnvironmentOnlyAgent(nn);
        rerun.run(reagent);

/*        this.nn = nn;
        System.out.println("assigned nn: " + nn.getOutputNeurons().size());
        this.fitnessVal = fitnessVal;

        Application.launch();*/
    }


    private static final int SCREEN_HEIGHT = 400;
    private static final int SCREEN_WIDTH = 700;
    private static final int CANVAS_WIDTH = 700;

    @Override
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
                NEATAgent reagent = new EnvironmentOnlyAgent(nn);
                rerun.run(reagent);

                // once done, stop gui thread
                MarioFitnessFunction.this.stop();
                return null;
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
}
