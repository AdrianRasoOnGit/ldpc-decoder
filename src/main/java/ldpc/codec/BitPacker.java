package ldpc.codec;

import java.nio.charset.StandardCharsets;

public final class BitPacker {
    private BitPacker() {}

    public static int[] stringToBits(String text) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        int[] bits = new int[bytes.length * 8];

        for (int i = 0; i < bytes.length; i++) {
            int value = bytes[i] & 0xFF;

            for (int bit = 0; bit < 8; bit++) {
                bits[i * 8 + bit] = (value >>> (7 - bit)) & 1;
            }
        }

        return bits;
    }

    public static String bitsToString(int[] bits) {
        if (bits.length % 8 != 0) {
            throw new IllegalArgumentException("Bit length must be divisible by 8");
        }

        byte[] bytes = new byte[bits.length / 8];

        for (int i = 0; i < bytes.length; i++) {
            int value = 0;

            for (int bit = 0; bit < 8; bit++) {
                value = (value << 1) | bits[i * 8 + bit];
            }

            bytes[i] = (byte) value;
        }

        return new String(bytes, StandardCharsets.UTF_8);
    }
}
