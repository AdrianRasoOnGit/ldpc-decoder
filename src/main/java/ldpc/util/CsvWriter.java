package ldpc.util;

import ldpc.simulation.SimulationResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class CsvWriter {

    private CsvWriter() {}

    public static void writeBerResults(
            Path output,
            List<SimulationResult> results
    ) throws IOException {

        Files.createDirectories(output.getParent());

        StringBuilder sb = new StringBuilder();

        sb.append(
                "ebN0Db,"
                        + "trials,"
                        + "totalBits,"
                        + "bitErrors,"
                        + "frameErrors,"
                        + "successfulDecodes,"
                        + "totalIterations,"
                        + "ber,"
                        + "fer,"
                        + "successRate,"
                        + "avgIterations\n"
        );

        for (SimulationResult result : results) {

            sb.append(result.ebN0Db()).append(',')
                    .append(result.trials()).append(',')
                    .append(result.totalBits()).append(',')
                    .append(result.bitErrors()).append(',')
                    .append(result.frameErrors()).append(',')
                    .append(result.successfulDecodes()).append(',')
                    .append(result.totalIterations()).append(',')
                    .append(result.ber()).append(',')
                    .append(result.fer()).append(',')
                    .append(result.successRate()).append(',')
                    .append(result.averageIterations())
                    .append('\n');
        }

        Files.writeString(
                output,
                sb.toString()
        );
    }
}
