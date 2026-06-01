package ldpc.codec;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BitPackerTest {
    @Test
    void roundTripsAsciiString() {
        String input = "You're in my blood like holy wine, "
            + "you taste so bitter, and so sweet; "
            + "I would drink a case of you, darling, "
            + "and I still be on my feet";

        int[] bits = BitPacker.stringToBits(input);
        String output = BitPacker.bitsToString(bits);

        assertEquals(input, output);
    }

    @Test
    void roundTripsUtf8String() {
        String input = "Información";

        int[] bits = BitPacker.stringToBits(input);
        String output = BitPacker.bitsToString(bits);

        assertEquals(input, output);
    }

    @Test
    void rejectsNonByteAlignedBits() {
        int[] bits = {1, 0, 1};

        assertThrows(
                IllegalArgumentException.class,
                () -> BitPacker.bitsToString(bits)
        );
    }
}
