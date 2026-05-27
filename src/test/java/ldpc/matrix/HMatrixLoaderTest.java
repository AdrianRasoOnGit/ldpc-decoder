package ldpc.matrix;

import ldpc.decoder.SyndromeChecker;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class HMatrixLoaderTest {
    @Test
    void loadsToyMatrixFromResources() throws IOException {
        CsrMatrix h = HMatrixLoader.loadResource("/matrices/toy/h_3x6.txt");

        assertEquals(3, h.rows());
        assertEquals(6, h.cols());
        assertEquals(9, h.edgeCount());

        assertEquals(0, h.colIndex(0));
        assertEquals(1, h.colIndex(1));
        assertEquals(3, h.colIndex(2));
    }

    @Test
    void loadedToyMatrixValidatesAllZeroCodeword() throws IOException {
        CsrMatrix h = HMatrixLoader.loadResource("/matrices/toy/h_3x6.txt");

        int[] bits = {0, 0, 0, 0, 0, 0};

        assertTrue(SyndromeChecker.isValid(h, bits));
    }
}
