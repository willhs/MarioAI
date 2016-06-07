package will.pso;

import will.pso.io.PSOIO;

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

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

    // generation to start on (only differs when continuing PSO runs)
    private static int startingGen = 0;

    public static void main(String[] args) {

        String particlesFilename = null;

        // parse arguments
        if (args.length == 1) {
            if (args[0].equals("-l")) {
                // arg is "continue last run"
                File particlesDir = new File("particle");
                File newest = Arrays.stream(particlesDir.listFiles())
                        .filter(f -> {
                            // true if file has valid particle file naming convention
                            String[] parts = f.getName().split("-");
                            boolean firstPart = parts[0].equals("particle");
                            try {
                                Long.parseLong(parts[1]);
                            } catch (NumberFormatException e) {
                                return false;
                            }
                            return true;
                        })
                        .max((f1, f2) -> {
                            long timestamp1 = Long.parseLong(f1.getName().split("-")[1]);
                            long timestamp2 = Long.parseLong(f2.getName().split("-")[1]);
                            return Long.compare(timestamp1, timestamp2);
                        }).get();

                particlesFilename = newest.getAbsolutePath();
                System.out.println("Loading from latest run: ");
            } else {
                // loading specific run
                particlesFilename = args[0];
                Scanner scan = null;
                try {
                    scan = new Scanner(new File(particlesFilename));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    new RuntimeException("Can't find particle file");
                }
                startingGen = scan.nextInt();
                System.out.println("Loading from specific run: ");
            }

        } else if (args.length > 1) {
            new IllegalArgumentException("Can't have more than one arg");
        }

        // initialise swarm
        WillSwarm swarm = null;
        if (particlesFilename == null) {
            // start new PSO run
            swarm = new WillSwarm(
                    new MarioProblem(),
                    numParticles,
                    c1,
                    c2,
                    inertia
            );
        } else {
            // previous run should be continued
            // read swarm from particle
            swarm = PSOIO.parseSwarm(new MarioProblem(), particlesFilename);
        }

        String filename = particlesFilename == null ?
                "particle" + File.separator + "particle-" + System.currentTimeMillis():
                particlesFilename
                ;

        swarm.setTopology(new WillRingTopology(neighbours));  //   Generation  2999          3.9968028886505635E-15

        for (int gen = startingGen; gen < numIterations; ++gen) {
            System.out.println("--------------------");
            System.out.println("Iterating over swarm (" + gen + ")");
            System.out.println("--------------------");

            swarm.iterate();

            System.out.println();
            System.out.println("-------------------------------------");
            System.out.println("PSO Generation  " + gen);
            System.out.println("-------------------------------------");

            // get best fitness in every iterate for different topology except star
            WillParticle gBestParticle = null;
            double gBestFitness = swarm.getProblem().getWorstFitness();

            for (int p = 0; p < swarm.numberOfParticles(); ++p) {
                WillParticle particle = swarm.getParticle(p);
                if (swarm.getProblem().isBetter(particle.getPBestFitness(), gBestFitness)) {
                    gBestParticle = swarm.getParticle(p);
                    gBestFitness = gBestParticle.getPBestFitness();
                    System.out.println("particle " + p + " beat global best");
                    System.out.println(particle);
                }
            }

            PSOIO.writePSOIterationToFile(filename, swarm, gen);
            System.out.println("Iteration written to " + filename);
            System.out.println("Global best fitness: " + gBestFitness);
        }
    }
}
