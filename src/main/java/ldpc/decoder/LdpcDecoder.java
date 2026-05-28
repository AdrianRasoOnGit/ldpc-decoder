package ldpc.decoder;

public interface LdpcDecoder {
    DecodeResult decode(float[] channelLlr);
}
