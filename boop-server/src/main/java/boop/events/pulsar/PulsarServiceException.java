package boop.events.pulsar;

import boop.events.EventServiceException;

public class PulsarServiceException extends EventServiceException {

    public PulsarServiceException(final String message) {
        super(message);
    }

}
