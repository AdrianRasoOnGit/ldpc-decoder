package ldpc.codec;

import ldpc.decoder.SyndromeChecker;
import ldpc.matrix.CsrMatrix;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SystematicToyEncoderTest {
    @Test
    void encodesValidCodeword() {
        SystematicToyEncoder encoder =
                new SystematicToyEncoder();

        CsrMatrix h =
                toyMatrix();

        int[] message = {1, 0, 1};
        int[] codeword = encoder.encode(message);

        assertEquals(6, codeword.length);
        assertTrue(SyndromeChecker.isValid(h, codeword));
    }

    @Test
    void rejectsWrongMessageLength() {
        SystematicToyEncoder encoder =
                new SystematicToyEncoder();

        assertThrows(
                IllegalArgumentException.class,
                () -> encoder.encode(new int[] {1, 0})
        );
    }

    private static CsrMatrix toyMatrix() {
        return CsrMatrix.fromRows(
                3,
                6,
                new int[][] {
                        {0, 1, 3},
                        {1, 2, 4},
                        {0, 2, 5}
                }
        );
    }
}
