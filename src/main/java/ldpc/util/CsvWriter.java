package ldpc.util;

import ldpc.simulation.SimulationResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class CsvWriter {
    private CsvWriter() {}

    public static void writeBerResults(Path path, List<SimulationResult> results) throws IOException {
        Path parent = path.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("ebN0Db,trials,totalBits,bitErrors,frameErrors,ber,fer\n");

        for (SimulationResult result : results) {
            sb.append(result.ebN0Db()).append(',')
                    .append(result.trials()).append(',')
                    .append(result.totalBits()).append(',')
                    .append(result.bitErrors()).append(',')
                    .append(result.frameErrors()).append(',')
                    .append(result.ber()).append(',')
                    .append(result.fer()).append('\n');
        }

        Files.writeString(path, sb.toString());
    }
}
