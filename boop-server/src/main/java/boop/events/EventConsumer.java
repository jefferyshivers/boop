package boop.events;

public interface EventConsumer<T extends EventTopic> {

    void onNext(EventProcessor eventProcessor);

    void close() throws EventServiceException;

}
