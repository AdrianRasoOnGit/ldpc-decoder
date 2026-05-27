package ldpc.matrix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class HMatrixLoader {
    private HMatrixLoader() {}

    public static CsrMatrix load(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream must not be null");
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String header = nextDataLine(reader);

            if (header == null) {
                throw new IOException("Missing matrix header");
            }

            String[] headerParts = header.trim().split("\\s+");

            if (headerParts.length != 2) {
                throw new IOException("Header must contain rows and columns");
            }

            int rows = Integer.parseInt(headerParts[0]);
            int cols = Integer.parseInt(headerParts[1]);

            int[][] rowColumns = new int[rows][];

            for (int row = 0; row < rows; row++) {
                String line = nextDataLine(reader);

                if (line == null) {
                    throw new IOException("Missing matrix row " + row);
                }

                String trimmed = line.trim();

                if (trimmed.isEmpty()) {
                    rowColumns[row] = new int[0];
                    continue;
                }

                String[] parts = trimmed.split("\\s+");
                rowColumns[row] = new int[parts.length];

                for (int i = 0; i < parts.length; i++) {
                    int col = Integer.parseInt(parts[i]);

                    if (col < 0 || col >= cols) {
                        throw new IOException("Column out of bounds at row " + row + ": " + col);
                    }

                    rowColumns[row][i] = col;
                }
            }

            return CsrMatrix.fromRows(rows, cols, rowColumns);
        }
    }

    public static CsrMatrix loadResource(String resourcePath) throws IOException {
        InputStream in = HMatrixLoader.class.getResourceAsStream(resourcePath);

        if (in == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }

        return load(in);
    }

    private static String nextDataLine(BufferedReader reader) throws IOException {
        String line;

        while ((line = reader.readLine()) != null) {
            String trimmed = line.trim();

            if (trimmed.isEmpty()) {
                continue;
            }

            if (trimmed.startsWith("#")) {
                continue;
            }

            return trimmed;
        }

        return null;
    }
}
