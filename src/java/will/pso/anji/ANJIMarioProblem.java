package will.pso.anji;

import com.anji.neat.Evolver;
import com.anji.util.Properties;
import will.pso.Feature;
import will.pso.WillProblem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Will on 16/05/2016.
 */
public class ANJIMarioProblem extends WillProblem {

    private Properties props;
    private List<Feature> Features;

    public ANJIMarioProblem() {

        // we are aiming for the HIGHEST score, not lowest
        setMinimization(false);

        // load some default props (taken from double pole balancing)
        try {
            props = new Properties("properties/mario.properties");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // how many generations will be needed per trial
        props.setProperty("num.generations", "" + 15);
        // prevent the same thing happening each time
//        props.remove("random.seed");

        props.setProperty("popul.size", "" + 120);

        // set default values and limits for each property that will be changed
        Features = new ArrayList<>();

        // feature: (name, starting val, min, max)
        Features.add(new Feature("add.connection.mutation.rate", 0.02, 0.0001, 0.5));
        Features.add(new Feature("remove.connection.mutation.rate", 0.01, 0.01, 0.3));
        Features.add(new Feature("remove.connection.max.weight", 100, 1, 500));
        Features.add(new Feature("add.neuron.mutation.rate", 0.01, 0.0001, 0.5));
        Features.add(new Feature("prune.mutation.rate", 1, 0, 1)); // lacking experimentation
        Features.add(new Feature("weight.mutation.rate", 0.75, 0.5, 0.8));
        Features.add(new Feature("weight.mutation.std.dev", 1.5, 1, 2));
        Features.add(new Feature("weight.max", 100, 1, 500));
        Features.add(new Feature("weight.min", -100, -500, -1));
        Features.add(new Feature("survival.rate", 0.2, 0.1, 0.5));

        // speciation
        Features.add(new Feature("chrom.compat.excess.coeff", 1, 0, 1)); // lacking exp
        Features.add(new Feature("chrom.compat.disjoint.coeff", 1, 0, 1)); // lacking exp
        Features.add(new Feature("chrom.compat.common.coeff", 0.04, 0, 1)); // lacking exp
        Features.add(new Feature("speciation.threshold", 0.2, 0.1, 1));
    }

    @Override
    public double fitness(List<Feature> Features) {
        Features.forEach(f -> props.setProperty(f.getName(), "" + f.getValue()));

        int NUM_TRIALS = 5;
        double total = 0;

        for (int t = 0; t < NUM_TRIALS; t++) {
            Evolver evolver = new Evolver();
            try {
                evolver.init(new com.anji.util.Properties(props));
                evolver.run();
                System.out.println("nice props: " + props);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("---------- evolver fucked up ----------");
                System.out.println("offending props: " + props);
            }
            total += evolver.getChamp().getFitnessValue();
        }
        double averageFitness = total / NUM_TRIALS;

        return averageFitness;
    }

    public List<Feature> getFeatures() {
        return Features;
    }

    public Properties getProps() {
        return props;
    }
}
