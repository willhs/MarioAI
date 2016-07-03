package will.pso;

import will.pso.anji.ANJIMarioProblem;
import will.pso.anji.ANJIRingTopology;
import will.pso.anji.ANJIWillSwarm;
import will.pso.anji.ANJIParticle;
import will.pso.io.PSOIO;
import will.pso.neuroph.MarioProblem;
import will.pso.neuroph.WillParticle;
import will.pso.neuroph.WillRingTopology;
import will.pso.neuroph.WillSwarm;

import javax.swing.*;
import java.io.*;
import java.util.Arrays;
import java.util.logging.*;

/**
 * Created by Will on 18/05/2016.
 * Runs PSO with a specially created ANJIMarioProblem.
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

    // generation to start on (only differs when continuing PSO runs)
    private static int startingIter = 0;
    private static final File swarmDir = new File("swarm");

    public static void main(String[] args) {

        String swarmFilename = null;

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

                swarmFilename = newest.getAbsolutePath();
                System.out.println("Continuing from latest run (" + swarmFilename + ")");
            } else if (args[0].equals("-f")) {
                // continue from a previous instance of PSO from a file
                JFileChooser chooser = new JFileChooser();
                chooser.setSelectedFile(swarmDir);
                int response = chooser.showDialog(null, "Pick PSO file");

                if (response == JFileChooser.APPROVE_OPTION) {
                    swarmFilename = chooser.getSelectedFile().getName();
                    System.out.println("Continuing from run in " + swarmFilename);
                } else {
                    System.err.println("Didn't select a file. Program closing");
                    System.exit(0);
                }
            } else {
                // loading specific run
                swarmFilename = args[0];
            }

        } else if (args.length > 1) {
            new IllegalArgumentException("Can't have more than one arg");
        }

        // initialise swarm
        WillSwarm swarm = null;
        double gBestFitness = -1;

        if (swarmFilename == null) {
            // start new PSO run
            swarm = new WillSwarm(
                    new MarioProblem(),
                    numParticles,
                    c1,
                    c2,
                    inertia
            );

            gBestFitness = swarm.getProblem().getWorstFitness();

        } else {
            // previous run should be continued
            // read swarm from particle
            swarm = PSOIO.parseSwarm(new MarioProblem(), swarmFilename);
            // todo: replace ugly separate parsing methods
//            gBestFitness = PSOIO.parseBestFitness(swarmFilename);
            gBestFitness = swarm.getProblem().getWorstFitness();
            startingIter = PSOIO.parseStartingIter(swarmFilename);
        }

        String filename = swarmFilename == null ?
                "swarm" + File.separator + "swarm-" + System.currentTimeMillis() :
                swarmFilename
                ;

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

            PSOIO.writePSOIterationToFile(filename, swarm, iter, gBestFitness);
            System.out.println("PSO Iteration written to " + filename);
            System.out.println("Global best fitness: " + gBestFitness);
        }
    }
}
