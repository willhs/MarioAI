package will.neat.encog.experiment;

import org.encog.neural.hyperneat.substrate.Substrate;
import will.mario.agent.encog.AgentFactory;
import will.mario.agent.encog.EncogAgentFS;
import will.neat.AbstractMarioFitnessFunction;
import will.neat.encog.substrate.MultiHiddenLayerSubstrateFS;
import will.neat.params.HyperNEATParameters;
import will.neat.params.HyperNEATParametersPSO;
import will.rf.action.ActionStratFactory;
import will.rf.action.StandardHoldStrat;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by Will on 9/10/2016.
 */
public class HyperNEATMarioEvolverFS extends HyperNEATMarioEvolver {
    private Point[] inputs;

    public HyperNEATMarioEvolverFS(HyperNEATParameters params, ActionStratFactory actionStratFactory,
                                   StringBuilder output, String name) {
        super(params, actionStratFactory, output, name);
    }

    public HyperNEATMarioEvolverFS(HyperNEATParameters params, ActionStratFactory actionStratFactory) {
        super(params, actionStratFactory);
    }

    public void setInputs(Point[] inputs) {
        this.inputs = inputs;
    }

    @Override
    protected Substrate setupSubstrate() {
        return new MultiHiddenLayerSubstrateFS(inputs).makeSubstrate();
    }

    @Override
    protected AgentFactory setupAgent(ActionStratFactory stratFactory) {
        return (nn) -> new EncogAgentFS(nn, stratFactory, inputs);
    }


    public static void main(String[] args) throws IOException {
        HyperNEATMarioEvolverFS evolve = new HyperNEATMarioEvolverFS(
                new HyperNEATParametersPSO(), () -> new StandardHoldStrat());

        evolve.setInputs(new Point[] { new Point(0,0), new Point(8,8) });
        evolve.setSimOptions(AbstractMarioFitnessFunction.DEFAULT_SIM_OPTIONS);

        evolve.run();
    }
}
