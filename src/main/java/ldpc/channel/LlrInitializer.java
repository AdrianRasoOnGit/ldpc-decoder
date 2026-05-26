package ldpc.channel;

public final class LlrInitializer {
    private LlrInitializer() {}

    public static float[] compute(float[] received, float sigma) {
        float[] llr = new float[received.length];
        float variance = sigma * sigma;

        for (int i = 0; i < received.length; i++) {
            llr[i] = 2.0f * received[i] / variance;
        }

        return llr;
    }
}
