package boop.events;

public interface EventProducer<T extends EventTopic> {

    void emit(Event event) throws EventServiceException;

    void close() throws EventServiceException;

}
