package will.pso;

import org.neuroph.contrib.neat.gen.Evolver;
import org.neuroph.contrib.neat.gen.NeuronGene;
import org.neuroph.contrib.neat.gen.NeuronType;
import org.neuroph.contrib.neat.gen.Organism;
import org.neuroph.contrib.neat.gen.impl.SimpleNeatParameters;
import org.neuroph.contrib.neat.gen.operations.MutationOperation;
import org.neuroph.contrib.neat.gen.operations.mutation.AddConnectionMutationOperation;
import org.neuroph.contrib.neat.gen.operations.mutation.AddNeuronMutationOperation;
import org.neuroph.contrib.neat.gen.operations.mutation.WeightMutationOperation;
import org.neuroph.contrib.neat.gen.operations.selector.NaturalSelectionOrganismSelector;
import org.neuroph.contrib.neat.gen.operations.speciator.DynamicThresholdSpeciator;
import org.neuroph.contrib.neat.gen.persistence.PersistenceException;
import vuw.pso.WillProblem;
import will.neat.neuroph.MarioFitnessFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Will on 1/07/2016.
 */
public class MarioProblem extends WillProblem {

    private static final int NUM_INPUT_NEURONS = 367;
    private static final int NUM_OUTPUT_NEURONS = 4;

    private SimpleNeatParameters params;

    private List<NeuronGene> inputNeurons;
    private List<NeuronGene> outputNeurons;

    public MarioProblem() {
        params = new SimpleNeatParameters();

        params.setFitnessFunction(new MarioFitnessFunction());
        params.setPopulationSize(100);
        params.setMaximumFitness(8000);
        params.setMaximumGenerations(50);

        DynamicThresholdSpeciator speciator = new DynamicThresholdSpeciator();
        speciator.setMaxSpecies(5);
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

        // instantiate input and output neurons
        inputNeurons = new ArrayList<>();
        outputNeurons = new ArrayList<>();

        for (int input = 0; input < NUM_INPUT_NEURONS; input++) {
            inputNeurons.add(new NeuronGene(NeuronType.INPUT, params));
        }

        for (int output = 0; output < NUM_OUTPUT_NEURONS; output++) {
            outputNeurons.add(new NeuronGene(NeuronType.OUTPUT, params));
        }

    }

    @Override
    public double fitness(List<Feature> position) {

        int NUM_TRIALS = 5;
        double total = 0;

        Evolver evolver = Evolver.createNew(params, inputNeurons, outputNeurons);

        for (int t = 0; t < NUM_TRIALS; t++) {
            Organism best = null;
            try {
                best = evolver.evolve();
            } catch (PersistenceException e) {
                e.printStackTrace();
            }
//            total += best.;
        }
        double averageFitness = total / NUM_TRIALS;

        return averageFitness;
    }
}
