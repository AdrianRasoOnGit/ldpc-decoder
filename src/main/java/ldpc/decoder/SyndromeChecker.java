package ldpc.decoder;

import ldpc.matrix.CsrMatrix;

public final class SyndromeChecker {
    private SyndromeChecker() {}

    public static boolean isValid(CsrMatrix h, int[] bits) {
        if (bits.length != h.cols()) {
            throw new IllegalArgumentException("Bits length must equal matrix columns");
        }

        for (int row = 0; row < h.rows(); row++) {
            int parity = 0;

            for (int edge = h.rowStart(row); edge < h.rowEnd(row); edge++) {
                int col = h.colIndex(edge);
                parity ^= bits[col] & 1;
            }

            if (parity != 0) {
                return false;
            }
        }

        return true;
    }
}
