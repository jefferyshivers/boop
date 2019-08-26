package boop.client;

import boop.events.BoopEvent;
import boop.events.BoopUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BoopClient")
public class BoopClientTest {

    @Nested
    @DisplayName("BoopClientBuilder")
    class BoopClientBuilderTest {

        @Test
        @DisplayName("happy `build()`s")
        public void testBuild() throws BoopClientException {
            BoopClient.builder()
                    .host("test.com")
                    .apiKey("123")
                    .build();

            BoopClient.builder()
                    .host("test.com")
                    .port(123)
                    .apiKey("123")
                    .build();
        }

        @Test
        @DisplayName("validations")
        public void testValidatesServer() {
            assertThrows(BoopClientException.class, () -> {
                BoopClient.builder()
                        .build();
            });

            assertThrows(BoopClientException.class, () -> {
                BoopClient.builder()
                        .host("test.com")
                        .build();
            });

            assertThrows(BoopClientException.class, () -> {
                BoopClient.builder()
                        .apiKey("123")
                        .build();
            });
        }
    }

    @Test
    @DisplayName("connect")
    public void testConnect() throws BoopClientException {
        BoopClient client = BoopClient.builder()
                .host("localhost")
                .apiKey("123")
                .build();
        assertFalse(client.isConnected());
        client.connect((boop -> {}));
        assertTrue(client.isConnected());
    }

    @Test
    @DisplayName("disconnect")
    public void testDisconnect() throws BoopClientException {
        BoopClient client = BoopClient.builder()
                .host("localhost")
                .apiKey("123")
                .build();
        client.connect((boop -> {}));
        assertTrue(client.isConnected());
        client.disconnect();
        assertFalse(client.isConnected());
    }

    @Test
    @DisplayName("send")
    public void testSend() throws BoopClientException {
        BoopClient client = BoopClient.builder()
                .host("localhost")
                .apiKey("123")
                .build();
        client.connect((boop -> {}));
        client.send(BoopEvent.newBuilder()
                .setUser(BoopUser.newBuilder()
                        .setName("Sandy Cheeks")
                        .build())
                .build());
    }

}
