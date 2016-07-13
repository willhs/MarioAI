package will.neat.neuroph;

import org.neuroph.contrib.neat.gen.*;
import org.neuroph.contrib.neat.gen.impl.SimpleNeatParameters;
import org.neuroph.contrib.neat.gen.operations.MutationOperation;
import org.neuroph.contrib.neat.gen.operations.mutation.AddConnectionMutationOperation;
import org.neuroph.contrib.neat.gen.operations.mutation.AddNeuronMutationOperation;
import org.neuroph.contrib.neat.gen.operations.mutation.WeightMutationOperation;
import org.neuroph.contrib.neat.gen.operations.selector.NaturalSelectionOrganismSelector;
import org.neuroph.contrib.neat.gen.operations.speciator.DynamicThresholdSpeciator;
import org.neuroph.contrib.neat.gen.persistence.PersistenceException;
import will.neat.neuroph.mutation.RemoveConnectionMutation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Will on 28/06/2016.
 */
public class MarioEvolver {

    private static final int NUM_INPUT_NEURONS = 367;
    private static final int NUM_OUTPUT_NEURONS = 4;
    private static final String PERSISTANCE_DIR = "db/neuroph";

    private static final int POP_SIZE = 200;
    private static final double MAX_FITNESS = 8000;
    private static final long MAX_GENERATIONS = 200;

    private static final int MIN_PER_SPECIE = 20;
    private static final int MAX_SPECIES = POP_SIZE / MIN_PER_SPECIE;
    private static final double SURVIVAL_RATIO = 0.1;
    private static final boolean KILL_UNPRODUCTIVE_SPECIES = true;

//    private static final double ADD_CONN_PROB = 0.9;
//    private static final double REMOVE_CONN_PROB = 0.3;
//    private static final double ADD_NEURON_PROB = 0.5;
//    private static final double WEIGHT_MUT_PROB = 0.9;

    private static final double ADD_CONN_PROB = 0.04;
    private static final double REMOVE_CONN_PROB = 0.02;
    private static final double ADD_NEURON_PROB = 0.02;
    private static final double WEIGHT_MUT_PROB = 0.04;

    private static final double MAX_WEIGHT_MUT_AMOUNT = 3;
    private static final int MAX_GENS_SPECIES = 30;

    public static void main(String[] args) {
        // set up NEAT paramaters
        SimpleNeatParameters params = new SimpleNeatParameters();

        params.setFitnessFunction(new MarioFitnessFunction());
        params.setPopulationSize(POP_SIZE);
        params.setMaximumFitness(MAX_FITNESS);
        params.setMaximumGenerations(MAX_GENERATIONS);

        DynamicThresholdSpeciator speciator = new DynamicThresholdSpeciator();
        speciator.setMaxSpecies(MAX_SPECIES);
        params.setSpeciator(speciator);

        NaturalSelectionOrganismSelector selector = (NaturalSelectionOrganismSelector) params
                .getOrganismSelector();
        selector.setSurvivalRatio(SURVIVAL_RATIO);
        selector.setKillUnproductiveSpecies(KILL_UNPRODUCTIVE_SPECIES);
        selector.setMaximumGenerationsSinceImprovement(MAX_GENS_SPECIES);
//
        List<MutationOperation> ops = new ArrayList<>();

        AddConnectionMutationOperation addConnection = new AddConnectionMutationOperation(ADD_CONN_PROB);
        AddNeuronMutationOperation addNeuron = new AddNeuronMutationOperation(ADD_NEURON_PROB);
        RemoveConnectionMutation removeConnection = new RemoveConnectionMutation(REMOVE_CONN_PROB);
        WeightMutationOperation weightMutation = new WeightMutationOperation(WEIGHT_MUT_PROB);
        weightMutation.setMaxWeightPertubation(MAX_WEIGHT_MUT_AMOUNT);

        ops.add(addNeuron);
        ops.add(addConnection);
        ops.add(weightMutation);
        ops.add(removeConnection);

        params.setMutationOperators(ops);

//        String RUN_DIR = "run-" + System.currentTimeMillis();
//        params.setPersistence(new DirectoryOutputPersistence(
//                PERSISTANCE_DIR + File.separator + RUN_DIR,
//                new XStreamSerializationDelegate(true))
//        );

        // instantiate input and output neurons
        List<NeuronGene> inputNeurons = new ArrayList<>();
        List<NeuronGene> outputNeurons = new ArrayList<>();

        for (int input = 0; input < NUM_INPUT_NEURONS; input++) {
            inputNeurons.add(new NeuronGene(NeuronType.INPUT, params));
        }

        for (int output = 0; output < NUM_OUTPUT_NEURONS; output++) {
            outputNeurons.add(new NeuronGene(NeuronType.OUTPUT, params));
        }

        Evolver evolver = Evolver.createNew(params, inputNeurons, outputNeurons);

        try {
            Organism best = evolver.evolve();
            System.out.println("GAME!");
            System.out.println("This game's winner: Organism " + best.getInnovationId());
        } catch (PersistenceException e) {
            System.err.println("Persistance error!");
            e.printStackTrace();
        }
    }

    private class ANJINEATParameters extends SimpleNeatParameters {
        public ANJINEATParameters() {
            super();

        }
    }
}
