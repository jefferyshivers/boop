package boop.events.pulsar;

import boop.events.EventService;
import boop.events.EventTopic;
import boop.events.EventTopicSubscription;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PulsarService implements EventService {

    private static final Logger logger = LoggerFactory.getLogger(PulsarService.class);

    private final PulsarClient pulsarClient;

    public PulsarService() throws PulsarClientException {
        this.pulsarClient = PulsarClient.builder()
                .serviceUrl("pulsar://localhost:6650")
                .build();
        logger.info("Pulsar client started");
    }

    public <T extends EventTopic> PulsarProducer<T> createProducer(T topic) throws PulsarServiceException {
        try {
            return new PulsarProducer(pulsarClient, topic);
        } catch (PulsarClientException e) {
            throw new PulsarServiceException(e.getMessage());
        }
    }

    public <T extends EventTopic, S extends EventTopicSubscription> PulsarConsumer createConsumer(T topic, S subscription) throws PulsarServiceException {
        try {
            return new PulsarConsumer(pulsarClient, topic, subscription);
        } catch (PulsarClientException e) {
            throw new PulsarServiceException(e.getMessage());
        }
    }

    public void closeAll() throws PulsarServiceException {
        try {
            pulsarClient.close();
        } catch (PulsarClientException e) {
            throw new PulsarServiceException(e.getMessage());
        }
    }

}
