package ldpc.matrix;

import ldpc.decoder.SyndromeChecker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlistMatrixLoaderTest {
    @Test
    void loadsToyAlistMatrix() throws Exception {
        CsrMatrix h =
                AlistMatrixLoader.loadResource(
                        "/matrices/alist/h_3x6.alist"
                );

        assertEquals(3, h.rows());
        assertEquals(6, h.cols());
        assertEquals(9, h.edgeCount());

        int[] zero = new int[h.cols()];

        assertTrue(SyndromeChecker.isValid(h, zero));
    }

    @Test
    void matchesToyMatrixStructure() throws Exception {
        CsrMatrix h =
                AlistMatrixLoader.loadResource(
                        "/matrices/alist/h_3x6.alist"
                );

        assertArrayEquals(
                new int[] {0, 1, 3},
                h.rowColumns(0)
        );

        assertArrayEquals(
                new int[] {1, 2, 4},
                h.rowColumns(1)
        );

        assertArrayEquals(
                new int[] {0, 2, 5},
                h.rowColumns(2)
        );
    }
}
