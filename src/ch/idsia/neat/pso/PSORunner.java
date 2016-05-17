package ch.idsia.neat.pso;

import pso.BasicVelocityClamp;
import pso.Particle;
import pso.Swarm;

/**
 * Created by Will on 18/05/2016.
 * Runs PSO with a specially created MarioProblem.
 * Copied main method from PSO main class as it has many magic numbers which seem important
 */
public class PSORunner {

    public static void main(String[] args) {

        int number_of_particles = 10;
        int number_of_iterations = 3000;
        double c1 = 1.49618, c2 = 1.49618;

        Swarm s = new Swarm();

        s.setProblem(new MarioProblem());

        s.setVelocityClamp(new BasicVelocityClamp());

        for (int i = 0; i < number_of_particles; ++i) {
            Particle p = new Particle();
            p.setSize(10);
            p.setC1(c1);
            p.setC2(c2);

            p.setInertia(0.7298);
            s.addParticle(p);
        }

        s.initialize();

        for (int i = 0; i < number_of_iterations; ++i) {
            s.iterate();
            System.out.println("");
            System.out.println("-------------------------------------");
            System.out.println("Generation  " + i);
//            System.out.println("BEST=" + s.getParticle(11).getPersonalFitness());
//            System.out.println("Average=" + Math.Average(s));

            /**
             *  // get bestfitnes in every iterate for different topology except star
             */
            Particle globalbest_particle = null;
            double globalbest_fitness = s.getProblem().getWorstFitness();

            for (int j = 0; j < s.numberOfParticles(); ++j) {
                if (s.getProblem().isBetter(s.getParticle(j).getPersonalFitness(), globalbest_fitness)) {
                    globalbest_particle = s.getParticle(j);
                    globalbest_fitness = globalbest_particle.getPersonalFitness();
                }
            }

            System.out.println(globalbest_fitness);
        }
    }
}
