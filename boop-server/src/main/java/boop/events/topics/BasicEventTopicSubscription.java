package boop.events.topics;

import boop.events.EventTopicSubscription;

public class BasicEventTopicSubscription implements EventTopicSubscription {

    private final String identifier;

    public BasicEventTopicSubscription(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

}
