package boop.services;

import boop.events.*;
import boop.events.topics.BasicEventTopic;
import boop.events.topics.BasicEventTopicSubscription;
import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoopService extends BoopServiceGrpc.BoopServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(BoopService.class);

    private final EventService eventService;

    public <T extends EventTopic> BoopService(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public StreamObserver<BoopEvent> exchangeBoops(StreamObserver<BoopEvent> responseObserver) {
        ReceivedBoopEventObserver observer = new ReceivedBoopEventObserver(user -> {
            try {
                BasicEventTopicSubscription subscription = new BasicEventTopicSubscription(user.getName());
                EventConsumer eventConsumer = eventService.createConsumer(BasicEventTopic.BOOP, subscription);
                eventConsumer.onNext(((boop) -> {
                    try {
                        if (!boop.getBoop().getUserOrBuilder().equals(user)) {
                            responseObserver.onNext(boop.getBoop());
                            return EventProcessor.Result.SUCCESS;
                        } else {
                            return EventProcessor.Result.IGNORED;
                        }
                    } catch (Exception e) {
                        logger.error("Failed to ");
                        return EventProcessor.Result.FAILED;
                    }
                }));
            } catch (EventServiceException e) {
                logger.error("Something went wrong: {}", e.getMessage());
                responseObserver.onError(e);
            }
        });

        try {
            EventProducer<BasicEventTopic> eventProducer = eventService.createProducer(BasicEventTopic.BOOP);
            observer.setEventProducer(eventProducer);
            observer.whenOnCompleted(() -> {
                try {
                    eventProducer.close();
                    // TODO stop consumer too
                } catch (EventServiceException e) {
                    logger.error("Unable to stop Event Producer: {}", e.getMessage());
                }
            });
        } catch (EventServiceException e) {
            logger.error("Something went wrong, unable to setup event producer: {}", e.getMessage());
            responseObserver.onError(e);
        }

        return observer;
    }

    @FunctionalInterface
    private interface BoopUserProcessor {

        /**
         * Process
         * @param user
         */
        void process(BoopUser user);

    }

    public class ReceivedBoopEventObserver implements StreamObserver<BoopEvent> {

        private BoopUser user;

        private final BoopUserProcessor boopUserProcessor;
        private EventProducer<BasicEventTopic> eventProducer;
        private Runnable onCompletedHandler;

        /**
         * Called
         * @param boopUserProcessor called when BoopUser if first received
         */
        private ReceivedBoopEventObserver(BoopUserProcessor boopUserProcessor) {
            this.boopUserProcessor = boopUserProcessor;
        }

        private void processBoopUser(BoopUser user) {
            this.user = user;
            boopUserProcessor.process(user);
        }

        private void setEventProducer(EventProducer<BasicEventTopic> eventProducer) {
            if (this.eventProducer == null) {
                this.eventProducer = eventProducer;
            } else {
                logger.error("An EventProducer has already been assigned to this instance. Closing this one...");
                try {
                    eventProducer.close();
                } catch (EventServiceException e) {
                    logger.error("Failed to close extra event producer: {}", e.getMessage());
                }

            }
        }

        @Override
        public void onNext(BoopEvent boop) {
            if (user == null) {
                processBoopUser(boop.getUser());
            }

            if (eventProducer != null) {
                try {
                    Event event = Event.newBuilder()
                            .setDateSent(Timestamp.newBuilder()
                                    .setSeconds(System.currentTimeMillis()*1000)
                                    .build())
                            .setBoop(boop)
                            .build();
                    eventProducer.emit(event);
                } catch (EventServiceException e) {
                    logger.error("Sadness: {}", e.getMessage());
                }
            } else {
                logger.error("Event producer not initialized; skipping event...");
            }

        }

        @Override
        public void onError(Throwable t) {
            logger.error("Client threw an error: {}", t.getMessage());
        }

        @Override
        public void onCompleted() {
            logger.info("Client signaled completion");
            onCompletedHandler.run();
        }

        public void whenOnCompleted(Runnable runnable) {
            this.onCompletedHandler = runnable;
        }
    }

}
