package will.neat.neuroph;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.Stage;
import org.jgap.Chromosome;
import org.neuroph.contrib.neat.gen.*;
import org.neuroph.contrib.neat.gen.impl.SimpleNeatParameters;
import org.neuroph.contrib.neat.gen.operations.MutationOperation;
import org.neuroph.contrib.neat.gen.operations.mutation.AddConnectionMutationOperation;
import org.neuroph.contrib.neat.gen.operations.mutation.AddNeuronMutationOperation;
import org.neuroph.contrib.neat.gen.operations.mutation.WeightMutationOperation;
import org.neuroph.contrib.neat.gen.operations.selector.NaturalSelectionOrganismSelector;
import org.neuroph.contrib.neat.gen.operations.speciator.DynamicThresholdSpeciator;
import org.neuroph.contrib.neat.gen.persistence.PersistenceException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Will on 28/06/2016.
 */
public class MarioEvolverGUI extends Application {

    private static final int NUM_INPUT_NEURONS = 367;
    private static final int NUM_OUTPUT_NEURONS = 4;
    private static final String PERSISTANCE_DIR = "db/neuroph";

    public static void main(String[] args) {
        Application.launch();
    }

    private NeatParameters makeNeatParameters() {
        // set up NEAT paramaters
        SimpleNeatParameters params = new SimpleNeatParameters();

        params.setFitnessFunction(new MarioFitnessFunction());
        params.setPopulationSize(500);
        params.setMaximumFitness(6000);
        params.setMaximumGenerations(500);

        DynamicThresholdSpeciator speciator = new DynamicThresholdSpeciator();
        speciator.setMaxSpecies(5);
        params.setSpeciator(speciator);

        NaturalSelectionOrganismSelector selector = (NaturalSelectionOrganismSelector) params
                .getOrganismSelector();
        selector.setSurvivalRatio(0.3);
//        selector.setKillUnproductiveSpecies(true);
//
        List<MutationOperation> ops = new ArrayList<>();

        AddConnectionMutationOperation addConnection = new AddConnectionMutationOperation(1);
        AddNeuronMutationOperation addNeuron = new AddNeuronMutationOperation(0.2);
        WeightMutationOperation weightMutation = new WeightMutationOperation(0.5);

        ops.add(addNeuron);
        ops.add(addConnection);
        ops.add(weightMutation);

        params.setMutationOperators(ops);

//        String RUN_DIR = "run-" + System.currentTimeMillis();
//        params.setPersistence(new DirectoryOutputPersistence(
//                PERSISTANCE_DIR + File.separator + RUN_DIR,
//                new XStreamSerializationDelegate(true))
//        );
        return params;
    }

    private static final int SCREEN_HEIGHT = 400;
    private static final int SCREEN_WIDTH = 700;
    private static final int CANVAS_WIDTH = 700;

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
        NeatParameters params = makeNeatParameters();

        // instantiate input and output neurons
        List<NeuronGene> inputNeurons = new ArrayList<>();
        List<NeuronGene> outputNeurons = new ArrayList<>();

        for (int input = 0; input < NUM_INPUT_NEURONS; input++) {
            inputNeurons.add(new NeuronGene(NeuronType.INPUT, params));
        }

        for (int output = 0; output < NUM_OUTPUT_NEURONS; output++) {
            outputNeurons.add(new NeuronGene(NeuronType.OUTPUT, params));
        }

        Evolver evolver = Evolver.createNew(params, inputNeurons, outputNeurons);

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                 try {
                    Organism best = evolver.evolve();
                    System.out.println("GAME!");
                    System.out.println("This game's winner: Organism " + best.getInnovationId());
                } catch (PersistenceException e) {
                    System.err.println("Persistance error!");
                    e.printStackTrace();
                }
                return null;
            }
        };

        new Thread(task).start();

        new AnimationTimer() {
            private double lastFitness;
            @Override
            public void handle(long now) {
                // redraw if new fitness

//                double fitness = evolver().getFitnessValue();
//                if (fitness > lastFitness) {
                    //System.out.println("fitness increased!");
                    gc.clearRect(0,0, SCREEN_WIDTH, SCREEN_HEIGHT);
//                    drawChamp(canvas, evolver);
            }
//                lastFitness = fitness;
        };//.start();
    }
}
