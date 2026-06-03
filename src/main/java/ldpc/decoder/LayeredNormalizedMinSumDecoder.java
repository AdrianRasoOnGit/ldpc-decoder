package ldpc.decoder;

import ldpc.matrix.CsrMatrix;

public final class LayeredNormalizedMinSumDecoder implements LdpcDecoder {
    private final LayeredMinSumDecoder delegate;

    public LayeredNormalizedMinSumDecoder(
            CsrMatrix h,
            int maxIterations,
            float alpha
    ) {
        this.delegate =
                new LayeredMinSumDecoder(
                        h,
                        maxIterations,
                        alpha
                );
    }

    @Override
    public DecodeResult decode(float[] channelLlr) {
        return delegate.decode(channelLlr);
    }
}
