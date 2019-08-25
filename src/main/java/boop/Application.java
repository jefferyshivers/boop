package boop;

import boop.events.EventService;
import boop.events.EventServiceException;
import boop.events.pulsar.PulsarService;
import boop.services.BoopService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    private final Integer port;
    private final Server server;
    private final EventService eventService;

    private Application(Integer port) {
        this.port = port;
        try {
            this.eventService = new PulsarService();
        } catch (PulsarClientException e) {
            logger.error("Failed to start Pulsar client: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        this.server = ServerBuilder.forPort(port)
                .addService(new BoopService(this.eventService))
                .build();
    }

    private void start() {
        try {
            server.start();
        } catch (IOException e) {
            logger.error("Failed to start server: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("shutting down gRPC server since JVM is shutting down");
            Application.this.stop();
        }));

        logger.info("server started on port {}", port);
    }

    private void stop() {
        try {
            eventService.closeAll();
        } catch (EventServiceException e) {
            logger.error("Failed to close event service client: {}", e.getMessage());
        }
        server.shutdown();
        logger.info("server successfully shut down");
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() {
        try {
            server.awaitTermination();
        } catch (InterruptedException e) {
            logger.error("Unable to block thread: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void main(String... args) {
        Application application = new Application(7777);
        application.start();
        application.blockUntilShutdown();
    }

}
