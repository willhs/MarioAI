package will.neat.anji;

import ch.idsia.benchmark.mario.engine.input.MarioKey;
import com.anji.neat.*;
import will.util.Algorithms;
import com.anji.util.Properties;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.jgap.Allele;
import org.jgap.Chromosome;

import java.util.List;
import java.util.SortedSet;
import java.util.stream.Collectors;

/**
 * Created by Will on 8/06/2016.
 */
public class MarioEvolverGui extends Application{

    private static final int SCREEN_HEIGHT = 400;
    private static final int SCREEN_WIDTH = 700;
    private static final int CANVAS_WIDTH = 700;

    private static Properties props;

    public static void main(String[] args) throws Throwable {

        if (args.length >= 1) {
            props = new Properties(args[0]);
        } else {
            String propertiesFilename = "mario.properties";
            props = new Properties(propertiesFilename);
        }

        // start gui
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("MarioAI");

        Group root = new Group();
        Scene scene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);
        Canvas canvas = new Canvas(CANVAS_WIDTH,SCREEN_HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        root.getChildren().add(canvas);
        primaryStage.setScene(scene);
        primaryStage.show();

        // initialise evolver
        Evolver evolver = new Evolver();
        evolver.init(props);

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                evolver.run();
                return null;
            }
        };

        new Thread(task).start();

        new AnimationTimer() {
            private double lastFitness;
            @Override
            public void handle(long now) {
                // redraw if new fitness

                double fitness = evolver.getChamp().getFitnessValue();
                if (fitness > lastFitness) {
                    //System.out.println("fitness increased!");
                    gc.clearRect(0,0, SCREEN_WIDTH, SCREEN_HEIGHT);
                    drawChamp(canvas, evolver);
                }
                lastFitness = fitness;
            }
        }.start();
    }

    public void drawChamp(Canvas canvas, Evolver evolver) {
        Chromosome champ = evolver.getChamp();
        GraphicsContext gc = canvas.getGraphicsContext2D();

        int gridX = 0, gridY = 0;
        int rows = 19, cols = 19;
        int gridSize = 400;
        double squareSize = (double)gridSize/rows;

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        // draw fitness
        double fitness = champ.getFitnessValue();
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
        // draw connections from src nodes
        SortedSet<Allele> alleles = (SortedSet<Allele>)champ.getAlleles();

        List<NeuronAllele> startNodes = alleles.stream()
                .filter(a -> a instanceof NeuronAllele)
                .map(a -> (NeuronAllele) a)
                .filter(na -> na.getType().toString().equals(NeuronType.INPUT.toString()))
                .collect(Collectors.toList());

        List<NeuronAllele> endNodes = alleles.stream()
                .filter(a -> a instanceof NeuronAllele)
                .map(a -> (NeuronAllele) a)
                .filter(na -> na.getType().toString().equals(NeuronType.OUTPUT.toString()))
                .collect(Collectors.toList());

        List<NeuronAllele> hiddenNodes = alleles.stream()
                .filter(a -> a instanceof NeuronAllele)
                .map(a -> (NeuronAllele) a)
                .filter(na -> na.getType().toString().equals(NeuronType.OUTPUT.toString()))
                .collect(Collectors.toList());

        List<ConnectionAllele> connections = alleles.stream()
                .filter(a -> a instanceof ConnectionAllele)
                .map(a -> (ConnectionAllele) a)
                .collect(Collectors.toList());

        int actionsPaddingTop = 10;
        int actionsX = 600;

        connections.forEach(conn -> {
            NeuronAllele src = startNodes.stream()
                    .filter(n -> n.getInnovationId() == conn.getSrcNeuronId() )
                    .findFirst()
                    .get();

            int srcIndex = startNodes.indexOf(src);

            int srcR = srcIndex / cols;
            int srcC = srcIndex == 0 ? 0 : srcIndex % cols;

            int srcX = (int)((srcR * squareSize) + (squareSize/2));
            int srcY = (int)((srcC * squareSize) + (squareSize/2));

            NeuronAllele dest = endNodes.stream()
                    .filter(n -> n.getInnovationId() == conn.getDestNeuronId() )
                    .findFirst().orElse(null);

            int actionsIndex = dest == null ? hiddenNodes.indexOf(dest)
                    : endNodes.indexOf(dest);

            int destX = actionsX;
            int destY = -1;

            // if in endNodes
            if (actionsIndex == endNodes.indexOf(dest)) {
                //System.out.println("marioKeys index: " + actionsIndex);
                destY = (int)(actionsPaddingTop + ((double)actionsIndex / endNodes.size()) * canvas.getHeight());
                // if in endNodes
            } else if (actionsIndex == hiddenNodes.indexOf(dest)){
                destY = srcY;
            } else {
                System.err.println("dest node wasn't found");
            }

//			System.out.println("srcIndex: " + srcIndex + ", src r,c: " + srcR + "," + srcC);
//			System.out.println("conn weight: " + conn.getWeight());

            int maxConnectionWidth = 5;

            // requires modified lib
/*            NeatConfiguration config = evolver.getConfig();

            double colourAmount = Algorithms.scaleToRange(
                    conn.getWeight(),
                    config.getMinConnectionWeight(),
                    config.getMaxConnectionWeight(),
                    0,
                    1
            );

            // absolute weight (assumes min + max = 0)
            double lineAmount = Algorithms.scaleToRange(
                    Math.abs(conn.getWeight()),
                    0,
                    config.getMaxConnectionWeight(),
                    0,
                    1
            );

//            System.out.println("ratio: " + ratio);

            // draw line
            gc.setStroke(Color.color(colourAmount, 0, 0));
            gc.setLineWidth(lineAmount * maxConnectionWidth);
            gc.strokeLine(srcX, srcY, destX, destY);*/
        });

        endNodes.forEach(n -> {
            int index = endNodes.indexOf(n);
            MarioKey k = MarioKey.getMarioKey(index);
            int destY = (int)(actionsPaddingTop + ((double)index / endNodes.size()) * canvas.getHeight());
            int textPaddingX = 20;

            gc.setLineWidth(1);
            gc.setStroke(Color.BLUEVIOLET);
            gc.strokeText(k.getName(), actionsX + textPaddingX, destY);
        });
    }
}
