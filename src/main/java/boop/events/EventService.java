package boop.events;

public interface EventService {

    <T extends EventTopic> EventProducer<T> createProducer(T topic) throws EventServiceException;

    <T extends EventTopic, S extends EventTopicSubscription> EventConsumer<T> createConsumer(T topic, S subscription) throws EventServiceException;

    void closeAll() throws EventServiceException;

}
