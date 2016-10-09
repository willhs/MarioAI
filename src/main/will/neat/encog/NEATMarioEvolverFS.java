package will.neat.encog;

import org.encog.ml.CalculateScore;
import org.encog.neural.hyperneat.substrate.Substrate;
import org.encog.neural.neat.NEATNetwork;
import will.mario.agent.NEATAgent;
import will.mario.agent.encog.AgentFactory;
import will.mario.agent.encog.EncogAgent;
import will.mario.agent.encog.EncogAgentFS;
import will.neat.AbstractMarioFitnessFunction;
import will.neat.encog.substrate.MultiHiddenLayerSubstrateFS;
import will.neat.params.HyperNEATParameters;
import will.neat.params.HyperNEATParametersPSO;
import will.neat.params.NEATParameters;
import will.rf.action.ActionStratFactory;
import will.rf.action.StandardHoldStrat;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by Will on 9/10/2016.
 */
public class NEATMarioEvolverFS extends HyperNEATMarioEvolver {
    private Point[] inputs;

    public NEATMarioEvolverFS(HyperNEATParameters params, ActionStratFactory actionStratFactory,
                              Path path, String name) {
        super(params, actionStratFactory, path, name);
    }

    public NEATMarioEvolverFS(HyperNEATParameters params, ActionStratFactory actionStratFactory) {
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
        NEATMarioEvolverFS evolve = new NEATMarioEvolverFS(
                new HyperNEATParametersPSO(), () -> new StandardHoldStrat());

        evolve.setInputs(new Point[] { new Point(0,0) });
        evolve.setSimOptions(AbstractMarioFitnessFunction.DEFAULT_SIM_OPTIONS);

        evolve.run();
    }
}
