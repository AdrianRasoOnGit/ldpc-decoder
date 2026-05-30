package ldpc.app;

import ldpc.decoder.MinSumDecoder;
import ldpc.decoder.NormalizedMinSumDecoder;
import ldpc.decoder.OffsetMinSumDecoder;
import ldpc.matrix.CsrMatrix;
import ldpc.matrix.HMatrixLoader;
import ldpc.simulation.BerSimulation;
import ldpc.simulation.DecoderFactory;
import ldpc.simulation.SimulationConfig;
import ldpc.simulation.SimulationResult;
import ldpc.util.CsvWriter;

import java.nio.file.Path;
import java.util.List;

public final class BerRunner {
    private static final String MATRIX_RESOURCE = "/matrices/toy/h_3x6.txt";

    private BerRunner() {}

    public static void main(String[] args) throws Exception {
        String decoderName =
                args.length > 0 ? args[0].toLowerCase() : "normalized";

        CsrMatrix h = HMatrixLoader.loadResource(MATRIX_RESOURCE);

        SimulationConfig config = new SimulationConfig(
                0.5,
                10_000,
                20,
                new double[] {0.0, 1.0, 2.0, 3.0, 4.0},
                1234L
        );

        DecoderFactory factory = createFactory(decoderName);

        BerSimulation simulation =
                new BerSimulation(h, config, factory);

        List<SimulationResult> results = simulation.run();

        Path output =
                Path.of("results/ber/ber_" + decoderName + ".csv");

        CsvWriter.writeBerResults(output, results);

        System.out.println("Decoder: " + decoderName);

        for (SimulationResult result : results) {
            System.out.printf(
                    "Eb/N0 %.1f dB | BER %.7f | FER %.7f%n",
                    result.ebN0Db(),
                    result.ber(),
                    result.fer()
            );
        }

        System.out.println("Wrote " + output);
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

            default ->
                    throw new IllegalArgumentException(
                            "Unknown decoder: "
                                    + decoderName
                                    + ". Use: minsum, normalized, normalized-minsum, "
                                    + "nms, offset, offset-minsum, or oms."
                    );
        };
    }
}
