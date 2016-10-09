package will.neat.encog.experiment;

import ch.idsia.benchmark.mario.engine.generalization.Tile;
import ch.idsia.benchmark.mario.options.FastOpts;
import org.encog.ml.ea.train.basic.TrainEA;
import org.encog.neural.neat.NEATCODEC;
import org.encog.neural.neat.NEATLink;
import org.encog.neural.neat.NEATNetwork;
import will.neat.AbstractMarioFitnessFunction;
import will.neat.encog.HyperNEATMarioEvolver;
import will.neat.encog.NEATMarioEvolver;
import will.neat.encog.NEATMarioEvolverFS;
import will.neat.params.HyperNEATParameters;
import will.neat.params.HyperNEATParametersPSO;
import will.neat.params.NEATParameters;
import will.rf.action.SharedHoldStrat;
import will.rf.action.StandardActionStrat;
import will.rf.action.StandardHoldStrat;
import will.rf.environment.EnvEnemyGrid;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Will on 8/10/2016.
 */
public class MarioAIExperiment {

    private int generations = 1000;

    private NEATParameters neatParams = new NEATParameters();
    private HyperNEATParameters hyperParams = new HyperNEATParametersPSO();

    private final String DEFAULT_SIM_OPTIONS = AbstractMarioFitnessFunction.DEFAULT_SIM_OPTIONS;
    private final String DEFAULT_LEVEL = AbstractMarioFitnessFunction.LEVEL;

    private String[] levels = {
            DEFAULT_SIM_OPTIONS.replace(DEFAULT_LEVEL, FastOpts.LEVEL_05_GAPS),
            DEFAULT_SIM_OPTIONS.replace(DEFAULT_LEVEL, FastOpts.LEVEL_06_GOOMBA),
            DEFAULT_SIM_OPTIONS.replace(DEFAULT_LEVEL, FastOpts.LEVEL_07_SPIKY),
            DEFAULT_SIM_OPTIONS.replace(DEFAULT_LEVEL, FastOpts.LEVEL_08_FLY_SPIKY)
    };
    private String outputDir = ".";

    public static void main(String[] args) throws IOException{
//        MarioAIExperiment ex = new MarioAIExperiment();
//        ex.run();

        Tile[][] tiles = {
                {Tile.NOTHING, Tile.SOMETHING, Tile.NOTHING},
                {Tile.NOTHING, Tile.NOTHING, Tile.NOTHING},
                {Tile.NOTHING, Tile.NOTHING, Tile.SOMETHING}
        };

        double[] binaryArray = envGridToBinaryArray(tiles);
        System.out.println(Arrays.toString(binaryArray));
    }

    private static double[] envGridToBinaryArray(Tile[][] tiles) {
        return Arrays.stream(tiles)
                .flatMap(tileRow -> {
                    return Arrays.stream(tileRow)
                            .map(tile -> {
                                // represent unique tiles
                                return tile == Tile.NOTHING ? 0 : 1;
                            });
                })
                .mapToDouble(i->i)
                .toArray();
    }

    public MarioAIExperiment() {
    }

    public void run() throws IOException{
        // for each of the experiments, run all level benchmarks
        // 1. run each type of action strat
        //      a. every frame
        //      b. standard hold
        //      c. timed action toggle
        // 2. phased search
        // 3. hyperneat
        //      a. normal
        //      b. with feature selection from phased search neat
        int numFiles = new File(outputDir).listFiles().length;

        String outputFilename = "neat-experiment-" + numFiles + ".csv";
        // initialise file writer
        Path output = Paths.get(outputFilename);
        // write first csv line
        try {
            Files.write(output, "algorithm,generation,fitness,connections,nodes,species".getBytes());
        } catch (IOException e) { e.printStackTrace(); }

        NEATParameters neatPhasedParams = new NEATParameters();
        neatPhasedParams.PHASED_SEARCH = true;

        NEATParameters neatShareActionParams = new NEATParameters();
        neatPhasedParams.NUM_INPUTS = 5;

        NEATMarioEvolver neatStandard = new NEATMarioEvolver(neatParams,
                () -> new StandardActionStrat(), output, "neat-standard");
        NEATMarioEvolver neatStandardHold = new NEATMarioEvolver(neatParams,
                () -> new StandardHoldStrat(), output, "neat-standard-hold");
        NEATMarioEvolver neatSharedHold = new NEATMarioEvolver(neatShareActionParams,
                () -> new SharedHoldStrat(), output, "neat-shared-hold");
        NEATMarioEvolver neatPhased = new NEATMarioEvolver(neatPhasedParams,
                () -> new StandardHoldStrat(), output, "neat-phased");
        NEATMarioEvolver hyperneat = new HyperNEATMarioEvolver(hyperParams,
                () -> new StandardHoldStrat(), output, "hyperneat");
        NEATMarioEvolverFS hyperneatFS = new NEATMarioEvolverFS(hyperParams,
                () -> new StandardHoldStrat(), output, "hyperneat-fs");

        // run all experiments without dependencies
        testOnLevels(neatStandard);
        testOnLevels(neatStandardHold);
        testOnLevels(neatSharedHold);
        TrainEA[] phasedResults = testOnLevels(neatPhased);
        testOnLevels(hyperneat);

        // use features from phased neat for hyperneat
        for (int i = 0; i < phasedResults.length; i++) {
            TrainEA result = phasedResults[i];
            String level = levels[i];
            NEATCODEC codec = new NEATCODEC();
            NEATNetwork network = (NEATNetwork) codec.decode(result.getBestGenome());
            Point[] features = extractFeatures(network);

            hyperneatFS.setSimOptions(level);
            hyperneatFS.setInputs(features);
            hyperneatFS.run();
        }
    }

    private TrainEA[] testOnLevels(NEATMarioEvolver evolver) throws IOException {
        TrainEA[] results = new TrainEA[levels.length];
        for (int i = 0; i < levels.length; i++) {
            String level = levels[i];

            evolver.setSimOptions(level);
            results[i] = evolver.run();
        }
        return results;
    }

    private Point[] extractFeatures(NEATNetwork network) {
        NEATLink[] links = network.getLinks();
        return Arrays.stream(links)
                .filter(l -> l.getFromNeuron() < network.getInputCount())
                .map(l -> {
                    int neuronNum = l.getFromNeuron();
                    int x = neuronNum % 13;
                    int y = neuronNum / 13;

                    return new Point(x, y);
                })
                .toArray(Point[]::new);
    }
}
