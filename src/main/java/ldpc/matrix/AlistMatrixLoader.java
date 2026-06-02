package ldpc.matrix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class AlistMatrixLoader {
    private AlistMatrixLoader() {}

    public static CsrMatrix loadResource(String resourcePath) throws IOException {
        InputStream input =
                AlistMatrixLoader.class.getResourceAsStream(resourcePath);

        if (input == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }

        try (BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(
                                     input,
                                     StandardCharsets.UTF_8
                             )
                     )) {

            return load(reader);
        }
    }

    public static CsrMatrix load(BufferedReader reader) throws IOException {
        int[] shape = readInts(reader);
        int cols = shape[0];
        int rows = shape[1];

        readInts(reader);
        int[] columnWeights = readInts(reader);
        readInts(reader);

        int[][] rowColumns = new int[rows][];

        for (int row = 0; row < rows; row++) {
            rowColumns[row] = new int[0];
        }

        int[][] tempRows = new int[rows][];
        int[] rowDegrees = new int[rows];

        for (int row = 0; row < rows; row++) {
            tempRows[row] = new int[cols];
        }

        for (int col = 0; col < cols; col++) {
            int[] entries = readInts(reader);
            int weight = columnWeights[col];

            for (int i = 0; i < weight; i++) {
                int rowIndex = entries[i] - 1;

                if (rowIndex < 0 || rowIndex >= rows) {
                    throw new IOException(
                            "Invalid row index "
                                    + entries[i]
                                    + " in column "
                                    + (col + 1)
                    );
                }

                tempRows[rowIndex][rowDegrees[rowIndex]++] = col;
            }
        }

        for (int row = 0; row < rows; row++) {
            rowColumns[row] = new int[rowDegrees[row]];

            System.arraycopy(
                    tempRows[row],
                    0,
                    rowColumns[row],
                    0,
                    rowDegrees[row]
            );
        }

        return CsrMatrix.fromRows(rows, cols, rowColumns);
    }

    private static int[] readInts(BufferedReader reader) throws IOException {
        String line;

        do {
            line = reader.readLine();

            if (line == null) {
                throw new IOException("Unexpected end of AList file");
            }

            line = line.trim();
        } while (line.isEmpty() || line.startsWith("#"));

        String[] parts = line.split("\\s+");
        int[] values = new int[parts.length];

        for (int i = 0; i < parts.length; i++) {
            values[i] = Integer.parseInt(parts[i]);
        }

        return values;
    }
}
