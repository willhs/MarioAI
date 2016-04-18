package ch.idsia.agents.impl.neat;

import ch.idsia.agents.AgentOptions;
import ch.idsia.agents.controllers.MarioAIBase;
import ch.idsia.benchmark.mario.engine.generalization.Tile;
import ch.idsia.benchmark.mario.engine.input.MarioInput;
import ch.idsia.benchmark.mario.engine.input.MarioKey;
import com.anji.integration.Activator;
import com.anji.integration.ActivatorTranscriber;
import com.anji.integration.TranscriberException;
import org.jgap.Chromosome;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Created by Will on 17/04/2016.
 */
public class NEATAgent extends MarioAIBase {

    private Chromosome chromosome;

    public NEATAgent(Chromosome chromosome) {
        this.chromosome = chromosome;
    }

    @Override
    public void reset(AgentOptions options) {
        super.reset(options);
    }

    public MarioInput actionSelectionAI() {
        // make decision
        ActivatorTranscriber factory = new ActivatorTranscriber();
        Activator activator = null;
        try {
            activator = factory.newActivator(chromosome);
        } catch (TranscriberException e) {
            e.printStackTrace();
        }

        // convert tiles to binary representation (tile or no tile).
        double[] tiles = Arrays.stream(this.tiles.tileField)
                .flatMap(tileRow -> {
                    return Arrays.stream(tileRow)
                        .map(tile -> {
                            //return tile.ordinal();
                            return tile == Tile.NOTHING ? 0 : 1;
                        });
                    })
                .mapToDouble(i->i)
                .toArray();

        // 1 or 0 for each of the game inputs: [left,right,down,jump,speed/attack,up(useless)]
        double[] networkOutput = activator.next(tiles);

        for (int i = 0; i < networkOutput.length; i++) {
            // output >= 0 == press key, <= 0 == don't press
            action.set(MarioKey.getMarioKey(i), networkOutput[i] >= 0);
        }
        return action;
    }

    public int getFitness() {
        return highestFitness;
    }
}
