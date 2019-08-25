package boop.events.pulsar;

import boop.events.EventService;
import boop.events.EventTopic;
import boop.events.EventTopicSubscription;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public class PulsarService implements EventService {

    private final PulsarClient pulsarClient;

    public PulsarService() throws PulsarClientException {
        this.pulsarClient = PulsarClient.builder()
                .serviceUrl("pulsar://localhost:6650")
                .build();
    }

    public <T extends EventTopic> PulsarProducer<T> createProducer(T topic) throws PulsarServiceException {
        try {
            return new PulsarProducer(pulsarClient, topic);
        } catch (PulsarClientException e) {
            // log...
            throw new PulsarServiceException(e.getMessage());
        }
    }

    public <T extends EventTopic, S extends EventTopicSubscription> PulsarConsumer createConsumer(T topic, S subscription) throws PulsarServiceException {
        try {
            return new PulsarConsumer(pulsarClient, topic, subscription);
        } catch (PulsarClientException e) {
            // log...
            throw new PulsarServiceException(e.getMessage());
        }
    }

    public void closeAll() throws PulsarServiceException {
        try {
            pulsarClient.close();
        } catch (PulsarClientException e) {
            // log...
            throw new PulsarServiceException(e.getMessage());
        }
    }

}
