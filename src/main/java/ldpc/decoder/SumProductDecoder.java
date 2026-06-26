package ldpc.decoder;

import ldpc.matrix.CsrMatrix;

import java.util.Arrays;

public final class SumProductDecoder implements LdpcDecoder {
    private static final float EPS = 1e-6f;

    private final CsrMatrix h;
    private final int maxIterations;

    private final int[] edgeToVar;
    private final int[][] varToEdges;

    private final float[] q;
    private final float[] r;
    private final float[] posterior;

    public SumProductDecoder(CsrMatrix h, int maxIterations) {
        if (maxIterations <= 0) {
            throw new IllegalArgumentException("maxIterations must be positive");
        }

        this.h = h;
        this.maxIterations = maxIterations;

        int edgeCount = h.edgeCount();
        this.edgeToVar = new int[edgeCount];
        this.q = new float[edgeCount];
        this.r = new float[edgeCount];
        this.posterior = new float[h.cols()];

        int[] degree = new int[h.cols()];

        for (int row = 0; row < h.rows(); row++) {
            for (int edge = h.rowStart(row); edge < h.rowEnd(row); edge++) {
                int var = h.colIndex(edge);
                edgeToVar[edge] = var;
                degree[var]++;
            }
        }

        this.varToEdges = new int[h.cols()][];

        for (int v = 0; v < h.cols(); v++) {
            varToEdges[v] = new int[degree[v]];
        }

        int[] cursor = new int[h.cols()];

        for (int edge = 0; edge < edgeCount; edge++) {
            int var = edgeToVar[edge];
            varToEdges[var][cursor[var]++] = edge;
        }
    }

    @Override
    public DecodeResult decode(float[] channelLlr) {
        if (channelLlr.length != h.cols()) {
            throw new IllegalArgumentException("LLR length must equal matrix columns");
        }

        Arrays.fill(r, 0.0f);

        for (int edge = 0; edge < q.length; edge++) {
            q[edge] = channelLlr[edgeToVar[edge]];
        }

        int[] hardBits = new int[h.cols()];

        for (int iteration = 1; iteration <= maxIterations; iteration++) {
            checkToVariable();
            variableToCheck(channelLlr);
            computePosterior(channelLlr);

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

    private void checkToVariable() {
        for (int row = 0; row < h.rows(); row++) {
            int start = h.rowStart(row);
            int end = h.rowEnd(row);

            for (int target = start; target < end; target++) {
                double product = 1.0;

                for (int edge = start; edge < end; edge++) {
                    if (edge == target) {
                        continue;
                    }

                    double value = Math.tanh(q[edge] / 2.0);
                    value = clamp(value, -1.0 + EPS, 1.0 - EPS);
                    product *= value;
                }

                product = clamp(product, -1.0 + EPS, 1.0 - EPS);
                r[target] = (float) (2.0 * atanh(product));
            }
        }
    }

    private void variableToCheck(float[] channelLlr) {
        for (int v = 0; v < h.cols(); v++) {
            float sum = channelLlr[v];

            for (int edge : varToEdges[v]) {
                sum += r[edge];
            }

            for (int edge : varToEdges[v]) {
                q[edge] = sum - r[edge];
            }
        }
    }

    private void computePosterior(float[] channelLlr) {
        System.arraycopy(channelLlr, 0, posterior, 0, channelLlr.length);

        for (int v = 0; v < h.cols(); v++) {
            for (int edge : varToEdges[v]) {
                posterior[v] += r[edge];
            }
        }
    }

    private static double atanh(double x) {
        return 0.5 * Math.log((1.0 + x) / (1.0 - x));
    }

    private static double clamp(double x, double lo, double hi) {
        return Math.max(lo, Math.min(hi, x));
    }
}
