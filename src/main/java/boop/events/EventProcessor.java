package boop.events;

@FunctionalInterface
public interface EventProcessor {

    enum Result { SUCCESS, FAILED, IGNORED }

    /**
     * Process
     * @param event
     * @return processed
     */
    Result process(Event event);

}
