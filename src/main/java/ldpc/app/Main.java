package ldpc.app;

import ldpc.channel.AwgnChannel;
import ldpc.channel.BpskModem;
import ldpc.channel.LlrInitializer;
import ldpc.decoder.DecodeResult;
import ldpc.decoder.MinSumDecoder;
import ldpc.decoder.SyndromeChecker;
import ldpc.matrix.CsrMatrix;

import java.util.Arrays;

public final class Main {
    public static void main(String[] args) {
        CsrMatrix h = CsrMatrix.fromRows(3, 6, new int[][] {
                {0, 1, 3},
                {1, 2, 4},
                {0, 2, 5}
        });

        int[] codeword = {0, 0, 0, 0, 0, 0};

        float sigma = 0.65f;

        float[] symbols = BpskModem.modulate(codeword);
        AwgnChannel channel = new AwgnChannel(1234L);
        float[] received = channel.transmit(symbols, sigma);
        float[] llr = LlrInitializer.compute(received, sigma);

        MinSumDecoder decoder = new MinSumDecoder(h, 20);
        DecodeResult result = decoder.decode(llr);

        System.out.println("Decoded bits: " + Arrays.toString(result.bits()));
        System.out.println("Iterations: " + result.iterations());
        System.out.println("Success: " + result.success());
        System.out.println("Syndrome valid: " + SyndromeChecker.isValid(h, result.bits()));
    }
}
