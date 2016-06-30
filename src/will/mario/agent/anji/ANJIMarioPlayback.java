package will.mario.agent.anji;

import ch.idsia.agents.IAgent;
import ch.idsia.benchmark.mario.MarioSimulator;
import ch.idsia.benchmark.mario.options.FastOpts;
import com.anji.integration.Activator;
import com.anji.integration.ActivatorTranscriber;
import com.anji.persistence.Persistence;
import com.anji.util.DummyConfiguration;
import com.anji.util.Properties;
import org.jgap.Chromosome;
import org.jgap.Configuration;

import javax.swing.*;

/**
 * Created by Will on 9/06/2016.
 */
public class ANJIMarioPlayback {

    public static void main(String[] args) throws Exception {
        String propertiesFile = "mario.properties";
        String chromosomeId = "141823";
        chromosomeId = JOptionPane.showInputDialog("chromosome id");

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

        String OPTIONS = ""
//                + FastOpts.VIS_OFF
                + FastOpts.VIS_ON_2X
                //+ " " + MarioOptions.IntOption.SIMULATION_TIME_LIMIT.getParam() + " 50"
//                + " " + MarioOptions.IntOption.VISUALIZATION_FPS.getParam() + " 30"
//                + FastOpts.VIS_FIELD(SimulatorOptions.ReceptiveFieldMode.GRID)
                + FastOpts.LEVEL_02_JUMPING
                + FastOpts.S_TIME_LIMIT_200;


        // CREATE SIMULATOR
        MarioSimulator simulator = new MarioSimulator(OPTIONS);

        // CREATE AGENT
        IAgent agent = new ANJIEnvironmentOnly(activator);

        // RUN SIMULATOR w/ AGENT
        simulator.run(agent);

        // repeat
        //main(args);
        System.exit(0);
    }
}
