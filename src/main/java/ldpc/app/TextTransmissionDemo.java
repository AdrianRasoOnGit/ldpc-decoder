package ldpc.app;

import ldpc.channel.AwgnChannel;
import ldpc.channel.BpskModem;
import ldpc.channel.LlrInitializer;
import ldpc.codec.BitPacker;
import ldpc.codec.LdpcEncoder;
import ldpc.codec.SystematicToyEncoder;
import ldpc.decoder.DecodeResult;
import ldpc.decoder.LayeredMinSumDecoder;
import ldpc.decoder.LayeredNormalizedMinSumDecoder;
import ldpc.decoder.LdpcDecoder;
import ldpc.decoder.MinSumDecoder;
import ldpc.decoder.NormalizedMinSumDecoder;
import ldpc.decoder.OffsetMinSumDecoder;
import ldpc.matrix.CsrMatrix;
import ldpc.matrix.HMatrixLoader;

import java.util.Arrays;

public final class TextTransmissionDemo {
    private static final String TOY_MATRIX_RESOURCE =
            "/matrices/toy/h_3x6.txt";

    private TextTransmissionDemo() {}

    public static void main(String[] args) throws Exception {
        String message =
                args.length > 0
                        ? args[0]
                        : "Hello LDPC";

        String decoderName =
                args.length > 1
                        ? args[1].toLowerCase()
                        : "layered";

        float sigma =
                args.length > 2
                        ? Float.parseFloat(args[2])
                        : 0.45f;

        CsrMatrix h =
                HMatrixLoader.loadResource(TOY_MATRIX_RESOURCE);

        LdpcEncoder encoder =
                new SystematicToyEncoder();

        LdpcDecoder decoder =
                createDecoder(
                        decoderName,
                        h,
                        20
                );

        int[] inputBits =
                BitPacker.stringToBits(message);

        int originalBitLength =
                inputBits.length;

        int[] paddedBits =
                BitPacker.padToMultiple(
                        inputBits,
                        encoder.messageLength()
                );

        int[] recoveredBits =
                new int[paddedBits.length];

        AwgnChannel channel =
                new AwgnChannel(1234L);

        int frames =
                paddedBits.length / encoder.messageLength();

        int failedFrames = 0;
        int totalBitErrors = 0;
        int totalIterations = 0;

        for (int frame = 0; frame < frames; frame++) {
            int start =
                    frame * encoder.messageLength();

            int[] messageFrame =
                    Arrays.copyOfRange(
                            paddedBits,
                            start,
                            start + encoder.messageLength()
                    );

            int[] codeword =
                    encoder.encode(messageFrame);

            float[] symbols =
                    BpskModem.modulate(codeword);

            float[] received =
                    channel.transmit(
                            symbols,
                            sigma
                    );

            float[] llr =
                    LlrInitializer.compute(
                            received,
                            sigma
                    );

            DecodeResult result =
                    decoder.decode(llr);

            totalIterations += result.iterations();

            if (!result.success()) {
                failedFrames++;
            }

            int[] decodedMessage =
                    Arrays.copyOfRange(
                            result.bits(),
                            0,
                            encoder.messageLength()
                    );

            for (int i = 0; i < decodedMessage.length; i++) {
                int index =
                        start + i;

                recoveredBits[index] =
                        decodedMessage[i];

                if (decodedMessage[i] != paddedBits[index]) {
                    totalBitErrors++;
                }
            }
        }

        int[] trimmedRecoveredBits =
                Arrays.copyOf(
                        recoveredBits,
                        originalBitLength
                );

        String recoveredMessage =
                BitPacker.bitsToString(trimmedRecoveredBits);

        System.out.println("Decoder: " + decoderName);
        System.out.println("Sigma: " + sigma);
        System.out.println("Frames: " + frames);
        System.out.println("Failed frames: " + failedFrames);
        System.out.println("Bit errors: " + totalBitErrors);

        System.out.printf(
                "Average iterations: %.2f%n",
                (double) totalIterations / frames
        );

        System.out.println();
        System.out.println("Original:");
        System.out.println(message);

        System.out.println();
        System.out.println("Recovered:");
        System.out.println(recoveredMessage);
    }

    private static LdpcDecoder createDecoder(
            String decoderName,
            CsrMatrix h,
            int maxIterations
    ) {
        return switch (decoderName) {
            case "minsum" ->
                    new MinSumDecoder(
                            h,
                            maxIterations
                    );

            case "normalized",
                 "normalized-minsum",
                 "nms" ->
                    new NormalizedMinSumDecoder(
                            h,
                            maxIterations,
                            0.75f
                    );

            case "offset",
                 "offset-minsum",
                 "oms" ->
                    new OffsetMinSumDecoder(
                            h,
                            maxIterations,
                            0.25f
                    );

            case "layered",
                 "layered-minsum",
                 "lms" ->
                    new LayeredMinSumDecoder(
                            h,
                            maxIterations,
                            1.0f
                    );

            case "layered-normalized",
                 "layered-normalized-minsum",
                 "lnms" ->
                    new LayeredNormalizedMinSumDecoder(
                            h,
                            maxIterations,
                            0.75f
                    );

            default ->
                    throw new IllegalArgumentException(
                            "Unknown decoder: "
                                    + decoderName
                    );
        };
    }
}
