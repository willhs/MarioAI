package will.mario.agent.anji;

import ch.idsia.benchmark.mario.MarioSimulator;
import ch.idsia.benchmark.mario.options.FastOpts;
import com.anji.integration.Activator;
import com.anji.integration.ActivatorTranscriber;
import com.anji.integration.TranscriberException;
import com.anji.util.Configurable;
import com.anji.util.Properties;
import org.jgap.BulkFitnessFunction;
import org.jgap.Chromosome;

import java.util.List;
/**
 * Created by Will on 17/04/2016.
 */
public class ANJIMarioFitnessFunction implements BulkFitnessFunction, Configurable {

    private float highScore;
    private ActivatorTranscriber factory;

    public static String OPTIONS = ""
            + FastOpts.VIS_OFF
//                + FastOpts.VIS_ON_2X
            //+ " " + MarioOptions.IntOption.SIMULATION_TIME_LIMIT.getParam() + " 50"
//                + " " + MarioOptions.IntOption.VISUALIZATION_FPS.getParam() + " 30"
//                + FastOpts.VIS_FIELD(SimulatorOptions.ReceptiveFieldMode.GRID)
            + FastOpts.LEVEL_02_JUMPING
            + FastOpts.S_TIME_LIMIT_200;


    @Override
    public void init(Properties props) {
        factory = (ActivatorTranscriber) props.singletonObjectProperty( ActivatorTranscriber.class );
     }

    @Override
    public void evaluate(List chromosomes) {
        for (Object chr : chromosomes) {
            Chromosome chromosome = (Chromosome) chr;
            // play mario
            if (chromosome.getId() == 10005) {
//                System.out.println(chromosome);
            }

            int fitnessValue = evaluate(chromosome);
            chromosome.setFitnessValue(fitnessValue);

            // update high reward
            if (fitnessValue > highScore) {
                highScore = fitnessValue;
            }
        }
    }

    private int evaluate(Chromosome chromosome) {

        Activator activator = null;
        try {
            activator = factory.newActivator(chromosome);
        } catch (TranscriberException e) {
            e.printStackTrace();
        }

        ANJIEnvironmentOnly agent = new ANJIEnvironmentOnly(activator);

        MarioSimulator simulator = new MarioSimulator(OPTIONS);
        simulator.run(agent);

        float fitness = agent.getFitness();
        return (int)fitness;
    }

    @Override
    public int getMaxFitnessValue() {
        return 8000;
    }

}
