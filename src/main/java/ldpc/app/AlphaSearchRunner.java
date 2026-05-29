package ldpc.app;

import ldpc.decoder.NormalizedMinSumDecoder;
import ldpc.matrix.CsrMatrix;
import ldpc.matrix.HMatrixLoader;
import ldpc.simulation.BerSimulation;
import ldpc.simulation.DecoderFactory;
import ldpc.simulation.SimulationConfig;
import ldpc.simulation.SimulationResult;
import ldpc.util.CsvWriter;

import java.nio.file.Path;
import java.util.List;

public final class AlphaSearchRunner {
    public static void main(String[] args) throws Exception {
        CsrMatrix h = HMatrixLoader.loadResource("/matrices/toy/h_3x6.txt");

        double[] alphaValues = {
                0.50, 0.60, 0.70, 0.75, 0.80, 0.90, 1.00
        };

        SimulationConfig config = new SimulationConfig(
                0.5,
                10_000,
                20,
                new double[] {0.0, 1.0, 2.0, 3.0, 4.0},
                1234L
        );

        double bestAlpha = alphaValues[0];
        long bestBitErrors = Long.MAX_VALUE;
        long bestFrameErrors = Long.MAX_VALUE;

        for (double alpha : alphaValues) {
            DecoderFactory factory =
                    (matrix, maxIterations) ->
                            new NormalizedMinSumDecoder(
                                    matrix,
                                    maxIterations,
                                    (float) alpha
                            );

            BerSimulation simulation = new BerSimulation(h, config, factory);
            List<SimulationResult> results = simulation.run();

            Path output = Path.of(
                    "results/ber/ber_nms_alpha_" + alphaName(alpha) + ".csv"
            );

            CsvWriter.writeBerResults(output, results);

            long bitErrors = totalBitErrors(results);
            long frameErrors = totalFrameErrors(results);

            System.out.printf(
                    "alpha %.2f | total bit errors %d | total frame errors %d%n",
                    alpha,
                    bitErrors,
                    frameErrors
            );

            if (
                    bitErrors < bestBitErrors ||
                    (bitErrors == bestBitErrors && frameErrors < bestFrameErrors)
            ) {
                bestAlpha = alpha;
                bestBitErrors = bitErrors;
                bestFrameErrors = frameErrors;
            }
        }

        System.out.println();
        System.out.printf(
                "Best alpha: %.2f | bit errors %d | frame errors %d%n",
                bestAlpha,
                bestBitErrors,
                bestFrameErrors
        );
    }

    private static long totalBitErrors(List<SimulationResult> results) {
        long total = 0;

        for (SimulationResult result : results) {
            total += result.bitErrors();
        }

        return total;
    }

    private static long totalFrameErrors(List<SimulationResult> results) {
        long total = 0;

        for (SimulationResult result : results) {
            total += result.frameErrors();
        }

        return total;
    }

    private static String alphaName(double alpha) {
        return String.format("%.2f", alpha).replace(".", "_");
    }
}
