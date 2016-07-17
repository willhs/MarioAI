package will.rf.environment;

import ch.idsia.benchmark.mario.engine.generalization.EntityType;
import ch.idsia.benchmark.mario.engine.generalization.Tile;
import ch.idsia.benchmark.mario.engine.input.MarioInput;
import ch.idsia.benchmark.mario.engine.input.MarioKey;
import ch.idsia.benchmark.mario.environments.IEnvironment;
import org.neuroph.core.NeuralNetwork;
import will.mario.agent.neuroph.NEATAgent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.idsia.benchmark.mario.engine.input.MarioKey.getKeys;

/**
 * Created by Will on 15/07/2016.
 */
public class EnvEntEnvironment extends AbstractGridEnvironment {
    @Override
    public double[] asInputNeurons(IEnvironment environment, MarioInput lastInput) {
        // convert 2d tiles to input neurons
        double[] environmentGrid = envGridToBinaryArray(environment.getTileField());

        // add entities
        double[] entityGrid = entityGridToBinaryArray(environment.getEntityField());

        // add the last action as a neurons
        double[] lastMarioActions = lastMarioActionsToArray(lastInput);

        // concatenated
        return Stream.concat(
                Stream.concat(
                    Arrays.stream(environmentGrid).boxed(),
                    Arrays.stream(entityGrid).boxed()
                ),
                Arrays.stream(lastMarioActions).boxed()
            )
            .mapToDouble(d->d)
            .toArray();
    }
}
