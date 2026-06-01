package ldpc.codec;

/**
 * Systematic encoder for the toy 3x6 parity-check matrix:
 *
 * H =
 * row 0: 0 1 3
 * row 1: 1 2 4
 * row 2: 0 2 5
 *
 * Constraints:
 * x0 + x1 + x3 = 0
 * x1 + x2 + x4 = 0
 * x0 + x2 + x5 = 0
 *
 * Systematic layout:
 * message = [x0, x1, x2]
 * parity  = [x3, x4, x5]
 */
public final class SystematicToyEncoder implements LdpcEncoder {
    @Override
    public int messageLength() {
        return 3;
    }

    @Override
    public int codewordLength() {
        return 6;
    }

    @Override
    public int[] encode(int[] messageBits) {
        if (messageBits.length != messageLength()) {
            throw new IllegalArgumentException("Toy encoder expects exactly 3 message bits");
        }

        int x0 = messageBits[0] & 1;
        int x1 = messageBits[1] & 1;
        int x2 = messageBits[2] & 1;

        int x3 = x0 ^ x1;
        int x4 = x1 ^ x2;
        int x5 = x0 ^ x2;

        return new int[] {
                x0,
                x1,
                x2,
                x3,
                x4,
                x5
        };
    }
}
