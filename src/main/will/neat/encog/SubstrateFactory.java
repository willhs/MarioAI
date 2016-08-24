package will.neat.encog;

import org.encog.neural.hyperneat.substrate.Substrate;

/**
 * Created by Will on 21/08/2016.
 */
public interface SubstrateFactory {

    public abstract Substrate makeSubstrate();
}
