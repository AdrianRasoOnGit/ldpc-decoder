package ldpc.channel;

public final class BpskModem {
    private BpskModem() {}

    public static float[] modulate(int[] bits) {
        float[] symbols = new float[bits.length];

        for (int i = 0; i < bits.length; i++) {
            symbols[i] = bits[i] == 0 ? 1.0f : -1.0f;
        }

        return symbols;
    }

    public static int[] hardDecision(float[] llr) {
        int[] bits = new int[llr.length];

        for (int i = 0; i < llr.length; i++) {
            bits[i] = llr[i] < 0.0f ? 1 : 0;
        }

        return bits;
    }
}
