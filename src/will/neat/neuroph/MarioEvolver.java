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
import org.neuroph.contrib.neat.gen.persistence.impl.DirectoryOutputPersistence;
import org.neuroph.contrib.neat.gen.persistence.impl.xstream.XStreamSerializationDelegate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Will on 28/06/2016.
 */
public class MarioEvolver {

    private static final int NUM_INPUT_NEURONS = 367;
    private static final int NUM_OUTPUT_NEURONS = 4;
    private static final String PERSISTANCE_DIR = "db/neuroph";

    public static void main(String[] args) {
        // set up NEAT paramaters
        SimpleNeatParameters params = new SimpleNeatParameters();

        params.setFitnessFunction(new MarioFitnessFunction());
        params.setPopulationSize(500);
        params.setMaximumFitness(8500);
        params.setMaximumGenerations(500);

        DynamicThresholdSpeciator speciator = new DynamicThresholdSpeciator();
        speciator.setMaxSpecies(25);
        params.setSpeciator(speciator);

        NaturalSelectionOrganismSelector selector = (NaturalSelectionOrganismSelector) params
                .getOrganismSelector();
//        selector.setSurvivalRatio(0.1);
        selector.setKillUnproductiveSpecies(true);
//
        List<MutationOperation> ops = new ArrayList<>();

        AddConnectionMutationOperation addConnection = new AddConnectionMutationOperation(1);
        AddNeuronMutationOperation addNeuron = new AddNeuronMutationOperation(0.4);
        WeightMutationOperation weightMutation = new WeightMutationOperation(0.4);
        weightMutation.setMaxWeightPertubation(1);

        ops.add(addNeuron);
        ops.add(addConnection);
        ops.add(weightMutation);

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
}
