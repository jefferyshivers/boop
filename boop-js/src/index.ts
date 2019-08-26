const grpc = require('grpc');
const protobuf = require('@grpc/proto-loader');

// er, this only will work from the mono repo though?
const PROTO_PATH = __dirname + '../../boop-proto/src/main/proto/services/boop.proto';
// var PROTO_PATH = __dirname + '../../../services/boop.proto';

const packageDefinition = protobuf.loadSync(
    PROTO_PATH,
    {keepCase: true,
        longs: String,
        enums: String,
        defaults: true,
        oneofs: true
    });

const protoDescriptor = grpc.loadPackageDefinition(packageDefinition);

class Server {

    private _host: String;
    private _port: Number = 80;
    private _apiKey: String;

    public setHost(host: String): void {
        this._host = host;
    }

    public setPort(port: Number): void {
        this._port = port;
    }

    public setApiKey(apiKey: String): void {
        this._apiKey = apiKey;
    }

    public validate(): Server {
        if (this._host == null || this._host == undefined) {
            throw new BoopClientError("Endpoint must be provided");
        } else if (this._apiKey == null || this._apiKey == undefined) {
            throw new BoopClientError("API key must be provided");
        }
        return this;
    }

}

class BoopClientError extends Error {}

interface ReceivedBoopProcessor {
    (boopEvent: String): void;
}

interface ErrorCallback {
    (error: Error): void;
}


class BoopClient {


    private _server: Server;
    // private _connectedStub: BoopServiceGrpc.BoopServiceStub;
    // private observer: ServerResponseObserver;
    // private observable: StreamObserver<BoopEvent>;

    public constructor(server: Server) {
        this._server = BoopClient.validateServer(server);
    }

    private static validateServer(server: Server): Server {
        if (server == null || server == undefined) {
            throw new BoopClientError("Cannot be built without Server details");
        }

        return server.validate();
    }

    private validateProcessor(processor: ReceivedBoopProcessor): ReceivedBoopProcessor {
        if (!!processor) {
            throw new BoopClientError("Processor cannot be null");
        }

        return processor;
    }

    public isConnected(): Boolean {
        // TODO:
        // return (observable != null) && (observer != null) && (connectedStub != null);
        return true
    }

    public disconnect(): void {
        // TODO:
        // observable.onCompleted();
        // observable = null;
        // observer = null;
        // connectedStub = null;
    }

    public connect(processor: ReceivedBoopProcessor): void {
        this.validateProcessor(processor)

        //TODO: gRPC stuff
        // Channel channel = ManagedChannelBuilder.forAddress(server.host, server.port)
        //     .build();
        //
        // connectedStub = BoopServiceGrpc.newStub(channel);
        // observer = new ServerResponseObserver(processor);
        // observable = connectedStub.exchangeBoops(observer);
    }


    /*
    TODO stream observer

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
     */

    public onError(errorProcessor: ErrorCallback): void {
        // observer.setErrorProcessor(errorProcessor);
    }

    public send(boop: String): void {
        // if (observable == null) {
        //     throw new BoopClientException("Observable not set, nothing to call `onNext`");
        // }
        // observable.onNext(boop);
    }

    public static builder(): BoopClientBuilder {
        return new BoopClientBuilder();
    }

}

class BoopClientBuilder {

    private _server: Server;

    public constructor() {
        this._server = new Server();
    }

    public host(host: String): BoopClientBuilder {
        this._server.setHost(host);
        return this;
    }

    public port(port: Number): BoopClientBuilder {
        this._server.setPort(port);
        return this;
    }

    public apiKey(apiKey: String): BoopClientBuilder {
        this._server.setApiKey(apiKey);
        return this;
    }

    public build(): BoopClient {
        return new BoopClient(this._server);
    }

}

export default {
    BoopClientBuilder,
    BoopClient,
    BoopClientError,
    protoDescriptor
}
