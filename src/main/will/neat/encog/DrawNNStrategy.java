package will.neat.encog;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.train.basic.TrainEA;
import org.encog.ml.train.MLTrain;
import org.encog.ml.train.strategy.Strategy;
import org.encog.neural.hyperneat.HyperNEATCODEC;
import org.encog.neural.hyperneat.substrate.Substrate;
import org.encog.neural.hyperneat.substrate.SubstrateNode;
import org.encog.neural.neat.NEATNetwork;
import org.encog.neural.neat.NEATPopulation;
import will.util.Algorithms;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Will on 25/08/2016.
 */
public class DrawNNStrategy implements Strategy {

    private final double MIN_LINK_WIDTH = 0.4;
    private final double MAX_LINK_WIDTH = 2;
    private final Paint INPUT_NEURON_COLOUR = Color.AQUA;
    private final Paint HIDDEN_NEURON_COLOUR = Color.BROWN;
    private final Paint OUTPUT_NEURON_COLOUR = Color.RED;
    private final Paint BIAS_NEURON_COLOUR = Color.YELLOW;

    private final double PADDING = 20;
    private final double WIDTH, HEIGHT;

//    private final Paint CONNECTION_LOW

    private TrainEA train;
    private Canvas canvas;

    private final int NEURON_SIZE = 5;

    public DrawNNStrategy(Canvas canvas) {
        this.canvas = canvas;
        this.WIDTH = canvas.getWidth() * 0.6;
        this.HEIGHT = canvas.getHeight() * 0.6;
    }

    @Override
    public void init(MLTrain train) {
        this.train = (TrainEA) train;
    }

    @Override
    public void preIteration() {
        draw();
    }

    private void draw() {
        try {
            System.out.println("drawing");
            if (train.getIteration() < 1) { return; }

            GraphicsContext g = canvas.getGraphicsContext2D();
            g.clearRect(0,0,canvas.getWidth(), canvas.getHeight());

            NEATPopulation pop = (NEATPopulation) train.getPopulation();
            Substrate substrate = pop.getSubstrate();
            Genome champ = pop.getBestGenome();
            NEATNetwork nn = (NEATNetwork) new HyperNEATCODEC().decode(champ);

            List<SubstrateNode> allNodes = Stream.concat(
                    Stream.concat(
                            substrate.getInputNodes().stream(),
                            substrate.getHiddenNodes().stream()
                    ),
                    substrate.getOutputNodes().stream()
            ).collect(Collectors.toList());

            // draw links
            Arrays.stream(nn.getLinks()).forEach(link -> {
                if (link.getFromNeuron() == 0) return;
//                System.out.println(link.getFromNeuron() + " " + link.getToNeuron());
                SubstrateNode from = allNodes.stream()
                        .filter(n -> n.getId() == link.getFromNeuron())
                        .findFirst()
                        .get();

                SubstrateNode to = allNodes.stream()
                        .filter(n -> n.getId() == link.getToNeuron())
                        .findFirst()
                        .get();

                double NNWeightRange = 3.0;
                double weightProp = link.getWeight() / NNWeightRange;
                double weightPropAbs = Math.abs(weightProp);
                double lineWidth = MIN_LINK_WIDTH + (weightPropAbs * MAX_LINK_WIDTH-MIN_LINK_WIDTH);


                double greenAmount = Algorithms.clamp(weightProp, 0, 1);
                double redAmount = Math.abs(Algorithms.clamp(weightProp, -1, 0));
                g.setStroke(Color.color(redAmount, greenAmount, 0, weightPropAbs));

                double[] transformedFrom = transform(from.getLocation());
                double[] transformedTo = transform(to.getLocation());
                g.setLineWidth(1);
                g.strokeLine(transformedFrom[0], transformedFrom[1],
                        transformedTo[0], transformedTo[1]);
            });

            // draw nodes
            substrate.getInputNodes().forEach(n -> {
                double[] loc = n.getLocation();
                double[] locTrans = transform(loc);
                g.setFill(INPUT_NEURON_COLOUR);
                g.fillOval(locTrans[0], locTrans[1], NEURON_SIZE, NEURON_SIZE);
            });

            substrate.getHiddenNodes().forEach(n -> {
                double[] loc = n.getLocation();
                double[] locTrans = transform(loc);
                g.setFill(HIDDEN_NEURON_COLOUR);
                g.fillOval(locTrans[0], locTrans[1], NEURON_SIZE, NEURON_SIZE);
            });

            substrate.getOutputNodes().forEach(n -> {
                double[] loc = n.getLocation();
                double[] locTrans = transform(loc);
                g.setFill(OUTPUT_NEURON_COLOUR);
                g.fillOval(locTrans[0], locTrans[1], NEURON_SIZE, NEURON_SIZE);
            });

            // bias node
            double[] originTransformed = transform(new double[]{0,0,0});
            g.setFill(BIAS_NEURON_COLOUR);
            g.fillOval(originTransformed[0], originTransformed[1], NEURON_SIZE, NEURON_SIZE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private double[] transform(double[] point) {
        Vector3D vec = new Vector3D(point[0], point[1], point[2]);
        Transform isometric = Transform.identity()
                .compose(Transform.newXRotation(Math.PI/4))
                .compose(Transform.newYRotation(Math.PI/4))
                .compose(Transform.newTranslation(canvas.getWidth()/2, canvas.getHeight()/2, 0))
                .compose(Transform.newScale(WIDTH/2, HEIGHT/2, WIDTH/4))
                ;

        Vector3D result = isometric.multiply(vec);
        return new double[]{ result.x, result.y, result.z };
    }

    @Override
    public void postIteration() { }
}

