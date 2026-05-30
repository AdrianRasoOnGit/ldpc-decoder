package ldpc.matrix;

import ldpc.decoder.SyndromeChecker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegularLdpcMatrixFactoryTest {
    @Test
    void createsMatrixWithExpectedShape() {
        CsrMatrix h = RegularLdpcMatrixFactory.create(48, 96, 3);

        assertEquals(48, h.rows());
        assertEquals(96, h.cols());
        assertEquals(288, h.edgeCount());
    }

    @Test
    void allZeroCodewordIsValid() {
        CsrMatrix h = RegularLdpcMatrixFactory.create(48, 96, 3);

        int[] bits = new int[h.cols()];

        assertTrue(SyndromeChecker.isValid(h, bits));
    }

    @Test
    void rejectsInvalidInputs() {
        assertThrows(
                IllegalArgumentException.class,
                () -> RegularLdpcMatrixFactory.create(0, 96, 3)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> RegularLdpcMatrixFactory.create(48, 0, 3)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> RegularLdpcMatrixFactory.create(48, 96, 0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> RegularLdpcMatrixFactory.create(48, 96, 49)
        );
    }
}
