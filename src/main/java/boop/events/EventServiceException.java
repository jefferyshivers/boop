package boop.events;

public abstract class EventServiceException extends Exception {

    public EventServiceException(final String message) {
        super(message);
    }

}
