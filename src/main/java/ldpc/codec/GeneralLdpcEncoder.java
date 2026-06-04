package ldpc.codec;

import ldpc.matrix.CsrMatrix;

import java.util.Arrays;

public final class GeneralLdpcEncoder implements LdpcEncoder {
    private final int n;
    private final int rank;
    private final int[] pivotColumns;
    private final int[] freeColumns;
    private final boolean[][] rref;

    public GeneralLdpcEncoder(CsrMatrix h) {
        this.n = h.cols();

        boolean[][] matrix = denseCopy(h);
        RrefResult result = rref(matrix);

        this.rank = result.rank();
        this.pivotColumns = result.pivotColumns();
        this.rref = result.matrix();
        this.freeColumns = computeFreeColumns(n, pivotColumns);
    }

    @Override
    public int messageLength() {
        return freeColumns.length;
    }

    @Override
    public int codewordLength() {
        return n;
    }

    @Override
    public int[] encode(int[] messageBits) {
        if (messageBits.length != messageLength()) {
            throw new IllegalArgumentException(
                    "Expected "
                            + messageLength()
                            + " message bits, got "
                            + messageBits.length
            );
        }

        int[] codeword = new int[n];

        for (int i = 0; i < freeColumns.length; i++) {
            codeword[freeColumns[i]] = messageBits[i] & 1;
        }

        for (int row = rank - 1; row >= 0; row--) {
            int pivotCol = pivotColumns[row];
            int value = 0;

            for (int freeCol : freeColumns) {
                if (rref[row][freeCol]) {
                    value ^= codeword[freeCol];
                }
            }

            codeword[pivotCol] = value;
        }

        return codeword;
    }

    public int[] extractMessage(int[] codeword) {
        if (codeword.length != codewordLength()) {
            throw new IllegalArgumentException(
                    "Expected codeword length "
                            + codewordLength()
                            + ", got "
                            + codeword.length
            );
        }

        int[] messageBits = new int[messageLength()];

        for (int i = 0; i < freeColumns.length; i++) {
            messageBits[i] = codeword[freeColumns[i]] & 1;
        }

        return messageBits;
    }

    public int rank() {
        return rank;
    }

    public int[] pivotColumns() {
        return Arrays.copyOf(pivotColumns, pivotColumns.length);
    }

    public int[] freeColumns() {
        return Arrays.copyOf(freeColumns, freeColumns.length);
    }

    public int[] messageColumns() {
        return freeColumns();
    }

    private static boolean[][] denseCopy(CsrMatrix h) {
        boolean[][] dense = new boolean[h.rows()][h.cols()];

        for (int row = 0; row < h.rows(); row++) {
            for (int edge = h.rowStart(row); edge < h.rowEnd(row); edge++) {
                dense[row][h.colIndex(edge)] = true;
            }
        }

        return dense;
    }

    private static RrefResult rref(boolean[][] matrix) {
        int rows = matrix.length;
        int cols = rows == 0 ? 0 : matrix[0].length;

        int[] pivotColumns = new int[Math.min(rows, cols)];
        int rank = 0;

        for (int col = 0; col < cols && rank < rows; col++) {
            int pivotRow = -1;

            for (int row = rank; row < rows; row++) {
                if (matrix[row][col]) {
                    pivotRow = row;
                    break;
                }
            }

            if (pivotRow == -1) {
                continue;
            }

            swapRows(matrix, rank, pivotRow);

            for (int row = 0; row < rows; row++) {
                if (row != rank && matrix[row][col]) {
                    xorRows(matrix[row], matrix[rank]);
                }
            }

            pivotColumns[rank] = col;
            rank++;
        }

        return new RrefResult(
                matrix,
                Arrays.copyOf(pivotColumns, rank),
                rank
        );
    }

    private static void swapRows(boolean[][] matrix, int a, int b) {
        if (a == b) {
            return;
        }

        boolean[] tmp = matrix[a];
        matrix[a] = matrix[b];
        matrix[b] = tmp;
    }

    private static void xorRows(boolean[] target, boolean[] source) {
        for (int i = 0; i < target.length; i++) {
            target[i] ^= source[i];
        }
    }

    private static int[] computeFreeColumns(int cols, int[] pivotColumns) {
        boolean[] isPivot = new boolean[cols];

        for (int pivot : pivotColumns) {
            isPivot[pivot] = true;
        }

        int freeCount = cols - pivotColumns.length;
        int[] freeColumns = new int[freeCount];

        int index = 0;

        for (int col = 0; col < cols; col++) {
            if (!isPivot[col]) {
                freeColumns[index++] = col;
            }
        }

        return freeColumns;
    }

    private record RrefResult(
            boolean[][] matrix,
            int[] pivotColumns,
            int rank
    ) {}
}
