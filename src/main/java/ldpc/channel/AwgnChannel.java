package ldpc.channel;

import java.util.Random;

public final class AwgnChannel {
    private final Random random;

    public AwgnChannel(long seed) {
        this.random = new Random(seed);
    }

    public float[] transmit(float[] symbols, float sigma) {
        float[] received = new float[symbols.length];

        for (int i = 0; i < symbols.length; i++) {
            received[i] = symbols[i] + (float) (random.nextGaussian() * sigma);
        }

        return received;
    }
}
