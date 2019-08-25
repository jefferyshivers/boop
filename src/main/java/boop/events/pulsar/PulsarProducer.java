package boop.events.pulsar;

import boop.events.Event;
import boop.events.EventProducer;
import boop.events.EventTopic;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public class PulsarProducer<T extends EventTopic> implements EventProducer<T> {

    private final Producer<byte[]> pulsarProducer;
    private final T eventTopic;

    public PulsarProducer(PulsarClient pulsarClient, T eventTopic) throws PulsarClientException {
        this.pulsarProducer = pulsarClient.newProducer()
                .topic(eventTopic.getIdentifier())
                .create();
        this.eventTopic = eventTopic;
    }

    public void emit(Event event) throws PulsarServiceException {
        try {
            pulsarProducer.send(event.toByteArray());
        } catch (PulsarClientException e) {
            // log...
            throw new PulsarServiceException(e.getMessage());
        }
    }

    public void close() throws PulsarServiceException {
        try {
            pulsarProducer.close();
        } catch (PulsarClientException e) {
            // log...
            throw new PulsarServiceException(e.getMessage());
        }
    }

}
