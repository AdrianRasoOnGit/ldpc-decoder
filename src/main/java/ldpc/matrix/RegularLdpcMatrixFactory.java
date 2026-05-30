package ldpc.matrix;

public final class RegularLdpcMatrixFactory {
    private RegularLdpcMatrixFactory() {}

    /**
     * Creates a simple regular LDPC-style parity-check matrix.
     *
     * Each variable node has variableDegree connections.
     * Each row receives approximately balanced connections.
     */
    public static CsrMatrix create(
            int rows,
            int cols,
            int variableDegree
    ) {
        if (rows <= 0) {
            throw new IllegalArgumentException("rows must be positive");
        }

        if (cols <= 0) {
            throw new IllegalArgumentException("cols must be positive");
        }

        if (variableDegree <= 0 || variableDegree > rows) {
            throw new IllegalArgumentException(
                    "variableDegree must be in [1, rows]"
            );
        }

        int[][] rowColumns = new int[rows][];
        int[] rowDegrees = new int[rows];

        for (int col = 0; col < cols; col++) {
            for (int k = 0; k < variableDegree; k++) {
                int row = Math.floorMod(col + k * (rows / variableDegree + 1), rows);
                rowDegrees[row]++;
            }
        }

        for (int row = 0; row < rows; row++) {
            rowColumns[row] = new int[rowDegrees[row]];
        }

        int[] cursor = new int[rows];

        for (int col = 0; col < cols; col++) {
            for (int k = 0; k < variableDegree; k++) {
                int row = Math.floorMod(col + k * (rows / variableDegree + 1), rows);
                rowColumns[row][cursor[row]++] = col;
            }
        }

        return CsrMatrix.fromRows(rows, cols, rowColumns);
    }
}
