package will.pso;

import will.pso.encog.EncogMarioProblem;
import will.pso.io.PSOIO;
import will.pso.io.PSOIONeuroph;
import will.pso.neuroph.NeurophMarioProblem;
import will.pso.neuroph.NeurophSwarm;

import javax.swing.*;
import java.io.*;
import java.util.Arrays;

/**
 * Created by Will on 18/05/2016.
 * Runs PSO with a specially created MarioProblem.
 * Copied main method from PSO main class as it has many magic numbers which seem important
 */
public class PSORunner {

    // the highly sought-after golden PSO parameter values
    private static final int numParticles = 10;
    private static final int numIterations = 3000;
    public static final double c1 = 1.49618;
    public static final double c2 = 1.49618;
    public static final double inertia = 0.7298;
    private static final int neighbours = 4;
    private static final String SWARM_DIR = "swarm";

    // generation to start on (only differs when continuing PSO runs)
    private static int startingIter = 0;
    private static final File swarmDir = new File("swarm");

    public static void main(String[] args) {

        String prevSwarmFilename = null;
        String chosenFilename = null;

        // parse arguments
        if (args.length == 1) {
            if (args[0].equals("-l")) {
                // arg is "continue last run"
                File newest = Arrays.stream(swarmDir.listFiles())
                        .filter(f -> {
                            // true if file has valid particle file naming convention
                            String[] parts = f.getName().split("-");
                            boolean firstPart = parts[0].equals("swarm");
                            try {
                                Long.parseLong(parts[1]);
                            } catch (NumberFormatException e) {
                                return false;
                            }
                            return firstPart;
                        })
                        .max((f1, f2) -> {
                            long timestamp1 = Long.parseLong(f1.getName().split("-")[1]);
                            long timestamp2 = Long.parseLong(f2.getName().split("-")[1]);
                            return Long.compare(timestamp1, timestamp2);
                        }).get();

                prevSwarmFilename = newest.getAbsolutePath();
                System.out.println("Continuing from latest run (" + prevSwarmFilename + ")");
            } else if (args[0].equals("-f")) {
                // continue from a previous instance of PSO from a file
                JFileChooser chooser = new JFileChooser();
                chooser.setSelectedFile(swarmDir);
                int response = chooser.showDialog(null, "Pick PSO file");

                if (response == JFileChooser.APPROVE_OPTION) {
                    prevSwarmFilename = chooser.getSelectedFile().getName();
                    System.out.println("Continuing from run in " + prevSwarmFilename);
                } else {
                    System.err.println("Didn't select a file. Program closing");
                    System.exit(0);
                }
            } else {
                // loading specific run
                prevSwarmFilename = args[0];
            }

        } else if (args.length > 1) {
            new IllegalArgumentException("Can't have more than one arg");
        } else {
            String filenameInput = JOptionPane.showInputDialog("name the file?");
            if (filenameInput == null) {
                chosenFilename = SWARM_DIR + File.separator + filenameInput;
            }
        }

        // initialise swarm
        WillSwarm swarm = null;
        double gBestFitness = -1;

        if (prevSwarmFilename == null) {
            // start new PSO run
            swarm = new WillSwarm(
                    new EncogMarioProblem(),
                    numParticles,
                    c1,
                    c2,
                    inertia
            );

            gBestFitness = swarm.getProblem().getWorstFitness();

        } else {
            // previous run should be continued
            // read swarm from particle
            swarm = PSOIO.parseSwarm(new EncogMarioProblem(), prevSwarmFilename);
            // todo: replace ugly separate parsing methods
//            gBestFitness = PSOIONeuroph.parseBestFitness(swarmFilename);
            gBestFitness = swarm.getProblem().getWorstFitness();
            startingIter = PSOIO.parseStartingIter(prevSwarmFilename);
        }

        // if filename has been chosen, use that or if previous filename has been
        // specified, use that, otherwise use default filename
        if (chosenFilename == null) {
            chosenFilename = prevSwarmFilename == null ?
                    SWARM_DIR + File.separator + "swarm-" + System.currentTimeMillis() :
                    prevSwarmFilename
            ;
        }

        swarm.setTopology(new WillRingTopology(neighbours));  //   Generation  2999          3.9968028886505635E-15

        for (int iter = startingIter; iter < numIterations; ++iter) {
            System.out.println("--------------------");
            System.out.println("Iterating over swarm (" + iter + ")");
            System.out.println("--------------------");

            swarm.iterate();

            System.out.println();
            System.out.println("-------------------------------------");
            System.out.println("PSO Iteration " + iter);
            System.out.println("-------------------------------------");

            // get best fitness in every iterate for different topology except star
            WillParticle gBestParticle = null;

            for (int p = 0; p < swarm.numberOfParticles(); ++p) {
                WillParticle particle = swarm.getParticle(p);
                if (swarm.getProblem().isBetter(particle.getPBestFitness(), gBestFitness)) {
                    gBestParticle = swarm.getParticle(p);
                    gBestFitness = gBestParticle.getPBestFitness();
                    System.out.println("particle " + p + " beat global best");
                    System.out.println(particle);
                }
            }

            PSOIO.writePSOIterationToFile(chosenFilename, swarm, iter, gBestFitness);
            System.out.println("PSO Iteration written to " + chosenFilename);
            System.out.println("Global best fitness: " + gBestFitness);
        }
    }
}
