package ldpc.codec;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class BitPacker {
    private BitPacker() {}

    public static int[] stringToBits(String text) {
        return bytesToBits(
                text.getBytes(StandardCharsets.UTF_8)
        );
    }

    public static String bitsToString(int[] bits) {
        return new String(
                bitsToBytes(bits),
                StandardCharsets.UTF_8
        );
    }

    public static int[] bytesToBits(byte[] bytes) {
        int[] bits = new int[bytes.length * 8];

        for (int i = 0; i < bytes.length; i++) {
            int value = bytes[i] & 0xFF;

            for (int bit = 0; bit < 8; bit++) {
                bits[i * 8 + bit] =
                        (value >>> (7 - bit)) & 1;
            }
        }

        return bits;
    }

    public static byte[] bitsToBytes(int[] bits) {
        if (bits.length % 8 != 0) {
            throw new IllegalArgumentException(
                    "Bit length must be divisible by 8"
            );
        }

        byte[] bytes = new byte[bits.length / 8];

        for (int i = 0; i < bytes.length; i++) {
            int value = 0;

            for (int bit = 0; bit < 8; bit++) {
                value =
                        (value << 1)
                                | (bits[i * 8 + bit] & 1);
            }

            bytes[i] = (byte) value;
        }

        return bytes;
    }

    public static int[] padToMultiple(
            int[] bits,
            int blockSize
    ) {
        int paddedLength =
                ((bits.length + blockSize - 1) / blockSize)
                        * blockSize;

        return Arrays.copyOf(bits, paddedLength);
    }
}
