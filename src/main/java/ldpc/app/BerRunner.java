package ldpc.app;

import ldpc.decoder.LayeredMinSumDecoder;
import ldpc.decoder.MinSumDecoder;
import ldpc.decoder.NormalizedMinSumDecoder;
import ldpc.decoder.OffsetMinSumDecoder;
import ldpc.matrix.AlistMatrixLoader;
import ldpc.matrix.CsrMatrix;
import ldpc.matrix.Gf2Rank;
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

    private static final String ALIST_TOY_MATRIX_RESOURCE =
            "/matrices/alist/h_3x6.alist";

    private static final String PEG_10000_R05_AWGN_RESOURCE =
            "/matrices/alist/peg_10000_r05_awgn.alist";

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

        CsrMatrix h = loadMatrix(matrixName);

        int rank = Gf2Rank.compute(h);
        double codeRate = Gf2Rank.codeRate(h);

        SimulationConfig config = createConfig(
                matrixName,
                codeRate
        );

        DecoderFactory factory = createFactory(decoderName);
        BerSimulation simulation = new BerSimulation(h, config, factory);

        List<SimulationResult> results = simulation.run();

        Path output = Path.of(
                "results/ber/ber_"
                        + decoderName
                        + "_"
                        + matrixName
                        + ".csv"
        );

        CsvWriter.writeBerResults(output, results);

        System.out.println("Decoder: " + decoderName);
        System.out.println("Matrix: " + matrixName);
        System.out.println("Rows: " + h.rows());
        System.out.println("Cols: " + h.cols());
        System.out.println("Edges: " + h.edgeCount());
        System.out.println("Rank: " + rank);
        System.out.printf("Code rate: %.6f%n", codeRate);
        System.out.println("Trials per point: " + config.trialsPerPoint());
        System.out.println("Max iterations: " + config.maxIterations());

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

        System.out.println("Wrote " + output);
    }

    private static SimulationConfig createConfig(
            String matrixName,
            double codeRate
    ) {
        if (matrixName.equals("peg-10000")
                || matrixName.equals("peg-10000-r05-awgn")
                || matrixName.equals("upm-awgn-r05-n10000")) {

            return new SimulationConfig(
                    codeRate,
                    200,
                    30,
                    new double[] {0.0, 1.0, 2.0, 3.0, 4.0},
                    1234L
            );
        }

        return new SimulationConfig(
                codeRate,
                10_000,
                30,
                new double[] {0.0, 1.0, 2.0, 3.0, 4.0},
                1234L
        );
    }

    private static CsrMatrix loadMatrix(String matrixName) throws Exception {
        return switch (matrixName) {
            case "toy" ->
                    HMatrixLoader.loadResource(TOY_MATRIX_RESOURCE);

            case "alist-toy" ->
                    AlistMatrixLoader.loadResource(
                            ALIST_TOY_MATRIX_RESOURCE
                    );

            case "peg-10000",
                 "peg-10000-r05-awgn",
                 "upm-awgn-r05-n10000" ->
                    AlistMatrixLoader.loadResource(
                            PEG_10000_R05_AWGN_RESOURCE
                    );

            case "gallager",
                 "regular",
                 "regular-96" ->
                    RegularLdpcMatrixFactory.create(48, 96, 3);

            case "regular-504" ->
                    RegularLdpcMatrixFactory.create(252, 504, 3);

            default ->
                    throw new IllegalArgumentException(
                            "Unknown matrix: "
                                    + matrixName
                                    + ". Use: toy, alist-toy, peg-10000, "
                                    + "peg-10000-r05-awgn, upm-awgn-r05-n10000, "
                                    + "gallager, regular, regular-96, or regular-504."
                    );
        };
    }

    private static DecoderFactory createFactory(String decoderName) {
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

            case "layered",
                 "layered-minsum",
                 "lms" ->
                    (h, maxIterations) ->
                            new LayeredMinSumDecoder(
                                    h,
                                    maxIterations,
                                    0.75f
                            );

            default ->
                    throw new IllegalArgumentException(
                            "Unknown decoder: "
                                    + decoderName
                                    + ". Use: minsum, normalized, normalized-minsum, "
                                    + "nms, offset, offset-minsum, oms, layered, "
                                    + "layered-minsum, or lms."
                    );
        };
    }
}
