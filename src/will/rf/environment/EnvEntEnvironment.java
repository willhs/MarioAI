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

import static ch.idsia.benchmark.mario.engine.input.MarioKey.getKeys;

/**
 * Created by Will on 15/07/2016.
 */
public class EnvEntEnvironment implements GridEnvironment {
    @Override
    public double[] getInputNeurons(IEnvironment environment, MarioInput lastInput) {
        // convert 2d tiles to input neurons
        List<Double> environmentGrid = Arrays.stream(environment.getTileField())
                .flatMap(tileRow -> {
                    return Arrays.stream(tileRow)
                            .map(tile -> {
                                // 0 if nothing in tile or 1 if something
                                return tile == Tile.NOTHING ? 0 : 1;
                            });
                })
                .mapToDouble(i->i)
                .boxed()
                .collect(Collectors.toList());

        // add entities
        List<Double> entityGrid = Arrays.stream(environment.getEntityField())
                .flatMap(entityRow -> {
                    return Arrays.stream(entityRow)
                            .map(entities -> {
                                // 0 if tile has no entities, 1 if it does
                                return entities.isEmpty() ||
                                    entities.stream().allMatch(entity -> entity.type == EntityType.NOTHING)
                                        ? 0 : 1;
                            });
                })
                .mapToDouble(i->i)
                .boxed()
                .collect(Collectors.toList());

        // add the last action as a neurons
        // one input for each different key
        List<Double> lastActions = MarioKey.getKeys().stream()
                .map(key -> lastInput.getPressed().contains(key) ? 1 : 0 )
                .mapToDouble(d->d)
                .boxed()
                .collect(Collectors.toList());

        List<Double> inputNeurons = new ArrayList<>();
        inputNeurons.addAll(environmentGrid);
        inputNeurons.addAll(entityGrid);
        inputNeurons.addAll(lastActions);

        return inputNeurons.stream()
                .mapToDouble(d -> d)
                .toArray();
    }
}
