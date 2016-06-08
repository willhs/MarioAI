package will.pso;

import com.anji.neat.Evolver;
import vuw.pso.WillProblem;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by Will on 16/05/2016.
 */
public class MarioProblem extends WillProblem {

    private final Properties props;
    private List<Feature> features;

    public MarioProblem() {

        // we are aiming for the HIGHEST score, not lowest
        setMinimization(false);

        props = new Properties();

        // load some default props (taken from double pole balancing)
        try {
            props.load(new FileReader("properties" + File.separator + "mario.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // how many generations will be needed per trial
        props.setProperty("num.generations", "" + 20);
        // prevent the same thing happening each time
        props.remove("random.seed");

        props.setProperty("popul.size", "" + 200);

        // set default values and limits for each property that will be changed
        features = new ArrayList<>();

        // feature: (name, starting val, min, max)
        features.add(new Feature("add.connection.mutation.rate", 0.02, 0.0001, 0.5));
        features.add(new Feature("remove.connection.mutation.rate", 0.01, 0.01, 0.3));
        features.add(new Feature("remove.connection.max.weight", 100, 1, 500));
        features.add(new Feature("add.neuron.mutation.rate", 0.01, 0.0001, 0.5));
        features.add(new Feature("prune.mutation.rate", 1, 0.5, 1.5)); // lacking experimentation
        features.add(new Feature("weight.mutation.rate", 0.75, 0.5, 0.8));
        features.add(new Feature("weight.mutation.std.dev", 1.5, 1, 2));
        features.add(new Feature("weight.max", 100, 0, 500));
        features.add(new Feature("weight.min", -100, -500, -1));
        features.add(new Feature("survival.rate", 0.2, 0.1, 0.5));

        // speciation
        features.add(new Feature("chrom.compat.excess.coeff", 1, 0, 1)); // lacking exp
        features.add(new Feature("chrom.compat.disjoint.coeff", 1, 0, 1)); // lacking exp
        features.add(new Feature("chrom.compat.common.coeff", 0.04, 0, 1)); // lacking exp
        features.add(new Feature("speciation.threshold", 0.2, 0.1, 1));
    }

    @Override
    public double fitness(List<Feature> features) {
        features.forEach(f -> props.setProperty(f.getName(), "" + f.getValue()));

        Evolver evolver = new Evolver();
        int NUM_TRIALS = 10;
        double total = 0;
        for (int t = 0; t < NUM_TRIALS; t++) {
            try {
                evolver.init(new com.anji.util.Properties(props));
                evolver.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
            total += evolver.getChamp().getFitnessValue();
        }
        double averageFitness = total / NUM_TRIALS;

        return averageFitness;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public Properties getProps() {
        return props;
    }
}