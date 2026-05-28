package ldpc.simulation;

import ldpc.decoder.LdpcDecoder;
import ldpc.matrix.CsrMatrix;

@FunctionalInterface
public interface DecoderFactory {
    LdpcDecoder create(CsrMatrix h, int MaxIterations);
}
