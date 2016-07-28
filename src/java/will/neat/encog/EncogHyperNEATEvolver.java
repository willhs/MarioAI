package will.neat.encog;

import org.encog.ml.CalculateScore;
import org.encog.ml.ea.opp.CompoundOperator;
import org.encog.ml.ea.opp.selection.TruncationSelection;
import org.encog.ml.ea.train.basic.TrainEA;
import org.encog.neural.hyperneat.HyperNEATCODEC;
import org.encog.neural.hyperneat.HyperNEATGenome;
import org.encog.neural.hyperneat.substrate.Substrate;
import org.encog.neural.hyperneat.substrate.SubstrateNode;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.training.opp.*;
import org.encog.neural.neat.training.opp.links.MutatePerturbLinkWeight;
import org.encog.neural.neat.training.opp.links.MutateResetLinkWeight;
import org.encog.neural.neat.training.opp.links.SelectFixed;
import org.encog.neural.neat.training.opp.links.SelectProportion;
import org.encog.neural.neat.training.species.OriginalNEATSpeciation;

import java.util.logging.Logger;

/**
 * Created by Will on 17/07/2016.
 */
public class EncogHyperNEATEvolver {

    // io
    public static final int NUM_INPUT_NEURONS = 361; //1089; // 19*19 grid = 361
    public static final int NUM_OUTPUT_NEURONS = 4;

    // evolution
    public static final int POP_SIZE = 200;
    public static final double MAX_FITNESS = 15000;
    public static final long MAX_GENERATIONS = 200;

    public static final int MIN_PER_SPECIE = 10;
    public static final int MAX_SPECIES = POP_SIZE / MIN_PER_SPECIE;
    private static final int MAX_GENS_SPECIES = 50;
    public static final double SURVIVAL_RATIO = 0.1;
    public static final boolean KILL_UNPRODUCTIVE_SPECIES = true;
    private static final double COMPAT_THRESHOLD = 6;
    private static final double ELITE_RATE = 0.1;
    private static final double CROSSOVER_PROB = 0.5;

    private static Logger logger = Logger.getLogger(EncogHyperNEATEvolver.class
            .getSimpleName());

    // mutations

    // fully connected
//    private static final double ADD_CONN_PROB = 0.2;
//    private static final double REMOVE_CONN_PROB = 0.8;
//    private static final double ADD_NEURON_PROB = 0.5;
//    private static final double PERTURB_PROB = 0.9;

    private static final double ADD_CONN_PROB = 0.8;
    private static final double REMOVE_CONN_PROB = 0.2;
    private static final double ADD_NEURON_PROB = 0.5;
    private static final double PERTURB_PROB = 0.9;

    // start no connections (FS-NEAT)
//    private static final double ADD_CONN_PROB = 1;
//    private static final double ADD_NEURON_PROB = 1;
//    private static final double PERTURB_PROB = 1;
//    private static final double REMOVE_CONN_PROB = 0.2;

    private static final double PERTURB_SD = 0.92; // perturb standard deviation

    public static void main(String[] args) {

        Substrate substrate = makeMarioBrosSubstrate();

        NEATPopulation population = new NEATPopulation(substrate, POP_SIZE);
        population.setActivationCycles(5);
        population.setSurvivalRate(SURVIVAL_RATIO);
        population.reset();
        population.setCODEC(new HyperNEATCODECWill());

        CalculateScore fitnessFunction = new EncogMarioFitnessFunction();

        OriginalNEATSpeciation speciation = new OriginalNEATSpeciation();
        speciation.setCompatibilityThreshold(COMPAT_THRESHOLD);
        speciation.setMaxNumberOfSpecies(MAX_SPECIES);
        speciation.setNumGensAllowedNoImprovement(MAX_GENS_SPECIES);

        final TrainEA neat = new TrainEA(population, fitnessFunction);
        neat.setSpeciation(speciation);
        neat.setSelection(new TruncationSelection(neat, 0.2));
        neat.setEliteRate(ELITE_RATE);

        CompoundOperator compoundWeightMutation = composeWeightMutation();
        NEATMutateWeights weightMutation = new NEATMutateWeights(
                new SelectFixed(1),
                new MutatePerturbLinkWeight(PERTURB_SD)
        );

        CompoundOperator crossover = new CompoundOperator();
        crossover.getComponents().add(CROSSOVER_PROB, new NEATCrossover());

        neat.addOperation(0.5, new NEATCrossover());
        neat.addOperation(PERTURB_PROB, compoundWeightMutation);
        neat.addOperation(ADD_NEURON_PROB, new NEATMutateAddNode());
        neat.addOperation(ADD_CONN_PROB, new NEATMutateAddLink());
        neat.addOperation(REMOVE_CONN_PROB, new NEATMutateRemoveLink());
        neat.getOperators().finalizeStructure();
        neat.setThreadCount(1);

/*        neat.addStrategy(new Strategy() {

            TrainEA train;

            @Override
            public void init(MLTrain train) {
                this.train = (TrainEA) train;
            }

            @Override
            public void preIteration() {

            }

            @Override
            public void postIteration() {
                HyperNEATGenome genome = (HyperNEATGenome) train.getBestGenome();
                List<NEATNeuronGene> inputs = genome.getNeuronsChromosome().stream()
                        .filter(c -> c.getNeuronType() == NEATNeuronType.Input)
                        .collect(Collectors.toList());

                inputs.forEach(i -> {
                    System.out.println(i.getId() + ": " + i.get);
                });
            }
        });*/

//        neat.setCODEC(new HyperNEATCODEC());
        neat.setCODEC(new HyperNEATCODECWill());

        int gen = 0;

        // evolve til done
        while (!neat.isTrainingDone() ||
                (population.getBestGenome() != null && population.getBestGenome().getScore() < MAX_FITNESS)) {

            neat.iteration();

            // report
            double bestFitness = population.getBestGenome().getScore();
            double bestFitnessGen = population.getSpecies().stream()
                    .flatMap(s -> s.getMembers().stream())
                    .mapToDouble(g -> g.getScore())
                    .max()
                    .getAsDouble();

            int numSpecies = population.getSpecies().size();

            double averageCPPNLinks = population.getSpecies().stream()
                    .map(s -> s.getMembers())
                    .flatMap(genomes -> genomes.stream())
                    .mapToInt(genome -> ((HyperNEATGenome)genome).getLinksChromosome().size())
                    .average()
                    .getAsDouble();

            double averageCPPNNodes = population.getSpecies().stream()
                    .map(s -> s.getMembers())
                    .flatMap(genomes -> genomes.stream())
                    .mapToInt(genome -> ((HyperNEATGenome)genome).getNeuronsChromosome().size())
                    .average()
                    .getAsDouble();


            logger.info("Generation:\t" + gen);
            logger.info("Best fitness:\t" + bestFitnessGen);
            logger.info("Num species:\t" + numSpecies);
            logger.info("Ave CPPN conns:\t" + averageCPPNLinks);
            logger.info("Ave CPPN nodes:\t" + averageCPPNNodes);

            gen++;
        }

        logger.info("Evolving done");
        logger.info("Winning fitness: " + population.getBestGenome().getScore());
    }

    private static CompoundOperator composeWeightMutation() {
        final CompoundOperator weightMutation = new CompoundOperator();

        weightMutation.getComponents().add(
                0.1125,
                new NEATMutateWeights(new SelectFixed(1),
                        new MutatePerturbLinkWeight(0.02)));
        weightMutation.getComponents().add(
                0.1125,
                new NEATMutateWeights(new SelectFixed(2),
                        new MutatePerturbLinkWeight(0.02)));
        weightMutation.getComponents().add(
                0.1125,
                new NEATMutateWeights(new SelectFixed(3),
                        new MutatePerturbLinkWeight(0.02)));
        weightMutation.getComponents().add(
                0.1125,
                new NEATMutateWeights(new SelectProportion(0.02),
                        new MutatePerturbLinkWeight(0.02)));
        weightMutation.getComponents().add(
                0.1125,
                new NEATMutateWeights(new SelectFixed(1),
                        new MutatePerturbLinkWeight(1)));
        weightMutation.getComponents().add(
                0.1125,
                new NEATMutateWeights(new SelectFixed(2),
                        new MutatePerturbLinkWeight(1)));
        weightMutation.getComponents().add(
                0.1125,
                new NEATMutateWeights(new SelectFixed(3),
                        new MutatePerturbLinkWeight(1)));
        weightMutation.getComponents().add(
                0.1125,
                new NEATMutateWeights(new SelectProportion(0.02),
                        new MutatePerturbLinkWeight(1)));
        weightMutation.getComponents().add(
                0.03,
                new NEATMutateWeights(new SelectFixed(1),
                        new MutateResetLinkWeight()));
        weightMutation.getComponents().add(
                0.03,
                new NEATMutateWeights(new SelectFixed(2),
                        new MutateResetLinkWeight()));
        weightMutation.getComponents().add(
                0.03,
                new NEATMutateWeights(new SelectFixed(3),
                        new MutateResetLinkWeight()));
        weightMutation.getComponents().add(
                0.01,
                new NEATMutateWeights(new SelectProportion(0.02),
                        new MutateResetLinkWeight()));

        weightMutation.getComponents().finalizeStructure();
        return weightMutation;
    }

    private static Substrate makeMarioBrosSubstrate() {
//        Substrate substrate = SubstrateFactory.factorSandwichSubstrate();
        Substrate substrate = new Substrate(2);

        int gridWidth = 19;
        int gridHeight = 19;

        // make inputs
        for (int r = 0; r < gridWidth; r++ ) {
            for (int c = 0; c < gridHeight; c++) {
                SubstrateNode input = substrate.createInputNode();
                input.getLocation()[0] = c;
                input.getLocation()[1] = r;
            }
        }

        // make hidden nodes (same as input positions)
        for (int r = 0; r < gridWidth; r++ ) {
            for (int c = 0; c < gridHeight; c++) {
                SubstrateNode input = substrate.createHiddenNode();
                input.getLocation()[0] = c;
                input.getLocation()[1] = r;
            }
        }

        // make outputs
        int middleX = gridWidth/2;
        int middleY = gridHeight/2;
        double spread = 1;

        // right
        SubstrateNode right = substrate.createOutputNode();
        right.getLocation()[0] = middleX + spread;
        right.getLocation()[1] = middleY;

        // left
        SubstrateNode left = substrate.createOutputNode();
        left.getLocation()[0] = middleX - spread;
        left.getLocation()[1] = middleY;

        // jump
        SubstrateNode up = substrate.createOutputNode();
        up.getLocation()[0] = middleX;
        up.getLocation()[1] = middleY + spread;

        // speed
        SubstrateNode speed = substrate.createOutputNode();
        speed.getLocation()[0] = middleX;
        speed.getLocation()[1] = middleY;

        System.out.println(substrate);

        return substrate;
    }
}
