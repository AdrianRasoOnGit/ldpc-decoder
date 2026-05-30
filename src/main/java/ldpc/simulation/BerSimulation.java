package ldpc.simulation;

import ldpc.channel.AwgnChannel;
import ldpc.channel.BpskModem;
import ldpc.channel.LlrInitializer;
import ldpc.decoder.DecodeResult;
import ldpc.decoder.LdpcDecoder;
import ldpc.decoder.MinSumDecoder;
import ldpc.matrix.CsrMatrix;

import java.util.ArrayList;
import java.util.List;

public final class BerSimulation {
    private final CsrMatrix h;
    private final SimulationConfig config;
    private final DecoderFactory decoderFactory;

    public BerSimulation(CsrMatrix h, SimulationConfig config) {
        this(h, config, MinSumDecoder::new);
    }

    public BerSimulation(
            CsrMatrix h,
            SimulationConfig config,
            DecoderFactory decoderFactory
    ) {
        this.h = h;
        this.config = config;
        this.decoderFactory = decoderFactory;
    }

    public List<SimulationResult> run() {
        List<SimulationResult> results = new ArrayList<>();

        for (double ebN0Db : config.ebN0DbValues()) {
            results.add(runPoint(ebN0Db));
        }

        return results;
    }

    private SimulationResult runPoint(double ebN0Db) {
        int n = h.cols();

        int[] codeword = new int[n];
        float[] symbols = BpskModem.modulate(codeword);

        float sigma = sigmaFromEbN0(ebN0Db, config.codeRate());

        AwgnChannel channel = new AwgnChannel(
                config.seed() + Double.doubleToLongBits(ebN0Db)
        );

        LdpcDecoder decoder = decoderFactory.create(h, config.maxIterations());

        long bitErrors = 0;
        long frameErrors = 0;
        long totalBits = 0;
        long successfulDecodes = 0;
        long totalIterations = 0;

        for (int trial = 0; trial < config.trialsPerPoint(); trial++) {
            float[] received = channel.transmit(symbols, sigma);
            float[] llr = LlrInitializer.compute(received, sigma);

            DecodeResult result = decoder.decode(llr);

            int frameBitErrors = countBitErrors(codeword, result.bits());

            bitErrors += frameBitErrors;
            totalBits += n;
            totalIterations += result.iterations();

            if (result.success()) {
                successfulDecodes++;
            }

            if (frameBitErrors > 0) {
                frameErrors++;
            }
        }

        return new SimulationResult(
                ebN0Db,
                config.trialsPerPoint(),
                totalBits,
                bitErrors,
                frameErrors,
                successfulDecodes,
                totalIterations
        );
    }

    public static float sigmaFromEbN0(double ebN0Db, double codeRate) {
        double ebN0Linear = Math.pow(10.0, ebN0Db / 10.0);
        return (float) Math.sqrt(1.0 / (2.0 * codeRate * ebN0Linear));
    }

    private static int countBitErrors(int[] expected, int[] actual) {
        int errors = 0;

        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != actual[i]) {
                errors++;
            }
        }

        return errors;
    }
}
