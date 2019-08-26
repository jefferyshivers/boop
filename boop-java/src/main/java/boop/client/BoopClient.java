package boop.client;

import boop.events.BoopEvent;
import boop.services.BoopServiceGrpc;
import io.grpc.*;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.MatchesPattern;
import java.util.function.UnaryOperator;

public class BoopClient {

    private static final Logger logger = LoggerFactory.getLogger(BoopClient.class);

    private final Server server;
    private BoopServiceGrpc.BoopServiceStub connectedStub;
    private ServerResponseObserver observer;
    private StreamObserver<BoopEvent> observable;

    private BoopClient(final Server server) throws BoopClientException {
        this.server = validate(server);
    }

    private Server validate(Server server) throws BoopClientException {
        if (server == null) {
            throw new BoopClientException("Cannot be built without Server details");
        }

        return server.validate();
    }

    private void validate(ReceivedBoopProcessor processor) throws BoopClientException {
        if (processor == null) {
            throw new BoopClientException("Processor cannot be null");
        }
    }

    public void connect(ReceivedBoopProcessor processor) throws BoopClientException {
        validate(processor);

        Channel channel = ManagedChannelBuilder.forAddress(server.host, server.port)
                .build();

        connectedStub = BoopServiceGrpc.newStub(channel);
        observer = new ServerResponseObserver(processor);
        observable = connectedStub.exchangeBoops(observer);
    }

    public boolean isConnected() {
        return (observable != null) && (observer != null) && (connectedStub != null);
    }

    public void disconnect() throws BoopClientException {
        observable.onCompleted();
        observable = null;
        observer = null;
        connectedStub = null;
    }

    public void onError(UnaryOperator<Throwable> errorProcessor) {
        observer.setErrorProcessor(errorProcessor);
    }

    private class ServerResponseObserver implements StreamObserver<BoopEvent> {

        private final ReceivedBoopProcessor processor;
        private UnaryOperator<Throwable> errorProcessor;

        private ServerResponseObserver(ReceivedBoopProcessor processor) {
            this.processor = processor;
        }

        private void setErrorProcessor(UnaryOperator<Throwable> errorProcessor) {
            this.errorProcessor = errorProcessor;
        }

        @Override
        public void onNext(BoopEvent boop) {
            if (processor == null) {
                logger.error("Could not process boop, processor was never set");
            } else {
                processor.process(boop);
            }
        }

        @Override
        public void onCompleted() {
            observable = null;
            observer = null;
            connectedStub = null;
        }

        @Override
        public void onError(Throwable t) {
            if (errorProcessor != null) {
                errorProcessor.apply(t);
            }
        }

    }

    public void send(BoopEvent boop) throws BoopClientException {
        if (observable == null) {
            throw new BoopClientException("Observable not set, nothing to call `onNext`");
        }
        observable.onNext(boop);
    }

    @FunctionalInterface
    public interface ReceivedBoopProcessor {

        public void process(BoopEvent boopEvent);

    }

    public static BoopClientBuilder builder() {
        return new BoopClientBuilder();
    }

    public static class BoopClientBuilder {

        private final Server server;

        private BoopClientBuilder() {
            this.server = new Server();
        }

        public BoopClientBuilder host(String host) {
            server.setHost(host);
            return this;
        }

        public BoopClientBuilder port(int port) {
            server.setPort(port);
            return this;
        }

        public BoopClientBuilder apiKey(String apiKey) {
            server.setApiKey(apiKey);
            return this;
        }

        public BoopClient build() throws BoopClientException {
            return new BoopClient(server);
        }

    }

    private static class Server {

        private String host;
        @MatchesPattern(value = "\\d{2,5}")
        private int port = 80;
        private String apiKey;

        private Server() {}

        private void setHost(String host) {
            this.host = host;
        }

        private void setPort(int port) {
            this.port = port;
        }

        private void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        private Server validate() throws BoopClientException {
            if (host == null || host.isBlank()) {
                throw new BoopClientException("Endpoint must be provided");
            } else if (apiKey == null || apiKey.isBlank()) {
                throw new BoopClientException("API key must be provided");
            }
            return this;
        }

    }

}
