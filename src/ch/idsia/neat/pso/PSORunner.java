package ch.idsia.neat.pso;

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

        swarm.setTopology(new WillRingTopology(4));  //   Generation  2999          3.9968028886505635E-15

        for (int i = 0; i < number_of_iterations; ++i) {
            swarm.iterate();
            System.out.println();
            System.out.println("-------------------------------------");
            System.out.println("PSO Generation  " + i);


//            System.out.println("BEST=" + s.getParticle(11).getPBestFitness());
//            System.out.println("Average=" + Math.Average(s));

            // get best fitness in every iterate for different topology except star
            WillParticle globalbest_particle = null;
            double globalbest_fitness = swarm.getProblem().getWorstFitness();

            for (int j = 0; j < swarm.numberOfParticles(); ++j) {
                WillParticle particle = swarm.getParticle(j);
                if (swarm.getProblem().isBetter(particle.getPBestFitness(), globalbest_fitness)) {
                    System.out.println("beaten");
                    globalbest_particle = swarm.getParticle(j);
                    globalbest_fitness = globalbest_particle.getPBestFitness();
                }
                System.out.println("not beaten");
                System.out.println("velocities: " + Arrays.toString(particle.getFeatures().stream().map(f -> f.getVel()).toArray()));
            }

            System.out.println(": " + swarm.getInertia());

            System.out.println("Global best fitness: " + globalbest_fitness);
        }
    }
}
