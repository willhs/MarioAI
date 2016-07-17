/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package will.pso.anji;

import will.pso.neuroph.Feature;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 *
 * @author xuebing + Will
 */
public class ANJIParticle {

    private List<ANJIFeature> features = new ArrayList<>();
    private List<Double> pBestFeats = new ArrayList<>();
    private List<Double> nBestFeats = new ArrayList<>();

    private double fitness; // current fitness
    private double pBestFitness; // personal best fitness
    private double nBestFitness;

    private double inertia;
    private double c1,  c2;
    private Random r1 = new Random(),  r2 = new Random();

    public ANJIParticle(List<ANJIFeature> features) {
        this.features = features.stream().map(f -> f.clone()).collect(Collectors.toList());

        for (ANJIFeature f : this.features) {
            // add dummy features (just until old code is understood)
            f.generateInitialVals();
            this.pBestFeats.add(0.0);
            this.nBestFeats.add(0.0);
        }
    }

    public ANJIParticle(List<ANJIFeature> features, List<Double> pBestFeats, List<Double> nBestFeats,
                        double fitness, double pBestFitness, double nBestFitness,
                        double c1, double c2, double inertia) {
        this.features = features;
        this.pBestFeats = pBestFeats;
        this.nBestFeats = nBestFeats;

        this.fitness = fitness;
        this.pBestFitness = pBestFitness;
        this.nBestFitness = nBestFitness;

        this.c1 = c1;
        this.c2 = c2;
        this.inertia = inertia;
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

    public List<ANJIFeature> getFeatures() {
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
        pBestFeats.set(index, value);
    }

    public double getPBestFeatures(int index) {
        return pBestFeats.get(index);
    }

    public List<Double> getPBestfeatures() {
        return pBestFeats;
    }

    public double getPBestFitness() {
        return pBestFitness;
    }

    public void setPBestFitness(double fitness_best_personal) {
        pBestFitness = fitness_best_personal;
    }

    public void setNBestFeats(int index, double value) {
        this.nBestFeats.set(index, value);
    }

    public double getNBestFeat(int index) {
        return nBestFeats.get(index);
    }

    public List<Double> getNBestfeatures() {
        return nBestFeats;
    }

    public double getNBestFitness() {
        return nBestFitness;
    }

    public void setNBestFitness(double nBest) {
        this.nBestFitness = nBest;
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
            double distToPBest = getPBestFeatures(i) - getFeatures(i);
            double distToNeighBest = getNBestFeat(i) - getFeatures(i);
            double firstMult = getC1() * getR1().nextDouble() * distToPBest;
            double secondMult = getC2() * getR2().nextDouble() * distToNeighBest;
            v_i += firstMult;
            v_i += secondMult;

/*            System.out.println("--- updating " + i + "th feature ----");
            System.out.println(getFeatures().get(i));
            System.out.printf("vel: %4.2f, after inertia: %4.2f\n", getVelocity(i), (getVelocity(i) * getInertia()));
            System.out.println("distToPBest: " + distToPBest + ", distToNeighBest: " + distToNeighBest);
            System.out.println("pBest: " + getPBestfeatures(i) + " nBest: " + getNBestFeat(i));
            System.out.printf("first mult: %4.2f, second: %4.2f \n", firstMult, secondMult);
            System.out.printf("final vel: %4.2f\n", v_i);
            System.out.println("----------------------------------");*/

            setVelocity(i, v_i);
        }
    }

    public void updatePosition() {
        for (ANJIFeature f : features) {
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

    /**
     * prints the differences between each feature's initial and current value
     */
    public void printDiffs() {
        features.forEach(f -> {
            f.printDiffs();
        });
    }

    public String toString() {
        return "Particle. PBest: " + pBestFitness + ", NBest: " + nBestFitness
                + "\nPBest position: " + pBestFeats
                + "\nNBest position: " + nBestFeats
                + "\nCurr. position: " + features.stream().map(f->f.getValue()).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ANJIParticle) {
            ANJIParticle other = (ANJIParticle) o;
            return features.equals(other.getFeatures())
                    && pBestFeats.equals(other.getPBestfeatures())
                    && nBestFeats.equals(other.getNBestfeatures())
                    && fitness == other.fitness
                    && pBestFitness == other.getPBestFitness()
                    && nBestFitness == other.getNBestFitness()
                    && c1 == other.getC1()
                    && c2 == other.getC2()
                    && inertia == other.getInertia();
        } else {
            return false;
        }
    }
}

