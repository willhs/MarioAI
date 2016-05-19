package ch.idsia.neat.environment;

import ch.idsia.agents.controllers.modules.Tiles;
import ch.idsia.benchmark.mario.engine.input.MarioInput;
import ch.idsia.benchmark.mario.engine.input.MarioKey;
import ch.idsia.benchmark.mario.environments.IEnvironment;

/**
 * Created by Will on 15/05/2016.
 *
 * A strategy for representing the environment
 */
public interface GameEnvironment {

    public double[] getInputNeurons(IEnvironment environment, MarioInput lastInput);
}
