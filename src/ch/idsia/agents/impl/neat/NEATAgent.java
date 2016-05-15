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

    private final Activator activator;

    public NEATAgent(Activator activator) {
        this.activator = activator;
    }

    @Override
    public void reset(AgentOptions options) {
        super.reset(options);
    }

    @Override
    public MarioInput actionSelection() {
        // convert 2d tiles to 1d array with binary representation (tile or no tile).
        double[] tiles = Arrays.stream(this.tiles.tileField)
                .flatMap(tileRow -> {
                    return Arrays.stream(tileRow)
                        .map(tile -> {
                            // represent unique tiles
                            //return tile.ordinal();
                            return tile == Tile.NOTHING ? 0 : 1;
                        });
                    })
                .mapToDouble(i->i)
                .toArray();


        // put tiles through the neural network to receive game inputs
        // 1 or 0 for each of the game inputs: [left,right,down,jump,speed/attack,up(useless)]
        double[] networkOutput = activator.next(tiles);

//        System.out.println("networkInput: " + Arrays.toString(tiles));
//        System.out.println("networkOutput: " + Arrays.toString(networkOutput));

        for (int i = 0; i < networkOutput.length; i++) {
            // output >= 0 == press key, <= 0 == don't press
            action.set(MarioKey.getMarioKey(i), networkOutput[i] >= 0.5);
        }
        return action;
    }

    public int getFitness() {
        return highestFitness;
    }
}
