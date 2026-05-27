package ldpc.decoder;

import ldpc.matrix.CsrMatrix;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SyndromeCheckerTest {
    @Test
    void allZeroCodewordIsValid() {
        CsrMatrix h = toyMatrix();

        int[] bits = {0, 0, 0, 0, 0, 0};

        assertTrue(SyndromeChecker.isValid(h, bits));
    }

    @Test
    void invalidCodewordFails() {
        CsrMatrix h = toyMatrix();

        int[] bits = {1, 0, 0, 0, 0, 0};

        assertFalse(SyndromeChecker.isValid(h, bits));
    }

    private static CsrMatrix toyMatrix() {
        return CsrMatrix.fromRows(3, 6, new int[][] {
                {0, 1, 3},
                {1, 2, 4},
                {0, 2, 5}
        });
    }
}
