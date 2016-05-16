package ch.idsia.agents.impl.neat;

import ch.idsia.agents.AgentOptions;
import ch.idsia.agents.IAgent;
import ch.idsia.agents.controllers.MarioAIBase;
import ch.idsia.agents.controllers.MarioAIBase2;
import ch.idsia.benchmark.mario.MarioSimulator;
import ch.idsia.benchmark.mario.engine.generalization.Tile;
import ch.idsia.benchmark.mario.engine.input.MarioInput;
import ch.idsia.benchmark.mario.engine.input.MarioKey;
import ch.idsia.benchmark.mario.environments.MarioEnvironment;
import ch.idsia.benchmark.mario.options.FastOpts;
import ch.idsia.neat.environment.BinaryGridEnvironment;
import ch.idsia.neat.environment.GameEnvironment;
import com.anji.integration.Activator;
import com.anji.integration.ActivatorTranscriber;
import com.anji.integration.TranscriberException;
import com.anji.neat.NeatActivator;
import com.anji.persistence.Persistence;
import com.anji.util.DummyConfiguration;
import com.anji.util.Properties;
import org.jgap.Chromosome;
import org.jgap.Configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Created by Will Hardwick-Smith on 17/04/2016.
 */
public class NEATAgent extends MarioAIBase2 {

    private final Activator activator;

    public NEATAgent(Activator activator) {
        this.activator = activator;
    }

    @Override
    public MarioInput actionSelection() {

        GameEnvironment env = new BinaryGridEnvironment();

        double[] inputs = env.getInputNeurons(environment, lastInput);

        // put tiles through the neural network to receive game inputs
        // 1 or 0 for each of the game inputs: [left,right,down,jump,speed/attack,up(useless)]
        double[] networkOutput = activator.next(inputs);

        MarioInput action = new MarioInput();

        for (int i = 0; i < networkOutput.length; i++) {
            // output >= 0 == press key, <= 0 == don't press
            action.set(MarioKey.getMarioKey(i), networkOutput[i] >= 0.5);
        }

        // todo: put this in parent class somehow
        lastInput = action;

        return action;
    }

    public int getFitness() {
        return highestFitness;
    }

    public static void main(String[] args) throws Exception {
        String propertiesFile = "mario.properties";
        String chromosomeId = "109204";

        Properties props = new Properties(propertiesFile);

        // initialise NEAT properties from properties file
        Persistence db = (Persistence) props.
                singletonObjectProperty( Persistence.PERSISTENCE_CLASS_KEY );

        Configuration config = new DummyConfiguration();

        Chromosome chromosome  = db.loadChromosome(chromosomeId, config);

        ActivatorTranscriber activatorFactory = (ActivatorTranscriber) props
                .singletonObjectProperty( ActivatorTranscriber.class );

        Activator activator = activatorFactory.newActivator(chromosome);

        // USE FLAT LEVEL WITHOUT ENEMIES
        String options = "" +
                FastOpts.VIS_ON_2X +
                FastOpts.LEVEL_02_JUMPING;

        // CREATE SIMULATOR
        MarioSimulator simulator = new MarioSimulator(options);

        // CREATE AGENT
        IAgent agent = new NEATAgent(activator);

        // RUN SIMULATOR w/ AGENT
        simulator.run(agent);

        // TERMINATE
        System.exit(0);
    }
}
