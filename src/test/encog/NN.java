package encog;

import org.encog.engine.network.activation.ActivationFunction;
import org.encog.engine.network.activation.ActivationLinear;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.neat.NEATLink;
import org.encog.neural.neat.NEATNetwork;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Will on 24/08/2016.
 */
public class NN {

    @Test
    public void inputsMapToOutputs() {
        List<NEATLink> links = new ArrayList<>();
        ActivationFunction[] functions = new ActivationFunction[5];
        Arrays.fill(functions, new ActivationLinear());
        links.add(new NEATLink(0,3,1));

        NEATNetwork nn = new NEATNetwork(2, 2, links, functions);

        MLData data = nn.compute(new BasicMLData(new double[]{1, 0}));

        assertTrue(Arrays.equals(data.getData(), new double[]{1, 0}));
    }

}
