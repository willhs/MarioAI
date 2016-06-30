package will.mario.environment;

import ch.idsia.benchmark.mario.engine.generalization.Tile;
import ch.idsia.benchmark.mario.engine.input.MarioInput;
import ch.idsia.benchmark.mario.engine.input.MarioKey;
import ch.idsia.benchmark.mario.environments.IEnvironment;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Will on 15/05/2016.
 *
 * Represents environment as binary tiles - 0 if nothing, 1 if something
 */
public class BinaryGridEnvironment implements GridEnvironment{

    @Override
    public double[] getInputNeurons(IEnvironment environment, MarioInput lastInput) {
        // convert 2d tiles to input neurons
        List<Double> inputNeurons = Arrays.stream(environment.getTileField())
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
/*        inputNeurons.addAll(Arrays.stream(environment.getEntityField())
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
                .collect(Collectors.toList())
        );*/

        // add the last action as a neurons
        // one input for each different key
        MarioKey.getKeys().forEach(key -> {
            int pressed  = lastInput.getPressed().contains(key) ? 1 : 0;
            inputNeurons.add((double)pressed);
        });

        return inputNeurons.stream()
            .mapToDouble(d -> d)
            .toArray();
    }
}
