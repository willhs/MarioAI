package will.neat.encog;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.CheckBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import org.encog.engine.network.activation.ActivationBipolarSteepenedSigmoid;
import org.encog.ml.ea.train.basic.TrainEA;
import org.encog.neural.hyperneat.HyperNEATGenome;
import org.encog.neural.neat.NEATPopulation;
import will.neat.AbstractMarioFitnessFunction;
import will.neat.params.HyperNEATParameters;

import java.util.logging.Logger;

/**
 * Created by Will on 17/07/2016.
 */
public class HyperNEATGUI extends Application {

    private static final double PADDING = 10;
    private static Logger logger = Logger.getLogger(HyperNEATGUI.class
            .getSimpleName());

    public HyperNEATGUI() {
    }

    private final double SCENE_WIDTH = 1000;
    private final double SCENE_HEIGHT = 600;
    private final double CANVAS_HEIGHT = 600;
    private final double CANVAS_WIDTH = 600;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Mario AI NEAT experiment");
        primaryStage.setOnCloseRequest(e -> { Platform.exit(); System.exit(0); });

        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
        primaryStage.setScene(scene);

        // top pane
        // checkbox for headless mode
        HBox top = new HBox();
        top.setPadding(new Insets(PADDING));
        CheckBox headless = new CheckBox("Headless");
        headless.setSelected(true);
        headless.selectedProperty().addListener((obs, old, newVal) ->
            AbstractMarioFitnessFunction.headless = newVal
        );
        top.getChildren().add(headless);
        root.setTop(top);

        Canvas canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        root.setCenter(canvas);
        root.setBackground(new Background(new BackgroundFill(Color.GRAY, null, null)));

        // define neat
        HyperNEATEvolver evolver = new HyperNEATEvolver();
        TrainEA neat = evolver.getNEAT();

        DrawNNStrategy draw = new DrawNNStrategy(canvas);
        neat.addStrategy(draw);

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, draw::rotateWithDrag);
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, draw::rotateWithDrag);

        // left info pane
        VBox left = new VBox();
        left.setPadding(new Insets(PADDING));
        populatLeftPane(left, evolver.getParams());
        root.setLeft(left);

        Task<Void> evolve = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // evolve til done
                while (!neat.isTrainingDone()) {
                    neat.iteration();
                    logIteration(neat);
                }

                logger.info("Evolving done");
                logger.info("Winning fitness: " + neat.getPopulation().getBestGenome().getScore());

                return null;
            }
        };
        Thread thread = new Thread(evolve);
        thread.setDaemon(true);
        thread.start();

        primaryStage.show();
    }

    private void populatLeftPane(VBox left, HyperNEATParameters params) {
        left.getChildren().add(new Text("-- Network --")); // spacing
        addTextField(left, "Activation cycles", params.ACTIVATION_CYCLES);
        addTextField(left, "NN weight range", params.NN_WEIGHT_RANGE);
        addTextField(left, "CPPN weight range", params.CPPN_WEIGHT_RANGE);
        addTextField(left, "CPPN min weight", params.CPPN_MIN_WEIGHT);
        addTextField(left, "Initial conn density", params.CPPN_MIN_WEIGHT);
        addTextField(left, "Activation function",
                params.ACTIVATION_FUNCTION instanceof ActivationBipolarSteepenedSigmoid
                ? "Steepened sigmoid"
                : "Clipped Linear"
        );
        addTextField(left, "Activation cycles", params.ACTIVATION_CYCLES);
        left.getChildren().add(new Text(""));
        left.getChildren().add(new Text("-- Evolution --")); // spacing
        addTextField(left, "Selection prop", params.SELECTION_PROP);
        addTextField(left, "Elite rate", params.ELITE_RATE);
        addTextField(left, "Crossover prob", params.CROSSOVER_PROB);
        left.getChildren().add(new Text(""));
        left.getChildren().add(new Text("-- Mutations --")); // spacing
        addTextField(left, "Add conn prob", params.ADD_CONN_PROB);
        addTextField(left, "Add neuron prob", params.ADD_NEURON_PROB);
        addTextField(left, "Perturb weight prob", params.PERTURB_PROB);
        addTextField(left, "Perturb SD", params.PERTURB_SD);
        addTextField(left, "Perturb type", params.WEIGHT_MUT_TYPE.name());
        addTextField(left, "Perturb prop*", params.WEIGHT_PERTURB_PROP);
        addTextField(left, "Perturb reset prob", params.RESET_WEIGHT_PROB);
        left.getChildren().add(new Text("")); // spacing
        left.getChildren().add(new Text("-- Speciation -- ")); // spacing
        addTextField(left, "Max species", params.MAX_SPECIES);
        addTextField(left, "No improve gens", params.MAX_GENS_SPECIES);
        addTextField(left, "Init compat thresh", params.INIT_COMPAT_THRESHOLD);
    }

    private void addTextField(Pane pane, String name, Object val) {
        String label = String.format("%-25s %-25s", name, val);
        Text text = new Text(label);
        text.setFont(javafx.scene.text.Font.font("Consolas", 12));
        pane.getChildren().add(text);
    }

    private void logIteration(TrainEA neat) {
        NEATPopulation population = (NEATPopulation) neat.getPopulation();
        double bestFitness = population.getBestGenome().getScore();
//            double bestFitnessGen = population.determineBestSpecies().getLeader().getScore();

        int numSpecies = population.getSpecies().size();

        double averageCPPNLinks = population.getSpecies().stream()
                .map(s -> s.getMembers())
                .flatMap(genomes -> genomes.stream())
                .mapToInt(genome -> ((HyperNEATGenome)genome).getLinksChromosome().size())
                .average()
                .getAsDouble();

        double averageCPPNNodes = population.getSpecies().stream()
                .map(s -> s.getMembers())
                .flatMap(genomes -> genomes.stream())
                .mapToInt(genome -> ((HyperNEATGenome)genome).getNeuronsChromosome().size())
                .average()
                .getAsDouble();


        logger.info("Generation:\t" + neat.getIteration());
        logger.info("Best fitness:\t" + bestFitness);
        logger.info("Num species:\t" + numSpecies);
        logger.info("Ave CPPN conns:\t" + averageCPPNLinks);
        logger.info("Ave CPPN nodes:\t" + averageCPPNNodes);
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

}

