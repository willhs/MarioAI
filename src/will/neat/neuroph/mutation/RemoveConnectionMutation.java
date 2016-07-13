package will.neat.neuroph.mutation;

import org.neuroph.contrib.neat.gen.*;
import org.neuroph.contrib.neat.gen.operations.mutation.AbstractMutationOperation;

import java.util.Set;

/**
 * Created by Will on 3/07/2016.
 */
public class RemoveConnectionMutation extends AbstractMutationOperation {
    /**
     * Creates a new <code>MutationOperation</code> with the provided
     * probability of occurring.
     *
     * @param mutationProbability the probability that this <codE>MutationOperation</codE>
     *                            should be applied.
     */
    public RemoveConnectionMutation(double mutationProbability) {
    super(mutationProbability);
    }

    @Override
    protected boolean mutate(NeatParameters neatParameters, Innovations innovations, FitnessScores fitnessScores, Organism o, Set<Gene> genesToAdd, Set<Gene> genesToRemove, int generationNumber) {
        if (!shouldMutate(neatParameters, getMutationProbability())
                || innovations.getConnectionGenes().isEmpty()) {
            return false;
        }

        int index = (int)(neatParameters.getRandomGenerator().nextDouble() * innovations.getConnectionGenes().size());
        ConnectionGene connection = innovations.getConnectionGenes().get(index);
        genesToRemove.add(connection);
        return true;
    }

    @Override
    protected int maxNumOfMutationsToPerform(Organism o) {
        return o.getConnections().size();
    }
}
