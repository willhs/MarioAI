package will.neat.neuroph;

import ch.idsia.benchmark.mario.MarioSimulator;
import ch.idsia.benchmark.mario.options.FastOpts;
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
import will.mario.agent.anji.ANJINEATAgent;
import will.mario.agent.NEATAgent;
import will.mario.agent.neuroph.NeurophAgent;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by Will on 29/06/2016.
 */
public class MarioFitnessFunction extends Application implements FitnessFunction {
    private static final double NOTABLE_FITNESS_JUMP = 100;
    public static String LEVEL = FastOpts.LEVEL_06_GOOMBA;
    public static String TIME_LIMIT = FastOpts.S_TIME_LIMIT_200;
    public static String DIFFICULTY = FastOpts.L_DIFFICULTY(0);
    public static String MARIO_TYPE = FastOpts.S_MARIO_SMALL;

    private static Logger logger = Logger.getLogger(MarioFitnessFunction.class
            .getSimpleName());

    public static String VIZ_OFF_OPTIONS = ""
            + FastOpts.VIS_OFF
            + LEVEL
            + DIFFICULTY
            + MARIO_TYPE
            + TIME_LIMIT
            ;

    public static String VIZ_ON_OPTIONS = VIZ_OFF_OPTIONS
            .replace(FastOpts.VIS_OFF, FastOpts.VIS_ON_2X)
//            + " " + MarioOptions.IntOption.SIMULATION_TIME_LIMIT.getParam() + " 50"
//                + " " + MarioOptions.IntOption.VISUALIZATION_FPS.getParam() + " 30"
//            + FastOpts.VIS_FIELD(SimulatorOptions.ReceptiveFieldMode.GRID)
            ;

    private static double bestFitness = 0;

    private final boolean RUNNING_PSO = false;
    private boolean headless = false;

    // temporary
    private NeuralNetwork nn;
    private double fitnessVal;

    public MarioFitnessFunction() {
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

            NEATAgent agent = new NeurophAgent(nn);

            String simulatorOptions = VIZ_OFF_OPTIONS;

            MarioSimulator simulator = new MarioSimulator(simulatorOptions);
            simulator.run(agent);

            float fitnessVal = agent.getFitness();
            ofs.setFitness(fitnessVal);

            if (fitnessVal > bestFitness) {
                logger.info("Fitness function saw new best fitness! = " + fitnessVal);
                logger.info("hidden neurons: " + ofs.getOrganism().getNeurons(NeuronType.HIDDEN).size());
                logger.info("connections: " + ofs.getOrganism().getConnections().size());

                // if larger jump in fitness is achieved, visualise
//                if (fitnessVal - bestFitness > NOTABLE_FITNESS_JUMP) {
                if (fitnessVal > 8000) {
                    if (!headless) {
                        visualise(nn, fitnessVal);
                    }
                }
                System.out.println("test");
                bestFitness = fitnessVal;
            }
        });
    }

    private void visualise(NeuralNetwork nn, double fitnessVal) {
        MarioSimulator rerun = new MarioSimulator(VIZ_ON_OPTIONS);
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
                NEATAgent reagent = new NeurophAgent(nn);
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
