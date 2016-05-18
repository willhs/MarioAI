package ch.idsia.neat.pso;

/**
 * Created by Will on 18/05/2016.
 */
public class Feature {

    private final double initialVal;

    private String name;
    private double val;
    private double max;
    private double min;
    private double vel; // velocity

    public Feature(double initialVal) {
        this.initialVal = initialVal;
    }

    public Feature(String name, double initialVal, double max, double min) {
        this.name = name;
        this.initialVal = initialVal;
        this.max = max;
        this.min = min;

        // calculate starting value and velocity (random)
        // use same calculations as the pso code
        this.vel = pso.Math.Scale(0, 1, Math.random(), min, max);
        this.val = pso.Math.Scale(0, 1, Math.random(),
                1.0 / 5.0 * min,
                1.0 / 5.0 * max
        );

    }

    public double getInitialVal() {
        return initialVal;
    }

    public double getValue() {
        return val;
    }

    public void setValue(double val) {
        this.val = val;
    }

    public double getMax() {
        return max;
    }

    public double getMin() {
        return min;
    }

    public String getName() {
        return name;
    }

    public double getVel() {
        return vel;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Feature) {
            Feature f = (Feature) o;
            return f.getValue() == this.val
                    && f.getName().equals(this.name);
        }
        return false;
    }

    public void setVel(double vel) {
        this.vel = vel;
    }
}
