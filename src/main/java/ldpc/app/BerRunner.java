package ldpc.app;

import ldpc.decoder.MinSumDecoder;
import ldpc.decoder.NormalizedMinSumDecoder;
import ldpc.decoder.OffsetMinSumDecoder;
import ldpc.matrix.CsrMatrix;
import ldpc.matrix.HMatrixLoader;
import ldpc.matrix.RegularLdpcMatrixFactory;
import ldpc.simulation.BerSimulation;
import ldpc.simulation.DecoderFactory;
import ldpc.simulation.SimulationConfig;
import ldpc.simulation.SimulationResult;
import ldpc.util.CsvWriter;

import java.nio.file.Path;
import java.util.List;

public final class BerRunner {

    private static final String TOY_MATRIX_RESOURCE =
            "/matrices/toy/h_3x6.txt";

    private BerRunner() {}

    public static void main(String[] args) throws Exception {

        String decoderName =
                args.length > 0
                        ? args[0].toLowerCase()
                        : "normalized";

        String matrixName =
                args.length > 1
                        ? args[1].toLowerCase()
                        : "toy";

        CsrMatrix h =
                loadMatrix(matrixName);

        double codeRate =
                estimateCodeRate(h);

        SimulationConfig config =
                new SimulationConfig(
                        codeRate,
                        10_000,
                        30,
                        new double[]{
                                0.0,
                                1.0,
                                2.0,
                                3.0,
                                4.0
                        },
                        1234L
                );

        DecoderFactory factory =
                createFactory(decoderName);

        BerSimulation simulation =
                new BerSimulation(
                        h,
                        config,
                        factory
                );

        List<SimulationResult> results =
                simulation.run();

        Path output =
                Path.of(
                        "results/ber/ber_"
                                + decoderName
                                + "_"
                                + matrixName
                                + ".csv"
                );

        CsvWriter.writeBerResults(
                output,
                results
        );

        System.out.println(
                "Decoder: " + decoderName
        );

        System.out.println(
                "Matrix: " + matrixName
        );

        System.out.println(
                "Rows: " + h.rows()
        );

        System.out.println(
                "Cols: " + h.cols()
        );

        System.out.println(
                "Edges: " + h.edgeCount()
        );

        System.out.printf(
                "Estimated rate: %.4f%n",
                codeRate
        );

        for (SimulationResult result : results) {

            System.out.printf(
                    "Eb/N0 %.1f dB | BER %.7f | FER %.7f | Success %.4f | AvgIter %.2f%n",
                    result.ebN0Db(),
                    result.ber(),
                    result.fer(),
                    result.successRate(),
                    result.averageIterations()
            );
        }

        System.out.println(
                "Wrote " + output
        );
    }

    private static CsrMatrix loadMatrix(
            String matrixName
    ) throws Exception {

        return switch (matrixName) {

            case "toy" ->
                    HMatrixLoader.loadResource(
                            TOY_MATRIX_RESOURCE
                    );

            case "gallager",
                 "regular",
                 "regular-96" ->
                    RegularLdpcMatrixFactory.create(
                            48,
                            96,
                            3
                    );

            case "regular-504" ->
                    RegularLdpcMatrixFactory.create(
                            252,
                            504,
                            3
                    );

            default ->
                    throw new IllegalArgumentException(
                            "Unknown matrix: "
                                    + matrixName
                    );
        };
    }

    private static double estimateCodeRate(
            CsrMatrix h
    ) {

        return Math.max(
                1e-6,
                (double) (h.cols() - h.rows())
                        / h.cols()
        );
    }

    private static DecoderFactory createFactory(
            String decoderName
    ) {

        return switch (decoderName) {

            case "minsum" ->
                    MinSumDecoder::new;

            case "normalized",
                 "normalized-minsum",
                 "nms" ->
                    (h, maxIterations) ->
                            new NormalizedMinSumDecoder(
                                    h,
                                    maxIterations,
                                    0.75f
                            );

            case "offset",
                 "offset-minsum",
                 "oms" ->
                    (h, maxIterations) ->
                            new OffsetMinSumDecoder(
                                    h,
                                    maxIterations,
                                    0.25f
                            );

            default ->
                    throw new IllegalArgumentException(
                            "Unknown decoder: "
                                    + decoderName
                    );
        };
    }
}
