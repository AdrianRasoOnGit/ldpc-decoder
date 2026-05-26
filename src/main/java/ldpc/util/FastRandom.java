package ldpc.util;

import java.util.Random;

public final class FastRandom {
    private final Random random;

    public FastRandom(long seed) {
        this.random = new Random(seed);
    }

    public float nextGaussian(float sigma) {
        return (float) (random.nextGaussian() * sigma);
    }
}
