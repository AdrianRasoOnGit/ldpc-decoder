package ldpc.decoder;

import ldpc.channel.AwgnChannel;
import ldpc.channel.BpskModem;
import ldpc.channel.LlrInitializer;
import ldpc.matrix.CsrMatrix;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NormalizedMinSumDecoderTest {
    @Test
    void decoderHandlesNoiselessAllZeroCodeword() {
        CsrMatrix h = toyMatrix();

        int[] codeword = {0, 0, 0, 0, 0, 0};
        float[] symbols = BpskModem.modulate(codeword);
        float[] llr = LlrInitializer.compute(symbols, 0.5f);

        NormalizedMinSumDecoder decoder =
                new NormalizedMinSumDecoder(h, 10, 0.75f);

        DecodeResult result = decoder.decode(llr);

        assertTrue(result.success());
        assertTrue(SyndromeChecker.isValid(h, result.bits()));
    }

    @Test
    void decoderRecoversMildlyNoisyAllZeroCodeword() {
        CsrMatrix h = toyMatrix();

        int[] codeword = {0, 0, 0, 0, 0, 0};
        float sigma = 0.45f;

        float[] symbols = BpskModem.modulate(codeword);
        float[] received = new AwgnChannel(7L).transmit(symbols, sigma);
        float[] llr = LlrInitializer.compute(received, sigma);

        NormalizedMinSumDecoder decoder =
                new NormalizedMinSumDecoder(h, 20, 0.75f);

        DecodeResult result = decoder.decode(llr);

        assertTrue(result.success());
        assertTrue(SyndromeChecker.isValid(h, result.bits()));
    }

    @Test
    void rejectsInvalidAlpha() {
        CsrMatrix h = toyMatrix();

        assertThrows(
                IllegalArgumentException.class,
                () -> new NormalizedMinSumDecoder(h, 10, 0.0f)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new NormalizedMinSumDecoder(h, 10, 1.5f)
        );
    }

    private static CsrMatrix toyMatrix() {
        return CsrMatrix.fromRows(3, 6, new int[][] {
                {0, 1, 3},
                {1, 2, 4},
                {0, 2, 5}
        });
    }
}
