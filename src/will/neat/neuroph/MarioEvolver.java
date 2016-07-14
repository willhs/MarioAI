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

    public static final int NUM_INPUT_NEURONS = 728; // 19*19 grid = 361
    public static final int NUM_OUTPUT_NEURONS = 4;
    public static final String PERSISTANCE_DIR = "db/neuroph";

    public static final int POP_SIZE = 200;
    public static final double MAX_FITNESS = 15000;
    public static final long MAX_GENERATIONS = 200;

    public static final int MIN_PER_SPECIE = 15;
    public static final int MAX_SPECIES = POP_SIZE / MIN_PER_SPECIE;
    public static final double SURVIVAL_RATIO = 0.1;
    public static final boolean KILL_UNPRODUCTIVE_SPECIES = true;

    // fully connected
//    private static final double ADD_CONN_PROB = 0.2;
//    private static final double REMOVE_CONN_PROB = 0.8;
//    private static final double ADD_NEURON_PROB = 0.5;
//    private static final double PERTURB_PROB = 0.9;

    // FS-NEAT
    private static final double ADD_CONN_PROB = 0.98;
    private static final double REMOVE_CONN_PROB = 0.01;
    private static final double ADD_NEURON_PROB = 0.92;
    private static final double PERTURB_PROB = 0.5;
    private static final double MAX_PERTURB = 1;

//    private static final double ADD_CONN_PROB = 0.04;
//    private static final double REMOVE_CONN_PROB = 0.02;
//    private static final double ADD_NEURON_PROB = 0.02;
//    private static final double PERTURB_PROB = 0.04;

//    private static final double MAX_PERTURB = 2;
    private static final int MAX_GENS_SPECIES = 15;

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
        WeightMutationOperation weightMutation = new WeightMutationOperation(PERTURB_PROB);
        weightMutation.setMaxWeightPertubation(MAX_PERTURB);

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
