package ch.idsia.neat.pso;/*
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

    public WillSwarm(MarioProblem problem, int numParticles, double c1, double c2, double inertia) {
        this.problem = problem;
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

        // update fitness
        for (WillParticle particle : particles) {

            // evaluate fitness
            double newFitness = getProblem().fitness(particle.getFeatures());
            particle.setFitness(newFitness);

            int index = particles.indexOf(particle);

            System.out.print("ID(" + index + ") fitness: " + newFitness + " == ");
            for (Feature f : particle.getFeatures()) {
                System.out.print(f.getValue() + ", ");
            }

            //Check if new fitness is better than personal best...
            if (getProblem().isBetter(newFitness, particle.getPBestFitness())) {
                particle.setPBestFitness(newFitness);
                for (int j = 0; j < particle.getSize(); ++j) {
                    particle.setPBestPosition(j, particle.getFeatures(j));
                }
                System.out.println("PFit+");
                System.out.println();
            } else {
                System.out.println();
                System.out.println("PFit<=");
            }

            // personal best
            System.out.print("ID: " + index + ", Pbest: " + particle.getPBestFitness() + " ==");
            for (int j = 0; j < particle.getSize(); j++) {
                System.out.print(" " + particle.getPBestFeatures(j));
            }
            // global best
            System.out.println();
            System.out.print("ID: " + index + ", Gbest: " + particle.getNeighborhoodFitness() + " ==");
            for (int j = 0; j < particle.getSize(); j++) {
                System.out.print(" " + particle.getNeighborhoodPosition(j));
            }
            System.out.println("\n");
        }

        getTopology().share(this);

        for (WillParticle p : particles) {
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
}

