package will.neat.encog.experiment;

import ch.idsia.benchmark.mario.engine.generalization.Tile;
import ch.idsia.benchmark.mario.options.FastOpts;
import org.encog.ml.ea.train.basic.TrainEA;
import org.encog.neural.neat.NEATLink;
import org.encog.neural.neat.NEATNetwork;
import will.neat.AbstractMarioFitnessFunction;
import will.neat.params.HyperNEATParameters;
import will.neat.params.HyperNEATParametersPSO;
import will.neat.params.NEATParameters;
import will.rf.action.SharedHoldStrat;
import will.rf.action.StandardActionStrat;
import will.rf.action.StandardHoldStrat;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import static will.neat.AbstractMarioFitnessFunction.DIFFICULTY;

/**
 * Created by Will on 8/10/2016.
 */
public class MarioAIExperiment {

    private NEATParameters neatParams = new NEATParameters();
    private HyperNEATParameters hyperParams = new HyperNEATParametersPSO();

    private final String DEFAULT_SIM_OPTIONS = AbstractMarioFitnessFunction.DEFAULT_SIM_OPTIONS;
    private final String DEFAULT_LEVEL = AbstractMarioFitnessFunction.LEVEL;

    private String[] levels = {
            DEFAULT_SIM_OPTIONS.replace(DEFAULT_LEVEL, FastOpts.LEVEL_05_GAPS)
                                .replace(DIFFICULTY, FastOpts.L_DIFFICULTY(1)),
            DEFAULT_SIM_OPTIONS.replace(DEFAULT_LEVEL, FastOpts.LEVEL_06_GOOMBA),
            DEFAULT_SIM_OPTIONS.replace(DEFAULT_LEVEL, FastOpts.LEVEL_07_SPIKY),
            DEFAULT_SIM_OPTIONS.replace(DEFAULT_LEVEL, FastOpts.LEVEL_08_FLY_SPIKY)
    };
    private String outputDirName = "neat-output/";

    private boolean writeFile = true;

    private static final String NEAT_EACH_FRAME = "neat-each-frame";
    private static final String NEAT_STANDARD_HOLD = "neat-standard-hold";
    private static final String NEAT_SHARED_HOLD = "neat-shared-hold";
    private static final String NEAT_PHASED = "neat-phased";
    private static final String HYPERNEAT = "hyperneat";
    private static final String HYPERNEAT_PHASED = "hyperneat-phased";
    private String experimentName;

    public static void main(String[] args) throws IOException{

        String arg = null;
        if (args.length < 1) {
            arg = NEAT_STANDARD_HOLD;
        } else arg = args[0];

        MarioAIExperiment ex = new MarioAIExperiment(arg);
        ex.run();

        // testing
/*        Tile[][] tiles = {
                {Tile.NOTHING, Tile.SOMETHING, Tile.NOTHING},
                {Tile.NOTHING, Tile.NOTHING, Tile.NOTHING},
                {Tile.NOTHING, Tile.NOTHING, Tile.SOMETHING}
        };

        double[] binaryArray = envGridToBinaryArray(tiles);
        System.out.println(Arrays.toString(binaryArray));*/
    }

    public MarioAIExperiment(String name) {
        this.experimentName = name;
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

        // init string output
        StringBuilder sb = new StringBuilder();

        NEATParameters neatPhasedParams = new NEATParameters();
        neatPhasedParams.PHASED_SEARCH = true;

        HyperNEATParameters hyperPhasedParams = new HyperNEATParametersPSO();
        hyperPhasedParams.PHASED_SEARCH = true;

        NEATParameters neatShareActionParams = new NEATParameters();
        neatShareActionParams.NUM_OUTPUTS = 5;

        NEATMarioEvolver evolver = null;

        if (experimentName.equals(NEAT_EACH_FRAME)) {
            evolver = new NEATMarioEvolver(neatParams,
                () -> new StandardActionStrat(), sb, experimentName);
        } else if (experimentName.equals(NEAT_STANDARD_HOLD)) {
            evolver = new NEATMarioEvolver(neatParams,
                () -> new StandardHoldStrat(), sb, experimentName);
        } else if (experimentName.equals(NEAT_SHARED_HOLD)) {
            evolver = new NEATMarioEvolver(neatShareActionParams,
                () -> new SharedHoldStrat(), sb, experimentName);
        } else if (experimentName.equals(NEAT_PHASED)) {
            evolver = new NEATMarioEvolver(neatPhasedParams,
                    () -> new StandardHoldStrat(), sb, experimentName);
        } else if (experimentName.equals(HYPERNEAT)) {
            evolver = new HyperNEATMarioEvolver(hyperParams,
                    () -> new StandardHoldStrat(), sb, experimentName);
        } else if (experimentName.equals(HYPERNEAT_PHASED)) {
            evolver = new HyperNEATMarioEvolver(hyperPhasedParams,
                    () -> new StandardHoldStrat(), sb, experimentName);
        }

//        NEATMarioEvolver neatFrames = = new NEATMarioEvolver(neatParams,
//                () -> new StandardActionStrat(), sb, "neat-frames");
//        NEATMarioEvolver neatStandardHold = new NEATMarioEvolver(neatParams,
//                () -> new StandardHoldStrat(), sb, "neat-standard-hold");
//        NEATMarioEvolver neatSharedHold = new NEATMarioEvolver(neatShareActionParams,
//                () -> new SharedHoldStrat(), sb, "neat-shared-hold");
//        NEATMarioEvolver neatPhased = new NEATMarioEvolver(neatPhasedParams,
//                () -> new StandardHoldStrat(), sb, "neat-phased");
//        NEATMarioEvolver hyperneat = new HyperNEATMarioEvolver(hyperParams,
//                () -> new StandardHoldStrat(), sb, "hyperneat");
//        NEATMarioEvolver hyperneatPhased = new HyperNEATMarioEvolver(hyperPhasedParams,
//                () -> new StandardHoldStrat(), sb, "hyperneat-phased");

        // run all experiments without dependencies
//        testOnLevels(neatStandard);
//        testOnLevels(neatStandardHold);
//        testOnLevels(neatSharedHold);
//        testOnLevels(neatPhased);
//        testOnLevels(hyperneat);
//        testOnLevels(hyperneatPhased);

/*        String level = levels[1];

        NEATMarioEvolver evolver = new NEATMarioEvolver(neatParams,
                () -> new StandardHoldStrat());

        NEATMarioEvolver evolver = new HyperNEATMarioEvolver(hyperParams,
                () -> new StandardHoldStrat(), sb, "std hold");

        evolver.setSimOptions(level);
        evolver.run();*/

        testOnLevels(evolver);

        if (writeFile) {
            // make sure output dir exists
            File outputDir = new File(outputDirName);
            if (!outputDir.exists()) {
                outputDir.getAbsoluteFile().mkdirs();
            }

            int numFiles = outputDir.listFiles().length;

            String outputFilename = outputDirName + experimentName + "-" + numFiles + ".csv";
            Path output = Paths.get(outputFilename);
            // write string output to file
            try {
                Files.write(output,
                        "algorithm,generation,fitness,ave-links,best-links,ave-nodes,best-nodes,species\n".getBytes(),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);

                Files.write(output, sb.toString().getBytes(),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void testOnLevels(NEATMarioEvolver evolver) throws IOException {
        String basicName = evolver.getName();
        TrainEA[] results = new TrainEA[levels.length];
        for (int l = 0; l < levels.length; l++) {
            String level = levels[l];

            evolver.setSimOptions(level);
            evolver.setName(basicName + "-" + l);
            results[l] = evolver.run();
        }
    }

    private void runHyperNEATES() {
/*        // use features from phased neat for hyperneat
        for (int i = 0; i < phasedResults.length; i++) {
            TrainEA result = phasedResults[i];
            String level = levels[i];
            NEATCODEC codec = new NEATCODEC();
            NEATNetwork network = (NEATNetwork) codec.decode(result.getBestGenome());
            Point[] features = extractFeatures(network);

            hyperneatFS.setSimOptions(level);
            hyperneatFS.setInputs(features);
            hyperneatFS.run();
        }*/
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
}
