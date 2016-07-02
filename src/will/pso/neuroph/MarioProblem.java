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
import will.neat.neuroph.MarioFitnessFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by Will on 1/07/2016.
 */
public class MarioProblem extends WillProblem {

    private static final int NUM_INPUT_NEURONS = 367;
    private static final int NUM_OUTPUT_NEURONS = 4;

    private static final int POP_SIZE = 100;
    private static final double MAX_FITNESS = 8000;
    private static final long MAX_GENS = 50;

    private SimpleNeatParameters defaultParams;
    private List<Feature> features;

    private List<NeuronGene> inputNeurons;
    private List<NeuronGene> outputNeurons;

    public enum PARAMS {
        MAX_SPECIES, SURVIVAL_RATIO, ADD_CONN_PROB,
        ADD_NEURON_PROB, WEIGHT_ADJ_PROB, WEIGHT_PETURB
    };

    public MarioProblem() {
        defaultParams = new SimpleNeatParameters();

        defaultParams.setFitnessFunction(new MarioFitnessFunction());
        defaultParams.setPopulationSize(POP_SIZE);
        defaultParams.setMaximumFitness(MAX_FITNESS);
        defaultParams.setMaximumGenerations(MAX_GENS);

        DynamicThresholdSpeciator speciator = new DynamicThresholdSpeciator();
        defaultParams.setSpeciator(speciator);

        NaturalSelectionOrganismSelector selector = (NaturalSelectionOrganismSelector) defaultParams
                .getOrganismSelector();
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
        double maxSpecies = defaultParams.getPopulationSize()/10;

        features.add(new Feature(PARAMS.MAX_SPECIES, 1, maxSpecies));
        features.add(new Feature(PARAMS.SURVIVAL_RATIO, 0, 0.5));
        features.add(new Feature(PARAMS.ADD_CONN_PROB, 0, 1));
        features.add(new Feature(PARAMS.ADD_NEURON_PROB, 0, 1));
        features.add(new Feature(PARAMS.WEIGHT_ADJ_PROB, 0, 1));
        features.add(new Feature(PARAMS.WEIGHT_PETURB, 0, 3));

        return features;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    @Override
    public double fitness(List<Feature> position) {

        SimpleNeatParameters params = getParamsFromFeatures(position);

        int NUM_TRIALS = 5;
        double total = 0;

        Evolver evolver = Evolver.createNew(params, inputNeurons, outputNeurons);

        for (int t = 0; t < NUM_TRIALS; t++) {
            try {
                evolver.evolve();
            } catch (PersistenceException e) {
                e.printStackTrace();
            }
            total += evolver.getBestFitness();
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
                    speciator.setMaxSpecies((int)feature.getValue());
                    defaultParams.setSpeciator(speciator);
                    break;
                case SURVIVAL_RATIO:
                    NaturalSelectionOrganismSelector selector = (NaturalSelectionOrganismSelector) defaultParams
                            .getOrganismSelector();
                    selector.setSurvivalRatio(feature.getValue());
                    break;
                case ADD_CONN_PROB:
                    WeightMutationOperation weightMutation = new WeightMutationOperation(0.4);
                    weightMutation.setMaxWeightPertubation(1);
                    ops.add(new AddConnectionMutationOperation(feature.getValue()));
                    break;
                case ADD_NEURON_PROB:
                    ops.add(new AddNeuronMutationOperation(feature.getValue()));
                    break;
                case WEIGHT_ADJ_PROB:
                    ops.add(new WeightMutationOperation(feature.getValue()));
                    break;
                // uses last defined weight mut op so case must be after
                case WEIGHT_PETURB:
                    Optional<WeightMutationOperation> mut = ops.stream()
                            .filter(op -> op instanceof WeightMutationOperation)
                            .map(op -> (WeightMutationOperation)op)
                            .findFirst();

                    WeightMutationOperation op = mut.get();
                    op.setMaxWeightPertubation(feature.getValue());
                    break;
            }
            defaultParams.setMutationOperators(ops);
        });
        return defaultParams;
    }
}
