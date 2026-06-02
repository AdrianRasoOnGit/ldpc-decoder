package ldpc.matrix;

import java.util.BitSet;

public final class Gf2Rank {
    private Gf2Rank() {}

    public static int compute(CsrMatrix matrix) {
        BitSet[] rows = toBitRows(matrix);

        int rank = 0;
        int rowCount = matrix.rows();
        int colCount = matrix.cols();

        for (int col = 0; col < colCount && rank < rowCount; col++) {
            int pivot = findPivot(rows, rank, rowCount, col);

            if (pivot < 0) {
                continue;
            }

            swap(rows, rank, pivot);

            for (int row = 0; row < rowCount; row++) {
                if (row != rank && rows[row].get(col)) {
                    rows[row].xor(rows[rank]);
                }
            }

            rank++;
        }

        return rank;
    }

    public static double codeRate(CsrMatrix matrix) {
        int rank = compute(matrix);

        return (double) (matrix.cols() - rank) / matrix.cols();
    }

    private static BitSet[] toBitRows(CsrMatrix matrix) {
        BitSet[] rows = new BitSet[matrix.rows()];

        for (int row = 0; row < matrix.rows(); row++) {
            BitSet bitSet = new BitSet(matrix.cols());

            for (int edge = matrix.rowStart(row);
                 edge < matrix.rowEnd(row);
                 edge++) {

                bitSet.set(matrix.colIndex(edge));
            }

            rows[row] = bitSet;
        }

        return rows;
    }

    private static int findPivot(
            BitSet[] rows,
            int startRow,
            int rowCount,
            int col
    ) {
        for (int row = startRow; row < rowCount; row++) {
            if (rows[row].get(col)) {
                return row;
            }
        }

        return -1;
    }

    private static void swap(BitSet[] rows, int a, int b) {
        if (a == b) {
            return;
        }

        BitSet tmp = rows[a];
        rows[a] = rows[b];
        rows[b] = tmp;
    }
}
