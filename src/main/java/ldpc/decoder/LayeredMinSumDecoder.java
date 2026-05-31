package ldpc.decoder;

import ldpc.matrix.CsrMatrix;

import java.util.Arrays;

public final class LayeredMinSumDecoder implements LdpcDecoder {
    private final CsrMatrix h;
    private final int maxIterations;
    private final float alpha;

    private final int[] edgeToVar;
    private final float[] r;
    private final float[] posterior;

    public LayeredMinSumDecoder(
            CsrMatrix h,
            int maxIterations,
            float alpha
    ) {
        if (maxIterations <= 0) {
            throw new IllegalArgumentException("maxIterations must be positive");
        }

        if (alpha <= 0.0f || alpha > 1.0f) {
            throw new IllegalArgumentException("alpha must be in (0, 1]");
        }

        this.h = h;
        this.maxIterations = maxIterations;
        this.alpha = alpha;

        int edgeCount = h.edgeCount();

        this.edgeToVar = new int[edgeCount];
        this.r = new float[edgeCount];
        this.posterior = new float[h.cols()];

        for (int row = 0; row < h.rows(); row++) {
            for (int edge = h.rowStart(row); edge < h.rowEnd(row); edge++) {
                edgeToVar[edge] = h.colIndex(edge);
            }
        }
    }

    @Override
    public DecodeResult decode(float[] channelLlr) {
        if (channelLlr.length != h.cols()) {
            throw new IllegalArgumentException("LLR length must equal matrix columns");
        }

        Arrays.fill(r, 0.0f);
        System.arraycopy(channelLlr, 0, posterior, 0, channelLlr.length);

        int[] hardBits = new int[h.cols()];

        for (int iteration = 1; iteration <= maxIterations; iteration++) {

            for (int row = 0; row < h.rows(); row++) {
                updateLayer(row);
            }

            for (int v = 0; v < h.cols(); v++) {
                hardBits[v] = posterior[v] < 0.0f ? 1 : 0;
            }

            if (SyndromeChecker.isValid(h, hardBits)) {
                return new DecodeResult(
                        Arrays.copyOf(hardBits, hardBits.length),
                        iteration,
                        true
                );
            }
        }

        return new DecodeResult(
                Arrays.copyOf(hardBits, hardBits.length),
                maxIterations,
                false
        );
    }

    private void updateLayer(int row) {
        int start = h.rowStart(row);
        int end = h.rowEnd(row);

        float min1 = Float.POSITIVE_INFINITY;
        float min2 = Float.POSITIVE_INFINITY;
        int min1Edge = -1;
        int signProduct = 1;

        for (int edge = start; edge < end; edge++) {
            int var = edgeToVar[edge];

            float extrinsic = posterior[var] - r[edge];

            if (extrinsic < 0.0f) {
                signProduct = -signProduct;
            }

            float abs = Math.abs(extrinsic);

            if (abs < min1) {
                min2 = min1;
                min1 = abs;
                min1Edge = edge;
            } else if (abs < min2) {
                min2 = abs;
            }
        }

        for (int edge = start; edge < end; edge++) {
            int var = edgeToVar[edge];

            float extrinsic = posterior[var] - r[edge];

            int sign = signProduct;

            if (extrinsic < 0.0f) {
                sign = -sign;
            }

            float magnitude =
                    edge == min1Edge
                            ? min2
                            : min1;

            float newMessage =
                    alpha * sign * magnitude;

            posterior[var] =
                    extrinsic + newMessage;

            r[edge] = newMessage;
        }
    }
}
