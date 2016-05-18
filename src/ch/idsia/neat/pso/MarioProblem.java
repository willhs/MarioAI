package ch.idsia.neat.pso;

import com.anji.neat.Evolver;
import pso.Problem;
import pso.WillProblem;

import java.io.BufferedReader;
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

        // load default (taken from double pole balancing) properties from the file
        props = new Properties();

        try {
            props.load(new FileReader("properties" + File.separator + "mario.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // how many generations will be needed per trial
        props.setProperty("num.generations", "" + 3);
        props.remove("random.seed");

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
        features.add(new Feature("weight.max", 500, 1, 500));
        features.add(new Feature("weight.min", -500, -500, -1));
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
        try {
            evolver.init(new com.anji.util.Properties(props));
            evolver.run();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return evolver.getChamp().getFitnessValue();
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public Properties getProps() {
        return props;
    }
}
