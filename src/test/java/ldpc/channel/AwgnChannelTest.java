package ldpc.channel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AwgnChannelTest {
    @Test
    void transmitsSameLength() {
        AwgnChannel channel = new AwgnChannel(42L);

        float[] symbols = {1.0f, -1.0f, 1.0f};
        float[] received = channel.transmit(symbols, 0.5f);

        assertEquals(symbols.length, received.length);
    }
}
