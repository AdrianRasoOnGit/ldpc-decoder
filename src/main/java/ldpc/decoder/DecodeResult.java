package ldpc.decoder;

public record DecodeResult(
        int[] bits,
        int iterations,
        boolean success
) {}
