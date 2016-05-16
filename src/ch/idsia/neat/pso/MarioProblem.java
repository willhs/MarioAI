package ch.idsia.neat.pso;

import pso.Problem;

import java.util.List;
import com.anji.util.Properties;

/**
 * Created by Will on 16/05/2016.
 */
public class MarioProblem extends Problem {
    @Override
    public double fitness(List<Double> position) {
        Properties props = new Properties();

        // evolution
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

/*        add.connection.mutation.rate=0.02
        remove.connection.mutation.rate=0.01
        remove.connection.max.weight=100
        add.neuron.mutation.rate=0.01
        prune.mutation.rate=1.0
        weight.mutation.rate=0.75
        weight.mutation.std.dev=1.5
        weight.max=500.0
        weight.min=-500.0
        survival.rate=0.2
        chrom.compat.excess.coeff=1.0
        chrom.compat.disjoint.coeff=1.0
        chrom.compat.common.coeff=0.04
        speciation.threshold=0.2
        */
    }
}
