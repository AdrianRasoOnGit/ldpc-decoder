package ldpc.codec;

public interface LdpcEncoder {
    int messageLength();

    int codewordLength();

    int[] encode(int[] messageBits);
}
