package boop.events;

@FunctionalInterface
public interface EventProcessor {

    enum Result { SUCCESS, FAILED, IGNORED }

    /**
     * Process
     * @param event as byte array
     * @return processed
     */
    Result process(byte[] event);

}
