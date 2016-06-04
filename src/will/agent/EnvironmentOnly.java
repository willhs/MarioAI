package will.agent;

import ch.idsia.agents.IAgent;
import ch.idsia.benchmark.mario.MarioSimulator;
import ch.idsia.benchmark.mario.engine.input.MarioInput;
import ch.idsia.benchmark.mario.engine.input.MarioKey;
import ch.idsia.benchmark.mario.options.FastOpts;
import will.neat.environment.GameEnvironment;
import will.neat.environment.ValueGridEnvironment;
import com.anji.integration.Activator;
import com.anji.integration.ActivatorTranscriber;
import com.anji.persistence.Persistence;
import com.anji.util.DummyConfiguration;
import com.anji.util.Properties;
import org.jgap.Chromosome;
import org.jgap.Configuration;

/**
 * Created by Will Hardwick-Smith on 17/04/2016.
 */
public class EnvironmentOnly extends NEATAgent {

    public EnvironmentOnly(Activator activator) {
        super(activator);
    }

    @Override
    public MarioInput actionSelection() {

        // put relevant environment into form for neural net
//        GameEnvironment env = new BinaryGridEnvironment();
        GameEnvironment env = new ValueGridEnvironment();
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

    public static void main(String[] args) throws Exception {
        String propertiesFile = "mario.properties";
        String chromosomeId = "141823";

        Properties props = new Properties(propertiesFile);

        // initialise NEAT properties from mario.properties file
        Persistence db = (Persistence) props.
                singletonObjectProperty( Persistence.PERSISTENCE_CLASS_KEY );

        ActivatorTranscriber activatorFactory = (ActivatorTranscriber) props
                .singletonObjectProperty( ActivatorTranscriber.class );

        Configuration config = new DummyConfiguration();

        // load chromosome (network) and give it to this agent
        Chromosome chromosome  = db.loadChromosome(chromosomeId, config);

        Activator activator = activatorFactory.newActivator(chromosome);

        // Choose level properties
        String options = "" +
                FastOpts.VIS_ON_2X +
                FastOpts.LEVEL_03_COLLECTING
                ;

        // CREATE SIMULATOR
        MarioSimulator simulator = new MarioSimulator(options);

        // CREATE AGENT
        IAgent agent = new EnvironmentOnly(activator);

        // RUN SIMULATOR w/ AGENT
        simulator.run(agent);

        // TERMINATE
        System.exit(0);
    }
}
