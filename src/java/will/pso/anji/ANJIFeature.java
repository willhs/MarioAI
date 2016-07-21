package will.pso.anji;

/**
 * Created by Will on 18/05/2016.
 */
public class ANJIFeature {

    private final double initialVal;

    private String name;
    private double val;
    private double max;
    private double min;
    private double vel; // velocity

    public ANJIFeature(double initialVal) {
        this.initialVal = initialVal;
    }

    public ANJIFeature(String name, double initialVal, double min, double max) {
        this.name = name;
        this.initialVal = initialVal;
        this.max = max;
        this.min = min;
    }

    public ANJIFeature(String name, double val, double vel, double min, double max, double initialVal) {
        this.name = name;
        this.val = val;
        this.vel = vel;
        this.min = min;
        this.max = max;
        this.initialVal = initialVal;
    };

    public void generateInitialVals() {
        // calculate starting value and velocity (random)
        // use same calculations as the vuw.vuw.pso code
        this.val = vuw.pso.Math.Scale(0, 1, Math.random(), min, max);
        this.vel = vuw.pso.Math.Scale(0, 1, Math.random(),
                1.0 / 5.0 * -(max-min),
                1.0 / 5.0 * (max-min)
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
        if (o instanceof ANJIFeature) {
            ANJIFeature f = (ANJIFeature) o;
            return f.getValue() == this.val
                    && f.getName().equals(this.name)
                    && f.getVel() == f.getVel()
                    && f.getInitialVal() == f.getInitialVal()
                    && f.getMin() == f.getMin()
                    && f.getMax() == f.getMax();
        }
        return false;
    }

    public void setVel(double vel) {
        this.vel = vel;
    }

    public void printDiffs() {
        System.out.printf(
//                "%s: %f, %4.2f%% difference%n",
//                name, val, (((Math.abs(initialVal - val))/(Math.abs(max - min)))*100)
                "%s: val: %4.2f, init: %4.2f, vel: %4.2f\n",
                name, val, initialVal, vel
        );
    }

    public String toString(){
        return String.format(
        "%s: [%f - %f] val, init: (%4.2f, %f), vel: %4.2f",
                 name, min, max, val, initialVal, vel
        );
    }

    public ANJIFeature clone() {
        return new ANJIFeature(name, initialVal, min, max);
    }
}
