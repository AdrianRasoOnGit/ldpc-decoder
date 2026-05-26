package ldpc.matrix;

import java.util.Arrays;

public final class CsrMatrix {
    private final int rows;
    private final int cols;
    private final int[] rowPtr;
    private final int[] colIdx;

    private CsrMatrix(int rows, int cols, int[] rowPtr, int[] colIdx) {
        this.rows = rows;
        this.cols = cols;
        this.rowPtr = rowPtr;
        this.colIdx = colIdx;
    }

    public static CsrMatrix fromRows(int rows, int cols, int[][] rowColumns) {
        if (rowColumns.length != rows) {
            throw new IllegalArgumentException("rowColumns length must equal rows");
        }

        int edges = 0;
        for (int[] row : rowColumns) {
            edges += row.length;
        }

        int[] rowPtr = new int[rows + 1];
        int[] colIdx = new int[edges];

        int edge = 0;
        for (int r = 0; r < rows; r++) {
            rowPtr[r] = edge;
            for (int c : rowColumns[r]) {
                if (c < 0 || c >= cols) {
                    throw new IllegalArgumentException("Column out of bounds: " + c);
                }
                colIdx[edge++] = c;
            }
        }
        rowPtr[rows] = edge;

        return new CsrMatrix(rows, cols, rowPtr, colIdx);
    }

    public int rows() {
        return rows;
    }

    public int cols() {
        return cols;
    }

    public int edgeCount() {
        return colIdx.length;
    }

    public int rowStart(int row) {
        return rowPtr[row];
    }

    public int rowEnd(int row) {
        return rowPtr[row + 1];
    }

    public int colIndex(int edge) {
        return colIdx[edge];
    }

    public int[] rowPtrCopy() {
        return Arrays.copyOf(rowPtr, rowPtr.length);
    }

    public int[] colIdxCopy() {
        return Arrays.copyOf(colIdx, colIdx.length);
    }
}
