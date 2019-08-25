package boop.events.pulsar;

import boop.events.*;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.TimeUnit;

public class PulsarConsumer<T extends EventTopic> implements EventConsumer<T> {

    private static final Logger logger = LoggerFactory.getLogger(PulsarConsumer.class);

    private final org.apache.pulsar.client.api.Consumer<byte[]> pulsarConsumer;
    private final EventTopic eventTopic;
    private Boolean closed = true;
    private EventProcessor eventProcessor;

    public PulsarConsumer(PulsarClient pulsarClient, T eventTopic, EventTopicSubscription subscription) throws PulsarClientException {
        this.pulsarConsumer = pulsarClient.newConsumer()
                .topic(eventTopic.getIdentifier())
                .subscriptionName(subscription.getIdentifier())
                .subscribe();
        this.listen();
        this.eventTopic = eventTopic;
    }

    private void listen() {
        closed = false;

        new Thread(() -> {
            while (!closed) {
                logger.info("Pulsar Consumer listening...");
                try {
                    getNext();
                } catch (Exception e) {
                    logger.error("Consumer error: {}", e.getMessage());
                }
            }
        }).start();
    }

    private void getNext() throws PulsarClientException, InvalidProtocolBufferException {
        Message rawMessage = pulsarConsumer.receive(500, TimeUnit.MILLISECONDS);
        if (rawMessage != null) {
            byte[] data = rawMessage.getData();

            EventProcessor.Result result = eventProcessor.process(Event.newBuilder()
                    .setDateSent(Timestamp.newBuilder()
                            .setSeconds(System.currentTimeMillis()*1000)
                            .build())
                    .setBoop(BoopEvent.parseFrom(data))
                    .build());

            switch (result) {
                case FAILED:
                    logger.error("Failed to process event");
                case SUCCESS:
                     pulsarConsumer.acknowledge(rawMessage);
            }
        }
    }

    public void onNext(EventProcessor eventProcessor) {
        this.eventProcessor = eventProcessor;
    }

    public void close() throws PulsarServiceException {
        try {
            closed = true;
            pulsarConsumer.close();
        } catch (PulsarClientException e) {
            logger.error("Failed to close Pulsar consumer: {}", e.getMessage());
            throw new PulsarServiceException(e.getMessage());
        }
    }

}
