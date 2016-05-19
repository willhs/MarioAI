package ch.idsia.neat.pso;

import pso.Math;

/**
 * Created by Will on 18/05/2016.
 *
 * Based on RingTopology
 */
public class WillRingTopology extends WillTopology {

    private int _neighbors = 2;

    public WillRingTopology() {
    }

    public WillRingTopology(int n) {
        setNeighbors(n);
    }

    public void share(WillSwarm s) {

        for (WillParticle particle : s.getParticles()) {

            int index = s.getParticles().indexOf(particle);

//            System.out.println("getNeighbors()" + getNeighbors());
            System.out.println("");
//            if (i == 7) {
            System.out.print("i  " + index + "  NNN  ");

            WillParticle best_neighbor = null;
            double best_fitness = s.getProblem().getWorstFitness();

            for (int j = -getNeighbors() / 2; j <= getNeighbors() / 2; ++j) {

                System.out.print("  " + Math.ModEuclidean(index + j, s.numberOfParticles()));

                if (s.getProblem().isBetter(s.getParticle(Math.ModEuclidean(index + j, s.numberOfParticles())).getPBestFitness(), best_fitness)) {
                    best_neighbor = s.getParticle(Math.ModEuclidean(index + j, s.numberOfParticles()));
                    best_fitness = best_neighbor.getPBestFitness();
                }
            }
            System.out.println();
            System.out.println("best_neighbor: " + best_neighbor.getFeatures());
            System.out.println("best_fitness:  " + best_fitness);

            particle.setNeighborhoodFitness(best_fitness);

            for (int n = 0; n < particle.getSize(); ++n) {
                particle.setNeighborhoodPosition(n, best_neighbor.getPBestFeatures(n));
            }

//            for (int n = 0; n < p_i.getSize(); ++n) {
////                p_i.setNeighborhoodPosition(n, best_neighbor.getNeighborhoodPosition(n));
//                System.out.print(best_neighbor.getPBestFeatures(n));
//                System.out.println("");
//
//            }
        }
    }

    public int getNeighbors() {
        return _neighbors;
    }

    public void setNeighbors(int neighbors) {
        this._neighbors = neighbors;
    }
}
