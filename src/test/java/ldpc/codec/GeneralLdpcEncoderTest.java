package ldpc.codec;

import ldpc.decoder.SyndromeChecker;
import ldpc.matrix.CsrMatrix;
import ldpc.matrix.RegularLdpcMatrixFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeneralLdpcEncoderTest {
    @Test
    void encodesValidToyCodewords() {
        CsrMatrix h = toyMatrix();
        GeneralLdpcEncoder encoder = new GeneralLdpcEncoder(h);

        assertEquals(3, encoder.messageLength());
        assertEquals(6, encoder.codewordLength());

        int[] message = {1, 0, 1};
        int[] codeword = encoder.encode(message);

        assertTrue(SyndromeChecker.isValid(h, codeword));
    }

    @Test
    void encodesMultipleToyMessages() {
        CsrMatrix h = toyMatrix();
        GeneralLdpcEncoder encoder = new GeneralLdpcEncoder(h);

        int[][] messages = {
                {0, 0, 0},
                {1, 0, 0},
                {0, 1, 0},
                {0, 0, 1},
                {1, 1, 1}
        };

        for (int[] message : messages) {
            int[] codeword = encoder.encode(message);

            assertTrue(
                    SyndromeChecker.isValid(h, codeword),
                    "Encoded codeword must satisfy Hx = 0"
            );
        }
    }

    @Test
    void encodesValidRegularMatrixCodeword() {
        CsrMatrix h =
                RegularLdpcMatrixFactory.create(48, 96, 3);

        GeneralLdpcEncoder encoder =
                new GeneralLdpcEncoder(h);

        int[] message =
                new int[encoder.messageLength()];

        for (int i = 0; i < message.length; i++) {
            message[i] = i & 1;
        }

        int[] codeword =
                encoder.encode(message);

        assertEquals(h.cols(), codeword.length);
        assertTrue(SyndromeChecker.isValid(h, codeword));
    }

    @Test
    void rejectsWrongMessageLength() {
        CsrMatrix h = toyMatrix();
        GeneralLdpcEncoder encoder = new GeneralLdpcEncoder(h);

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
