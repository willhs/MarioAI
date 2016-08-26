package will.rf.environment;

import ch.idsia.benchmark.mario.engine.generalization.EntityType;
import ch.idsia.benchmark.mario.engine.generalization.Tile;
import ch.idsia.benchmark.mario.engine.input.MarioInput;
import ch.idsia.benchmark.mario.environments.IEnvironment;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Will on 26/08/2016.
 */
public class EnvEnemyGrid implements GridEnvironment{
    @Override
    public double[] asInputNeurons(IEnvironment environment, MarioInput lastInput) {
        // convert 2d tiles to input neurons
        List<Double> envGrid = Arrays.stream(environment.getTileField())
                .flatMap(tileRow -> {
                    return Arrays.stream(tileRow)
                            .map(tile -> {
                                // represent unique tiles
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
                                // -1 if tile has no entities, otherwise unique number for each type
                                return entities.isEmpty()
                                        || entities.stream().allMatch(entity -> entity.type == EntityType.NOTHING)
                                        ? 0
                                        : -1;
                            });
                })
                .mapToDouble(i->i)
                .boxed()
                .collect(Collectors.toList());

        double[] combined = new double[entityGrid.size()];

        for (int i = 0; i < entityGrid.size(); i++) {
            combined[i] = envGrid.get(i) == 0 ? entityGrid.get(i) : envGrid.get(i);
        }

        return combined;
    }
}
