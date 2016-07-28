package will.util;

import org.encog.engine.network.activation.ActivationFunction;
import org.encog.engine.network.activation.ActivationLinear;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.neat.NEATLink;
import org.encog.neural.neat.NEATNetwork;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Will on 28/07/2016.
 */
public class Test {
    public static void main(String[] args) {
        List<NEATLink> links = new ArrayList<>();
        ActivationFunction[] functions = new ActivationFunction[5];
        Arrays.fill(functions, new ActivationLinear());
        links.add(new NEATLink(4,0,1));

        NEATNetwork nn = new NEATNetwork(2, 2, links, functions);

        MLData data = nn.compute(new BasicMLData(new double[]{ 1,0 }));

        System.out.println("data:");
        System.out.println(Arrays.toString(data.getData()));
    }
}
