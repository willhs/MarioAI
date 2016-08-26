package will.neat.encog;

import org.encog.neural.hyperneat.substrate.Substrate;
import org.encog.neural.hyperneat.substrate.SubstrateNode;

/**
 * Created by Will on 26/08/2016.
 */
public class SandwichHiddenLayer2Frames extends SandwichHiddenLayer{

    @Override
    public Substrate makeSubstrate() {
        Substrate substrate = new Substrate(3);

        int gridWidthInput = 19;
        int gridHeightInput = 19;

        int gridWidthHidden = 5;
        int gridHeightHidden = 5;

        double hypercubeSize = 2;

        double xTickInput = hypercubeSize / gridWidthInput;
        double yTickInput = hypercubeSize / gridHeightInput;
        double xStartInput = -1 + xTickInput/2;
        double yStartInput = -1 + yTickInput/2;

        double xTickHidden = hypercubeSize / gridWidthInput; // temporarily using input width/height
        double yTickHidden = hypercubeSize / gridHeightInput;
        double xStartHidden = -(gridWidthHidden/(double)gridWidthInput) + xTickHidden/2;
        double yStartHidden = -(gridHeightHidden/(double)gridHeightInput) + yTickHidden/2;

        // make inputs
        for (int r = 0; r < gridWidthInput; r++ ) {
            for (int c = 0; c < gridHeightInput; c++) {
                SubstrateNode input = substrate.createInputNode();
                input.getLocation()[0] = xStartInput + (c * xTickInput);
                input.getLocation()[1] = yStartInput + (r * yTickInput);
                input.getLocation()[2] = -1;
            }
        }

        // make inputs
        for (int r = 0; r < gridWidthInput; r++ ) {
            for (int c = 0; c < gridHeightInput; c++) {
                SubstrateNode input = substrate.createInputNode();
                input.getLocation()[0] = xStartInput + (c * xTickInput);
                input.getLocation()[1] = yStartInput + (r * yTickInput);
                input.getLocation()[2] = -0.8;
            }
        }

        // make hidden nodes
        for (int r = 0; r < gridWidthHidden; r++ ) {
            for (int c = 0; c < gridHeightHidden; c++) {
                SubstrateNode hidden = substrate.createHiddenNode();
                hidden.getLocation()[0] = xStartHidden + (c * xTickHidden);
                hidden.getLocation()[1] = yStartHidden + (r * yTickHidden);
                hidden.getLocation()[2] = 0;

                // create connections to all input nodes
                substrate.getInputNodes().forEach(input -> substrate.createLink(input, hidden));
            }
        }

        // make outputs
        int middleX = 0;
        int middleY = 0;
        double variance = 1; // how far the node should vary from the centre

        int controls = 4;

        // coordinates for controls in order: left, right, jump, speed
        double[] xs = {
                middleX - variance,
                middleX + variance,
                middleX,
                middleX
        };
        double[] ys = {
                middleY,
                middleY,
                middleY - variance,
                middleY
        };

        for (int i = 0; i < controls; i++) {
            SubstrateNode output = substrate.createOutputNode();
            output.getLocation()[0] = xs[i];
            output.getLocation()[1] = ys[i];
            output.getLocation()[2] = 1;

            // create connections to all input nodes
            substrate.getHiddenNodes().forEach(hidden -> substrate.createLink(hidden, output));
        }

//        int inputs = substrate.getInputCount();
//        int hiddens = substrate.getHiddenNodes().size();
//
//        System.out.println(inputs + " " + hiddens);
//        System.out.println("links: " + substrate.getLinkCount());

        return substrate;
    }
}
