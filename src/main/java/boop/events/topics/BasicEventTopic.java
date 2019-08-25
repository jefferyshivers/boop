package boop.events.topics;

import boop.events.EventTopic;

public class BasicEventTopic<T> implements EventTopic {

    private final String topicIdentifier;

    public BasicEventTopic(final String topicIdentifier) {
        this.topicIdentifier = topicIdentifier;
    }

    public String getIdentifier() {
        return topicIdentifier;
    }

    public static final BasicEventTopic BOOP = new BasicEventTopic("boop");

}
