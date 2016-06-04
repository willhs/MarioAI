package will.pso;/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author xuebing
 */
public class WillSwarm {

    private MarioProblem problem;
    private WillTopology topology;
    private List<WillParticle> particles = new ArrayList<>();
    private Random _random = new Random();

    // useful for printing
    private final double inertia;
    private final double c1, c2;

    public WillSwarm(MarioProblem problem, int numParticles, double c1, double c2, double inertia) {
        this.problem = problem;
        this.inertia = inertia;
        this.c1 = c1;
        this.c2 = c2;
        // generate particles
        for (int p = 0; p < numParticles; p++) {

            WillParticle particle = new WillParticle(problem.getFeatures());

            particle.setC1(c1);
            particle.setC2(c2);
            particle.setInertia(inertia);

            particle.setPBestFitness(getProblem().getWorstFitness());
            particle.setNeighborhoodFitness(getProblem().getWorstFitness());

            particles.add(particle);
        }
    }

    public MarioProblem getProblem() {
        return problem;
    }

    public WillParticle getParticle(int index) {
        return particles.get(index);
    }

    public int numberOfParticles() {
        return particles.size();
    }

    public Random getRandom() {
        return _random;
    }

    public void iterate() {

        // update fitness for particles
        for (WillParticle particle : particles) {

            int index = particles.indexOf(particle);

            // evaluate fitness
            System.out.println("Getting fitness for particle: " + index + "...");
            double newFitness = getProblem().fitness(particle.getFeatures());
            particle.setFitness(newFitness);
            System.out.println("Fitness gotten");

            System.out.println("Particle " + index + " fitness: " + newFitness);
            for (Feature f : particle.getFeatures()) {
//                System.out.print(f.getValue() + ", ");
            }

            //Check if new fitness is better than personal best...
            if (getProblem().isBetter(newFitness, particle.getPBestFitness())) {
                particle.setPBestFitness(newFitness);
                for (int j = 0; j < particle.getSize(); ++j) {
                    particle.setPBestPosition(j, particle.getFeatures(j));
                }
//                System.out.println("PFit+");
            } else {
//                System.out.println();
//                System.out.println("PFit<=");
            }

//            System.out.println("\n");
        }

        getTopology().share(this);

        for (WillParticle p : particles) {
            int index = particles.indexOf(p);
//            System.out.println("====== Updating vel and position for particle: " + index);
//            System.out.println(p);
            p.updateVelocity();
            p.updatePosition();
        }
    }

    public WillTopology getTopology() {
        return topology;
    }

    public void setTopology(WillTopology topology) {
        this.topology = topology;
    }

    public List<WillParticle> getParticles() {
        return particles;
    }

    public double getInertia() {
        return inertia;
    }
}

