package ch.idsia.neat.pso;

import com.anji.neat.Evolver;
import pso.Problem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * Created by Will on 16/05/2016.
 */
public class MarioProblem extends Problem {

    private final Evolver evolver;
    private final Properties props;

    public MarioProblem() {

        // load default (taken from double pole balancing) properties from the file
        props = new Properties();

        try {
            props.load(new FileReader("properties" + File.separator + "mario.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // how many generations will be needed per trial
        props.setProperty("num.generations", "" + 40);

        evolver = new Evolver();
        try {
            evolver.init( new com.anji.util.Properties(props) );
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public double fitness(List<Double> position) {
        props.setProperty("add.connection.mutation.rate", "" + position.get(0));
        props.setProperty("remove.connection.mutation.rate", "" + position.get(1));
        props.setProperty("remove.connection.max.weight", "" + position.get(2));
        props.setProperty("add.neuron.mutation.rate", "" + position.get(3));
        props.setProperty("prune.mutation.rate", "" + position.get(4));
        props.setProperty("weight.mutation.rate", "" + position.get(5));
        props.setProperty("weight.mutation.std.dev", "" + position.get(6));
        props.setProperty("weight.max", "" + position.get(7));
        props.setProperty("weight.min", "" + position.get(8));
        props.setProperty("survival.rate", "" + position.get(9));
        props.setProperty("weight.mutation.std.dev", "" + position.get(10));

        // speciation
        props.setProperty("chrom.compat.excess.coeff", "" + position.get(11));
        props.setProperty("chrom.compat.disjoint.coeff", "" + position.get(12));
        props.setProperty("chrom.compat.common.coeff", "" + position.get(13));
        props.setProperty("speciation.threshold", "" + position.get(14));


        try {
            evolver.run();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return evolver.getChamp().getFitnessValue();
    }

    public Properties getProps() {
        return props;
    }
}
