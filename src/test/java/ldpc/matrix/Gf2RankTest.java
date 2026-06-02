package ldpc.matrix;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Gf2RankTest {
    @Test
    void computesToyMatrixRank() {
        CsrMatrix h = toyMatrix();

        assertEquals(3, Gf2Rank.compute(h));
        assertEquals(0.5, Gf2Rank.codeRate(h), 1e-12);
    }

    @Test
    void handlesDependentRows() {
        CsrMatrix h = CsrMatrix.fromRows(
                3,
                4,
                new int[][] {
                        {0, 1},
                        {1, 2},
                        {0, 2}
                }
        );

        assertEquals(2, Gf2Rank.compute(h));
        assertEquals(0.5, Gf2Rank.codeRate(h), 1e-12);
    }

    @Test
    void handlesZeroRows() {
        CsrMatrix h = CsrMatrix.fromRows(
                3,
                5,
                new int[][] {
                        {0, 1},
                        {},
                        {2, 3}
                }
        );

        assertEquals(2, Gf2Rank.compute(h));
        assertEquals(0.6, Gf2Rank.codeRate(h), 1e-12);
    }

    @Test
    void computesRegularMatrixRankWithinBounds() {
        CsrMatrix h =
                RegularLdpcMatrixFactory.create(48, 96, 3);

        int rank = Gf2Rank.compute(h);

        assertTrue(rank > 0);
        assertTrue(rank <= h.rows());

        double rate = Gf2Rank.codeRate(h);

        assertTrue(rate > 0.0);
        assertTrue(rate < 1.0);
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
