package ch.idsia.neat.pso;

/**
 * Created by Will on 18/05/2016.
 */
public class Position {

    private double val;
    private double max;
    private double min;

    public Position(double val) {
        this.val = val;
    }

    public Position(double val, double max, double min) {
        this.val = val;
        this.max = max;
        this.min = min;
    }

    public double getVal() {
        return val;
    }

    public double getMax() {
        return max;
    }

    public double getMin() {
        return min;
    }

    public void setValue(double value) {
        this.val = value;
    }
}
