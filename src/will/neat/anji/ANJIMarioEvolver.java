package will.neat.anji;

import com.anji.neat.Evolver;
import com.anji.util.Properties;

/**
 * Created by Will on 28/06/2016.
 */
public class ANJIMarioEvolver {

    public static void main(String[] args) throws Throwable {

        Properties props;

        if (args.length >= 1) {
            props = new Properties(args[0]);
        } else {
            String propertiesFilename = "mario.properties";
            props = new Properties(propertiesFilename);
        }

        // initialise evolver
        Evolver evolver = new Evolver();
        evolver.init(props);
        evolver.run();
    }
}