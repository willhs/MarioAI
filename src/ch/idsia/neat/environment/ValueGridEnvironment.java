package ch.idsia.neat.environment;

import ch.idsia.agents.controllers.modules.Tiles;
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
 * Represents environment as tiles - MIN_VAL if nothing, a unique integer for each other tile
 */
public class ValueGridEnvironment implements GridEnvironment{

    @Override
    public double[] getInputNeurons(IEnvironment environment, MarioInput lastInput) {
        // convert 2d tiles to input neurons
        List<Double> inputNeurons = Arrays.stream(environment.getTileField())
                .flatMap(tileRow -> {
                    return Arrays.stream(tileRow)
                            .map(tile -> {
                                // represent unique tiles
                                return tile == Tile.NOTHING ? Integer.MIN_VALUE : tile.ordinal();
                            });
                })
                .mapToDouble(i->i)
                .boxed()
                .collect(Collectors.toList());

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

