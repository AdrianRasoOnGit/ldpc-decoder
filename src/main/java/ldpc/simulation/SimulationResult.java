package ldpc.simulation;

public record SimulationResult(
        double ebN0Db,
        int trials,
        long totalBits,
        long bitErrors,
        long frameErrors
) {
    public double ber() {
        return totalBits == 0 ? 0.0 : (double) bitErrors / totalBits;
    }

    public double fer() {
        return trials == 0 ? 0.0 : (double) frameErrors / trials;
    }
}
