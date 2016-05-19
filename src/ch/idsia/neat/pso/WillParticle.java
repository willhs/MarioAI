/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.idsia.neat.pso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author xuebing
 */
public class WillParticle {

    private List<Feature> features = new ArrayList<>();
    private double fitness; // current fitness
    private List<Double> pBestFeatures = new ArrayList<>();
    private double pBestFitness; // personal best fitness
    private List<Feature> neighborhood_feature = new ArrayList<>();
    private double neighborhood_fitness;
    private double inertia;
    private double c1,  c2;
    private Random r1 = new Random(),  r2 = new Random();

    public WillParticle(List<Feature> features) {
        this.features = features;

        for (Feature f : features) {
            // add dummy features (just until old code is understood)
            this.pBestFeatures.add(0.0);
            this.neighborhood_feature.add(new Feature(0));
        }
    }

    public int getSize() {
        return features.size();
    }

    public void setPosition(int index, double value) {
        this.features.get(index).setValue(value);
    }

    public double getFeatures(int index) {
        return features.get(index).getValue();
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public void setVelocity(int index, double value) {
        features.get(index).setVel(value);
    }

    public double getVelocity(int index) {
        return features.get(index).getVel();
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public void setPBestPosition(int index, double value) {
        pBestFeatures.set(index, value);
    }

    public double getPBestFeatures(int index) {
        return pBestFeatures.get(index);
    }

    public double getPBestFitness() {
        return pBestFitness;
    }

    public void setPBestFitness(double fitness_best_personal) {
        pBestFitness = fitness_best_personal;
    }

    public void setNeighborhoodPosition(int index, double value) {
        this.neighborhood_feature.get(index).setValue(value);
    }

    public double getNeighborhoodPosition(int index) {
        return neighborhood_feature.get(index).getValue();
    }

    public double getNeighborhoodFitness() {
        return neighborhood_fitness;
    }

    public void setNeighborhoodFitness(double fitness_best_neighbor) {
        this.neighborhood_fitness = fitness_best_neighbor;
    }

    public double getInertia() {
        return inertia;
    }

    public void setInertia(double inertia) {
        this.inertia = inertia;
    }

    public double getC1() {
        return c1;
    }

    public void setC1(double c1) {
        this.c1 = c1;
    }

    public double getC2() {
        return c2;
    }

    public void setC2(double c2) {
        this.c2 = c2;
    }

    public Random getR1() {
        return r1;
    }

    public Random getR2() {
        return r2;
    }
    public void updateVelocity() {
        for (int i = 0; i < getSize(); ++i) {
            double v_i = getInertia() * getVelocity(i);
            double firstMult = getC1() * getR1().nextDouble() * (getPBestFeatures(i) - getFeatures(i));
            double secondMult = getC2() * getR2().nextDouble() * (getNeighborhoodPosition(i) - getFeatures(i));
            v_i += firstMult;
            v_i += secondMult;

            System.out.printf("first mult: %4.2f, second: %4.2f", firstMult, secondMult);

            setVelocity(i, v_i);
        }
    }

    public void updatePosition() {
        for (Feature f : features) {
            double newVal = f.getValue() + f.getVel();

            // clamp
            if (newVal > f.getMax()){
                newVal = f.getMax();
            }
            if (newVal < f.getMin()){
                newVal = f.getMin();
            }

            f.setValue(newVal);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof WillParticle) {
            WillParticle p = (WillParticle) o;

            return p.getFeatures().equals(p.getFeatures());
        }
        return false;
    }

    public void printDiffs() {
        features.forEach(f -> {
            f.printDiffs();
        });
    }
}
