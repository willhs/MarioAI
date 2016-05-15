package ch.idsia.neat.environment;

import ch.idsia.agents.controllers.modules.Tiles;
import ch.idsia.benchmark.mario.engine.generalization.Tile;
import ch.idsia.benchmark.mario.engine.input.MarioInput;
import ch.idsia.benchmark.mario.environments.IEnvironment;

import java.util.Arrays;

/**
 * Created by Will on 15/05/2016.
 *
 * Represents environment as tiles - MIN_VAL if nothing, a unique integer for each other tile
 */
public class ValueGridEnvironment implements GridEnvironment{

    @Override
    public double[] getInputNeurons(IEnvironment environment, MarioInput lastInput) {
        // convert 2d tiles to input neurons
        double[] neurons = Arrays.stream(environment.getTileField())
                .flatMap(tileRow -> {
                    return Arrays.stream(tileRow)
                            .map(tile -> {
                                // represent unique tiles
                                return tile == Tile.NOTHING ? Integer.MIN_VALUE : tile.ordinal();
                            });
                })
                .mapToDouble(i->i)
                .toArray();

        return neurons;
    }
}

