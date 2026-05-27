package ldpc.matrix;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CsrMatrixTest {
    @Test
    void buildsToyMatrix() {
        CsrMatrix h = CsrMatrix.fromRows(3, 6, new int[][] {
                {0, 1, 3},
                {1, 2, 4},
                {0, 2, 5}
        });

        assertEquals(3, h.rows());
        assertEquals(6, h.cols());
        assertEquals(9, h.edgeCount());

        assertEquals(0, h.rowStart(0));
        assertEquals(3, h.rowEnd(0));
        assertEquals(0, h.colIndex(0));
        assertEquals(1, h.colIndex(1));
        assertEquals(3, h.colIndex(2));
    }
}
