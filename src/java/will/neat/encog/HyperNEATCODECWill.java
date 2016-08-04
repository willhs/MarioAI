package will.neat.encog;

import org.encog.engine.network.activation.*;
import org.encog.ml.MLMethod;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.ea.genome.Genome;
import org.encog.ml.genetic.GeneticError;
import org.encog.neural.hyperneat.HyperNEATCODEC;
import org.encog.neural.hyperneat.substrate.Substrate;
import org.encog.neural.hyperneat.substrate.SubstrateLink;
import org.encog.neural.hyperneat.substrate.SubstrateNode;
import org.encog.neural.neat.NEATCODEC;
import org.encog.neural.neat.NEATLink;
import org.encog.neural.neat.NEATNetwork;
import org.encog.neural.neat.NEATPopulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by willhs on 22/07/2016.
 */
public class HyperNEATCODECWill extends HyperNEATCODEC {

    private double minWeight = 0.1;
    private double maxWeight = 1.0;

    Logger logger = Logger.getLogger(getClass().getSimpleName());

    /**
     * {@inheritDoc}
     */
    @Override
    public MLMethod decode(final Genome genome) {
        final NEATPopulation pop = (NEATPopulation) genome.getPopulation();
        final Substrate substrate = pop.getSubstrate();
        return decode(pop, substrate, genome);
    }

    public MLMethod decode(final NEATPopulation pop, final Substrate substrate,
                           final Genome genome) {
        // obtain the CPPN
        final NEATCODEC neatCodec = new NEATCODEC();
        NEATNetwork cppn = (NEATNetwork) neatCodec.decode(genome);

        // scale down weights
        double scaleFactor = 0.2;
        List<NEATLink> newLinks = Arrays.stream(cppn.getLinks())
                .map(l -> new NEATLink(l.getFromNeuron(), l.getToNeuron(), l.getWeight() * scaleFactor))
                .collect(Collectors.toList());

        cppn = new NEATNetwork(cppn.getInputCount(), cppn.getOutputCount(), newLinks, cppn.getActivationFunctions());

        // log weights
        if (Math.random() < 0.01) {
//            logger.info("CPPN links: (" + cppn.getLinks().length + ") : " + Arrays.toString(Arrays.stream(cppn.getLinks()).mapToDouble(l -> l.getWeight()).toArray()));
        }

        final List<NEATLink> linkList = new ArrayList<>();

        final ActivationFunction[] afs = new ActivationFunction[substrate
                .getNodeCount()];

        final ActivationFunction af = new ActivationClippedLinear();
        // all activation functions are the same
        for (int i = 0; i < afs.length; i++) {
            afs[i] = af;
        }

        final double c = this.maxWeight / (1.0 - this.minWeight);
        final MLData input = new BasicMLData(cppn.getInputCount());

        // First create all of the non-bias links.
        for (final SubstrateLink link : substrate.getLinks()) {
            final SubstrateNode source = link.getSource();
            final SubstrateNode target = link.getTarget();

            int index = 0;
            for (final double d : source.getLocation()) {
                input.setData(index++, d);
            }
            for (final double d : target.getLocation()) {
                input.setData(index++, d);
            }
            final MLData output = cppn.compute(input);

            double weight = output.getData(0);
            if (Math.abs(weight) > this.minWeight) {
                weight = (Math.abs(weight) - this.minWeight) * c
                        * Math.signum(weight);
                linkList.add(new NEATLink(source.getId(), target.getId(),
                        weight));
            }
        }

        // now create biased links
        input.clear();
        final int d = substrate.getDimensions();
        final List<SubstrateNode> biasedNodes = substrate.getBiasedNodes();
        for (final SubstrateNode target : biasedNodes) {
            for (int i = 0; i < d; i++) {
                input.setData(d + i, target.getLocation()[i]);
            }

            final MLData output = cppn.compute(input);

            double biasWeight = output.getData(1);
            if (Math.abs(biasWeight) > this.minWeight) {
                biasWeight = (Math.abs(biasWeight) - this.minWeight) * c
                        * Math.signum(biasWeight);
                linkList.add(new NEATLink(0, target.getId(), biasWeight));
            }
        }

        // check for invalid neural network
        if (linkList.size() == 0) {
            return null;
        }

        Collections.sort(linkList);

        final NEATNetwork network = new NEATNetwork(substrate.getInputCount(),
                substrate.getOutputCount(), linkList, afs);

        network.setActivationCycles(substrate.getActivationCycles());
        return network;

    }

    @Override
    public Genome encode(final MLMethod phenotype) {
        throw new GeneticError(
                "Encoding of a HyperNEAT network is not supported.");
    }

    /**
     * @return the maxWeight
     */
    public double getMaxWeight() {
        return this.maxWeight;
    }

    /**
     * @return the minWeight
     */
    public double getMinWeight() {
        return this.minWeight;
    }

    /**
     * @param maxWeight the maxWeight to set
     */
    public void setMaxWeight(final double maxWeight) {
        this.maxWeight = maxWeight;
    }

    /**
     * @param minWeight the minWeight to set
     */
    public void setMinWeight(final double minWeight) {

    }
}
