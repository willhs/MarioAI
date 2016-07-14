package will.pso.neuroph;

import org.neuroph.contrib.neat.gen.Evolver;
import org.neuroph.contrib.neat.gen.NeuronGene;
import org.neuroph.contrib.neat.gen.NeuronType;
import org.neuroph.contrib.neat.gen.impl.SimpleNeatParameters;
import org.neuroph.contrib.neat.gen.operations.MutationOperation;
import org.neuroph.contrib.neat.gen.operations.mutation.AddConnectionMutationOperation;
import org.neuroph.contrib.neat.gen.operations.mutation.AddNeuronMutationOperation;
import org.neuroph.contrib.neat.gen.operations.mutation.WeightMutationOperation;
import org.neuroph.contrib.neat.gen.operations.selector.NaturalSelectionOrganismSelector;
import org.neuroph.contrib.neat.gen.operations.speciator.DynamicThresholdSpeciator;
import org.neuroph.contrib.neat.gen.persistence.PersistenceException;
import will.neat.neuroph.MarioEvolver;
import will.neat.neuroph.MarioFitnessFunction;
import will.neat.neuroph.mutation.RemoveConnectionMutation;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by Will on 1/07/2016.
 */
public class MarioProblem extends WillProblem {

    // network io
    private static final int NUM_INPUT_NEURONS = MarioEvolver.NUM_INPUT_NEURONS;
    private static final int NUM_OUTPUT_NEURONS = MarioEvolver.NUM_OUTPUT_NEURONS;

    // constant neat parameters
    private static final int POP_SIZE =	200;
    private static final double MAX_FITNESS = 15000;
    private static final long MAX_GENS = 100;
    private static final int MIN_INDIVIDUAL_PER_SPECIE = 10;

    private SimpleNeatParameters defaultParams;
    private List<Feature> features;

    private List<NeuronGene> inputNeurons;
    private List<NeuronGene> outputNeurons;

    public enum PARAMS {
        MAX_SPECIES, SURVIVAL_RATIO, ADD_CONN_PROB, REMOVE_CONN_PROB,
        ADD_NEURON_PROB, WEIGHT_ADJ_PROB, WEIGHT_PETURB, SPECIES_DROPOFF
    };

    public MarioProblem() {
        // we are aiming for the HIGHEST score, not lowest
        setMinimization(false);

        defaultParams = new SimpleNeatParameters();

        defaultParams.setFitnessFunction(new MarioFitnessFunction());
        defaultParams.setPopulationSize(POP_SIZE);
        defaultParams.setMaximumFitness(MAX_FITNESS);
        defaultParams.setMaximumGenerations(MAX_GENS);

        DynamicThresholdSpeciator speciator = new DynamicThresholdSpeciator();
        defaultParams.setSpeciator(speciator);

        NaturalSelectionOrganismSelector selector = (NaturalSelectionOrganismSelector) defaultParams
                .getOrganismSelector();
        selector.setElitismEnabled(true);
        selector.setKillUnproductiveSpecies(true);

        // instantiate input and output neurons
        inputNeurons = new ArrayList<>();
        outputNeurons = new ArrayList<>();

        for (int input = 0; input < NUM_INPUT_NEURONS; input++) {
            inputNeurons.add(new NeuronGene(NeuronType.INPUT, defaultParams));
        }

        for (int output = 0; output < NUM_OUTPUT_NEURONS; output++) {
            outputNeurons.add(new NeuronGene(NeuronType.OUTPUT, defaultParams));
        }

        // initialise features
        features = makeFeatures(defaultParams);
    }

    private static List<Feature> makeFeatures(SimpleNeatParameters defaultParams) {
        List<Feature> features = new ArrayList<>();
        double maxSpecies = defaultParams.getPopulationSize() / MIN_INDIVIDUAL_PER_SPECIE;

        double maxSpeciesDropoff = MAX_GENS/2;

        features.add(new Feature(PARAMS.ADD_CONN_PROB, 0, 1));
        features.add(new Feature(PARAMS.ADD_NEURON_PROB, 0, 1));
        features.add(new Feature(PARAMS.WEIGHT_ADJ_PROB, 0, 1));
        features.add(new Feature(PARAMS.REMOVE_CONN_PROB, 0, 1));
        features.add(new Feature(PARAMS.WEIGHT_PETURB, 0, 2.5));
        features.add(new Feature(PARAMS.SURVIVAL_RATIO, 0, 0.5));
        features.add(new Feature(PARAMS.MAX_SPECIES, 1, maxSpecies));
        features.add(new Feature(PARAMS.SPECIES_DROPOFF, 5, maxSpeciesDropoff));

        return features;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    @Override
    public double fitness(List<Feature> position) {

        SimpleNeatParameters params = getParamsFromFeatures(position);

        int NUM_TRIALS = 1;
        double total = 0;

        Evolver evolver = Evolver.createNew(params, inputNeurons, outputNeurons);

        for (int t = 0; t < NUM_TRIALS; t++) {
            try {
                evolver.getParams().setRandomGenerator(new SecureRandom());
//                System.out.println(evolver.getParams().getRandomGenerator().hashCode());
//                System.out.println(evolver.getParams().getOrganismSelector().);
                evolver.evolve();
            } catch (PersistenceException e) {
                e.printStackTrace();
            }

            double trialBest = evolver.getBestFitness();
            total += trialBest;

            System.out.println("Trial " + t + " best = " + trialBest);
        }
        double averageFitness = total / NUM_TRIALS;

        return averageFitness;
    }

    private SimpleNeatParameters getParamsFromFeatures(List<Feature> position) {
        List<MutationOperation> ops = new ArrayList<>();
        position.forEach(feature -> {
            switch(feature.getFeature()) {
                case MAX_SPECIES:
                    DynamicThresholdSpeciator speciator = new DynamicThresholdSpeciator();
                    speciator.setMaxSpecies((int)Math.round(feature.getValue()));
                    defaultParams.setSpeciator(speciator);
                    break;
                case SURVIVAL_RATIO:
                    NaturalSelectionOrganismSelector selector = (NaturalSelectionOrganismSelector) defaultParams
                            .getOrganismSelector();
                    selector.setSurvivalRatio(feature.getValue());
                    break;
                case ADD_CONN_PROB:
                    ops.add(new AddConnectionMutationOperation(feature.getValue()));
                    break;
                case REMOVE_CONN_PROB:
                    ops.add(new RemoveConnectionMutation(feature.getValue()));
                    break;
                case ADD_NEURON_PROB:
                    ops.add(new AddNeuronMutationOperation(feature.getValue()));
                    break;
                case WEIGHT_ADJ_PROB:
                    ops.add(new WeightMutationOperation(feature.getValue()));
                    break;
                // uses last defined weight mut op so case must be after
                case WEIGHT_PETURB:
                    WeightMutationOperation mut = ops.stream()
                            .filter(op -> op instanceof WeightMutationOperation)
                            .map(op -> (WeightMutationOperation)op)
                            .findFirst()
                            .get();

                    mut.setMaxWeightPertubation(feature.getValue());
                    break;
            }
            defaultParams.setMutationOperators(ops);
        });
        return defaultParams;
    }
}
