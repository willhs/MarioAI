package ch.idsia.neat.pso;

import java.io.*;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Arrays;

/**
 * Created by Will on 18/05/2016.
 * Runs PSO with a specially created MarioProblem.
 * Copied main method from PSO main class as it has many magic numbers which seem important
 */
public class PSORunner {

    public static void main(String[] args) {

        // the highly sought-after golden PSO parameter values
        int number_of_particles = 10;
        int number_of_iterations = 3000;
        double c1 = 1.49618, c2 = 1.49618;
        double inertia = 0.7298;

        WillSwarm swarm = new WillSwarm(
                new MarioProblem(),
                number_of_particles,
                c1,
                c2,
                inertia
        );

        File file = new File("db" + File.separator + "particles" + File.separator + "particle-" + System.currentTimeMillis());

        swarm.setTopology(new WillRingTopology(4));  //   Generation  2999          3.9968028886505635E-15

        for (int gen = 0; gen < number_of_iterations; ++gen) {
            System.out.println("--------------------");
            System.out.println("Iterating over swarm (" + gen + ")");
            System.out.println("--------------------");
            swarm.iterate();
            System.out.println();
            System.out.println("-------------------------------------");
            System.out.println("PSO Generation  " + gen);
            System.out.println("-------------------------------------");

            // get best fitness in every iterate for different topology except star
            WillParticle globalbest_particle = null;
            double globalbest_fitness = swarm.getProblem().getWorstFitness();

            for (int p = 0; p < swarm.numberOfParticles(); ++p) {
                WillParticle particle = swarm.getParticle(p);
                if (swarm.getProblem().isBetter(particle.getPBestFitness(), globalbest_fitness)) {
                    globalbest_particle = swarm.getParticle(p);
                    globalbest_fitness = globalbest_particle.getPBestFitness();
                    System.out.println("particle " + p + " beat global best");
                    System.out.println(particle);
                    writeToFile(file, particle, gen);
                }
//                System.out.println("velocities: " + Arrays.toString(particle.getFeatures().stream().map(f -> f.getVel()).toArray()));
            }

            System.out.println("Global best fitness: " + globalbest_fitness);
        }

    }

    private static void writeToFile(File file, WillParticle particle, int generation) {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)))){
            particle.getPBestFeatures().forEach(p ->  {
                writer.println("Best particles from gen " + generation + ":");
                writer.println(particle.getFeatures());
                writer.println(particle.getFitness());
                writer.println();
                writer.close();
            } );
            System.out.println("Written to file");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
