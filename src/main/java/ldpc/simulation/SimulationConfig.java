package ldpc.simulation;

public record SimulationConfig(
        double codeRate,
        int trialsPerPoint,
        int maxIterations,
        double[] ebN0DbValues,
        long seed
) {
    public SimulationConfig {
        if (codeRate <= 0.0 || codeRate > 1.0) {
            throw new IllegalArgumentException("codeRate must be in (0, 1]");
        }

        if (trialsPerPoint <= 0) {
            throw new IllegalArgumentException("trialsPerPoint must be positive");
        }

        if (maxIterations <= 0) {
            throw new IllegalArgumentException("maxIterations must be positive");
        }

        if (ebN0DbValues == null || ebN0DbValues.length == 0) {
            throw new IllegalArgumentException("ebN0DbValues must not be empty");
        }
    }
}
