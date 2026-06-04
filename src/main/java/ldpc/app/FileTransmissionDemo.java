package ldpc.app;

import ldpc.channel.AwgnChannel;
import ldpc.channel.BpskModem;
import ldpc.channel.LlrInitializer;
import ldpc.codec.BitPacker;
import ldpc.codec.GeneralLdpcEncoder;
import ldpc.decoder.DecodeResult;
import ldpc.decoder.LayeredMinSumDecoder;
import ldpc.decoder.LayeredNormalizedMinSumDecoder;
import ldpc.decoder.LdpcDecoder;
import ldpc.decoder.MinSumDecoder;
import ldpc.decoder.NormalizedMinSumDecoder;
import ldpc.decoder.OffsetMinSumDecoder;
import ldpc.matrix.AlistMatrixLoader;
import ldpc.matrix.CsrMatrix;
import ldpc.matrix.Gf2Rank;
import ldpc.matrix.HMatrixLoader;
import ldpc.matrix.RegularLdpcMatrixFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public final class FileTransmissionDemo {
    private static final String TOY_MATRIX_RESOURCE =
            "/matrices/toy/h_3x6.txt";

    private static final String ALIST_TOY_MATRIX_RESOURCE =
            "/matrices/alist/h_3x6.alist";

    private static final String PEG_10000_R05_AWGN_RESOURCE =
            "/matrices/alist/peg_10000_r05_awgn.alist";

    private FileTransmissionDemo() {}

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            throw new IllegalArgumentException(
                    "Usage: FileTransmissionDemo <input-file> [output-file] [decoder] [matrix] [sigma]"
            );
        }

        Path inputPath =
                Path.of(args[0]);

        Path outputPath =
                args.length > 1
                        ? Path.of(args[1])
                        : defaultOutputPath(inputPath);

        String decoderName =
                args.length > 2
                        ? args[2].toLowerCase()
                        : "lnms";

        String matrixName =
                args.length > 3
                        ? args[3].toLowerCase()
                        : "peg-10000";

        float sigma =
                args.length > 4
                        ? Float.parseFloat(args[4])
                        : 0.45f;

        CsrMatrix h =
                loadMatrix(matrixName);

        int rank =
                Gf2Rank.compute(h);

        double codeRate =
                Gf2Rank.codeRate(h);

        System.out.println("Input: " + inputPath);
        System.out.println("Output: " + outputPath);
        System.out.println("Decoder: " + decoderName);
        System.out.println("Matrix: " + matrixName);
        System.out.println("Rows: " + h.rows());
        System.out.println("Cols: " + h.cols());
        System.out.println("Edges: " + h.edgeCount());
        System.out.println("Rank: " + rank);
        System.out.printf("Code rate: %.6f%n", codeRate);
        System.out.println("Sigma: " + sigma);

        System.out.println();
        System.out.println("Building encoder...");

        GeneralLdpcEncoder encoder =
                new GeneralLdpcEncoder(h);

        LdpcDecoder decoder =
                createDecoder(
                        decoderName,
                        h,
                        30
                );

        byte[] inputBytes =
                Files.readAllBytes(inputPath);

        int[] inputBits =
                BitPacker.bytesToBits(inputBytes);

        int originalBitLength =
                inputBits.length;

        int[] paddedBits =
                BitPacker.padToMultiple(
                        inputBits,
                        encoder.messageLength()
                );

        int[] recoveredBits =
                new int[paddedBits.length];

        int frames =
                paddedBits.length / encoder.messageLength();

        AwgnChannel channel =
                new AwgnChannel(1234L);

        long bitErrors = 0;
        int failedFrames = 0;
        int totalIterations = 0;

        long startTime =
                System.nanoTime();

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
                    encoder.extractMessage(
                            result.bits()
                    );

            for (int i = 0; i < decodedMessage.length; i++) {
                int index =
                        start + i;

                recoveredBits[index] =
                        decodedMessage[i];

                if (decodedMessage[i] != paddedBits[index]) {
                    bitErrors++;
                }
            }

            if ((frame + 1) % 100 == 0 || frame + 1 == frames) {
                System.out.printf(
                        "Processed %d / %d frames%n",
                        frame + 1,
                        frames
                );
            }
        }

        long endTime =
                System.nanoTime();

        int[] trimmedRecoveredBits =
                Arrays.copyOf(
                        recoveredBits,
                        originalBitLength
                );

        byte[] recoveredBytes =
                BitPacker.bitsToBytes(trimmedRecoveredBits);

        Path parent =
                outputPath.toAbsolutePath().getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        Files.write(
                outputPath,
                recoveredBytes
        );

        boolean bytePerfect =
                Arrays.equals(
                        inputBytes,
                        recoveredBytes
                );

        double ber =
                originalBitLength == 0
                        ? 0.0
                        : (double) bitErrors / originalBitLength;

        double averageIterations =
                frames == 0
                        ? 0.0
                        : (double) totalIterations / frames;

        double seconds =
                (endTime - startTime) / 1_000_000_000.0;

        System.out.println();
        System.out.println("Transmission summary");
        System.out.println("--------------------");
        System.out.println("Input bytes: " + inputBytes.length);
        System.out.println("Input bits: " + originalBitLength);
        System.out.println("Message bits per frame: " + encoder.messageLength());
        System.out.println("Codeword bits per frame: " + encoder.codewordLength());
        System.out.println("Frames: " + frames);
        System.out.println("Failed frames: " + failedFrames);
        System.out.println("Bit errors: " + bitErrors);
        System.out.printf("BER: %.12f%n", ber);
        System.out.printf("Average iterations: %.2f%n", averageIterations);
        System.out.printf("Elapsed seconds: %.2f%n", seconds);
        System.out.println("Byte-perfect recovery: " + bytePerfect);
    }

    private static Path defaultOutputPath(Path inputPath) {
        String filename =
                inputPath.getFileName().toString();

        return Path.of(
                "results/transmission/recovered_"
                        + filename
        );
    }

    private static CsrMatrix loadMatrix(String matrixName) throws Exception {
        return switch (matrixName) {
            case "toy" ->
                    HMatrixLoader.loadResource(
                            TOY_MATRIX_RESOURCE
                    );

            case "alist-toy" ->
                    AlistMatrixLoader.loadResource(
                            ALIST_TOY_MATRIX_RESOURCE
                    );

            case "regular",
                 "regular-96" ->
                    RegularLdpcMatrixFactory.create(
                            48,
                            96,
                            3
                    );

            case "regular-504" ->
                    RegularLdpcMatrixFactory.create(
                            252,
                            504,
                            3
                    );

            case "peg-10000",
                 "peg-10000-r05-awgn",
                 "upm-awgn-r05-n10000" ->
                    AlistMatrixLoader.loadResource(
                            PEG_10000_R05_AWGN_RESOURCE
                    );

            default ->
                    throw new IllegalArgumentException(
                            "Unknown matrix: "
                                    + matrixName
                    );
        };
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
