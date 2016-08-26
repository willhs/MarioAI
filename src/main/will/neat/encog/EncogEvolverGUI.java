package will.neat.encog;

import com.sun.jndi.cosnaming.CNCtx;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.stage.Stage;

/**
 * Created by Will on 6/08/2016.
 */
public class EncogEvolverGUI extends Application {

    private static final double GUI_WIDTH = 300;
    private static final double GUI_HEIGHT = 200;


    @Override
    public void start(Stage primaryStage) throws Exception {

        primaryStage.setTitle("MarioAI");

        Group root = new Group();
        Scene scene = new Scene(root, GUI_WIDTH, GUI_HEIGHT);
        Canvas canvas = new Canvas(GUI_WIDTH, GUI_HEIGHT);

        root.getChildren().add(canvas);
        primaryStage.setScene(scene);

        EvolveTask evolveTask = new EvolveTask();

        Thread thread = new Thread(evolveTask);
        thread.setDaemon(true);
        thread.start();

//        drawNeuralNet();

        primaryStage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    private class EvolveTask extends Task<Void> {

        @Override
        protected Void call() throws Exception {
            EncogHyperNEATEvolver evolver = new EncogHyperNEATEvolver();
            evolver.start(null);
            return null;
        }
    }

}
