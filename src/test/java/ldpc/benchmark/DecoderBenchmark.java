package ldpc.benchmark;

import ldpc.channel.AwgnChannel;
import ldpc.channel.BpskModem;
import ldpc.channel.LlrInitializer;
import ldpc.decoder.LayeredMinSumDecoder;
import ldpc.decoder.LayeredNormalizedMinSumDecoder;
import ldpc.decoder.MinSumDecoder;
import ldpc.decoder.NormalizedMinSumDecoder;
import ldpc.decoder.OffsetMinSumDecoder;
import ldpc.decoder.SumProductDecoder;
import ldpc.matrix.CsrMatrix;
import ldpc.matrix.RegularLdpcMatrixFactory;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class DecoderBenchmark {
    private CsrMatrix h;
    private float[] llr;

    @Param({"0.5", "1.0", "2.0"})
    public float sigma;

    @Setup
    public void setup() {
        h = RegularLdpcMatrixFactory.create(
                48,
                96,
                3
        );

        int[] codeword = new int[h.cols()];
        float[] symbols = BpskModem.modulate(codeword);
        float[] received =
                new AwgnChannel(1234L).transmit(
                        symbols,
                        sigma
                );

        llr = LlrInitializer.compute(
                received,
                sigma
        );
    }

    @Benchmark
    public Object minSum() {
        return new MinSumDecoder(
                h,
                30
        ).decode(llr);
    }

    @Benchmark
    public Object normalizedMinSum() {
        return new NormalizedMinSumDecoder(
                h,
                30,
                0.75f
        ).decode(llr);
    }

    @Benchmark
    public Object offsetMinSum() {
        return new OffsetMinSumDecoder(
                h,
                30,
                0.25f
        ).decode(llr);
    }

    @Benchmark
    public Object layeredMinSum() {
        return new LayeredMinSumDecoder(
                h,
                30,
                1.0f
        ).decode(llr);
    }

    @Benchmark
    public Object layeredNormalizedMinSum() {
        return new LayeredNormalizedMinSumDecoder(
                h,
                30,
                0.75f
        ).decode(llr);
    }

    @Benchmark
    public Object sumProduct() {
        return new SumProductDecoder(
                h,
                30
        ).decode(llr);
    }
}
