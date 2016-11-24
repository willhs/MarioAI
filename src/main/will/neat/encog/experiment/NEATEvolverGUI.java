package will.neat.encog.experiment;

import ch.idsia.benchmark.mario.options.FastOpts;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.commons.math3.analysis.function.Abs;
import will.mario.agent.encog.EncogAgent;
import will.neat.AbstractMarioFitnessFunction;
import will.neat.encog.gui.HyperNEATGUI;
import will.neat.params.NEATParameters;
import will.neat.params.SpikeyNEATParameters;
import will.rf.action.StandardHoldStrat;

import java.util.logging.Logger;

/**
 * Created by Will on 4/08/2016.
 */
public class NEATEvolverGUI extends Application {
    // io
    private static Logger logger = Logger.getLogger(HyperNEATGUI.class
            .getSimpleName());

    private NEATParameters params = new NEATParameters();

    public NEATEvolverGUI() {
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Mario AI NEAT experiment");

        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 400, 400);
        primaryStage.setScene(scene);

        // headless checkbox
        CheckBox checkbox = new CheckBox("Headless");
        checkbox.setSelected(true);
        checkbox.selectedProperty().addListener((obs, old, newVal) ->
                AbstractMarioFitnessFunction.headless = newVal
        );
        root.setLeft(checkbox);

        // define neat
        NEATMarioEvolver evolver = new NEATMarioEvolver(params, () -> new StandardHoldStrat());
        evolver.setSimOptions(AbstractMarioFitnessFunction.DEFAULT_SIM_OPTIONS
            .replace(AbstractMarioFitnessFunction.LEVEL,
                FastOpts.LEVEL_08_FLY_SPIKY
            )
        );

        Task<Void> evolve = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // evolve til done
                evolver.run();

                logger.info("Evolving done");
//                logger.info("Winning fitness: " + neat.getPopulation().getBestGenome().getScore());

                return null;
            }
        };
        Thread thread = new Thread(evolve);
        thread.setDaemon(true);
        thread.start();

        primaryStage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
