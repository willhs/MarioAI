package will.neat.neuroph;

import ch.idsia.benchmark.mario.engine.input.MarioKey;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.NumberAxis;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.neuroph.core.Connection;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.Neuron;
import will.util.Algorithms;

import java.util.List;

/**
 * Created by Will on 30/06/2016.
 */
public class Visualiser extends Application{

    private static final int SCREEN_HEIGHT = 400;
    private static final int SCREEN_WIDTH = 700;
    private static final int CANVAS_WIDTH = 700;

    private Canvas canvas;

    public static void drawNeuralNet(Canvas canvas, NeuralNetwork nn, double fitness) {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        int gridX = 0, gridY = 0;
        int rows = 19, cols = 19;
        int gridSize = 400;
        double squareSize = (double)gridSize/rows;

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        // draw fitness
        gc.strokeText("" + fitness, cols * 0.3 , gridY);

        // draw grid
        // draw horiz lines
        for (int r = 0; r < rows+1; r++) {
            int y = (int)(gridY + (r * squareSize));
            gc.strokeLine(gridX, y, gridX+gridSize, y);
        }
        // draw vert lines
        for (int c = 0; c < cols+1; c++) {
            int x = (int)(gridX + (c * squareSize));
            gc.strokeLine(x, gridY, x, gridY+gridSize);

        }

        int actionsPaddingTop = 10;
        int actionsX = 600;

        nn.getOutputNeurons().forEach(outputNeuron -> {

            int actionsIndex = nn.getOutputNeurons().indexOf(outputNeuron);

            int destX = actionsX;
            int destY = (int) (actionsPaddingTop + ((double) actionsIndex / nn.getOutputNeurons().size())
                            * canvas.getHeight());

            outputNeuron.getInputConnections().forEach(conn -> {

                Neuron src = conn.getConnectedNeuron();

                if (nn.getInputNeurons().contains(src)) {
                    // good
                    int srcIndex = nn.getInputNeurons().indexOf(src);

                    int srcR = srcIndex / cols;
                    int srcC = srcIndex == 0 ? 0 : srcIndex % cols;

                    int srcX = (int)((srcR * squareSize) + (squareSize/2));
                    int srcY = (int)((srcC * squareSize) + (squareSize/2));
                    drawConnection(gc, conn, srcX, srcY, destX, destY);
                }
            });
        });

//        endNodes.forEach(n -> {
//            int index = endNodes.indexOf(n);
//            MarioKey k = MarioKey.getMarioKey(index);
//            int destY = (int)(actionsPaddingTop + ((double)index / endNodes.size()) * canvas.getHeight());
//            int textPaddingX = 20;
//
//            gc.setLineWidth(1);
//            gc.setStroke(Color.BLUEVIOLET);
//            gc.strokeText(k.getFeature(), actionsX + textPaddingX, destY);
//        });
    }

    private static void drawConnection(GraphicsContext gc, Connection conn, double srcX, double srcY, double destX, double destY) {

        int maxConnectionWidth = 5;

        // clamped because i'm not sure at this point what the range of weights are
        double clampedWeight = Algorithms.clamp(conn.getWeight().getValue(), -1, 1);

        // positive
        double scaled = Algorithms.scaleToRange(clampedWeight, -1, 1, 0, 1);

        double redAmount = scaled;
        double blueAmount = 1 - redAmount;

        // absolute weight (assumes min + max = 0)
        double lineWidth = Math.abs(clampedWeight) * maxConnectionWidth;

        // draw line
        gc.setStroke(Color.color(redAmount, 0, blueAmount));
        gc.setLineWidth(lineWidth);
        gc.strokeLine(srcX, srcY, destX, destY);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        primaryStage.setTitle("MarioAI");

        Group root = new Group();
        Scene scene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);
        canvas = new Canvas(CANVAS_WIDTH,SCREEN_HEIGHT);

        root.getChildren().add(canvas);
        primaryStage.setScene(scene);

        //drawNeuralNet( nn, fitness);

        primaryStage.show();
    }

    public void begin() {
        Application.launch();
    }
}
