package boop.services;

import boop.events.*;
import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class BoopServiceTest {

    @Test
    public void testExchangeBoopsReceivesBoops() throws EventServiceException {

        // setup
        EventService mockEventService = mock(EventService.class);
        EventProducer<EventTopic> mockEventProducer = mock(EventProducer.class);
        when(mockEventService.createProducer(any(EventTopic.class)))
                .thenReturn(mockEventProducer);
        EventConsumer<EventTopic> mockEventConsumer = mock(EventConsumer.class);
        when(mockEventService.createConsumer(any(EventTopic.class), any(EventTopicSubscription.class)))
                .thenReturn(mockEventConsumer);
        BoopEvent boop = BoopEvent.newBuilder()
                .setDateSent(Timestamp.newBuilder()
                        .setSeconds(15)
                        .build())
                .setUser(BoopUser.newBuilder()
                        .setName("John")
                        .build())
                .build();

        // tests
        StreamObserver<BoopEvent> sendStreamObserver = new BoopService(mockEventService)
                .exchangeBoops(mock(StreamObserver.class));

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);

        sendStreamObserver.onNext(boop);
        verify(mockEventProducer).emit(eventCaptor.capture());
        assertEquals(boop, eventCaptor.getValue().getBoop());
    }

    @Test
    public void testExchangeBoopsSendsBoops() throws EventServiceException {
        // setup
        EventService eventService = mock(EventService.class);
        EventProducer<EventTopic> eventProducer = mock(EventProducer.class);
        when(eventService.createProducer(any(EventTopic.class)))
                .thenReturn(eventProducer);

        class MockEventConsumer implements EventConsumer<EventTopic> {
            private EventProcessor eventProcessor;

            public void receive(Event event) {
                eventProcessor.process(event);
            }

            @Override
            public void onNext(EventProcessor eventProcessor) {
                this.eventProcessor = eventProcessor;
            }

            @Override
            public void close() throws EventServiceException {}
        };
        MockEventConsumer eventConsumer = new MockEventConsumer();
        when(eventService.createConsumer(any(EventTopic.class), any(EventTopicSubscription.class)))
                .thenReturn(eventConsumer);

        StreamObserver<BoopEvent> responsetreamObserver = (StreamObserver<BoopEvent>) mock(StreamObserver.class);

        // tests
        StreamObserver<BoopEvent> sendStreamObserver = new BoopService(eventService)
                .exchangeBoops(responsetreamObserver);
        BoopUser plankton = BoopUser.newBuilder()
                .setName("Plankton")
                .build();
        Event spongebobBoop = Event.newBuilder()
                .setBoop(BoopEvent.newBuilder()
                        .setUser(BoopUser.newBuilder()
                                .setName("Spongebob")
                                .build())
                        .build())
                .build();
        Event planktonBoop = Event.newBuilder()
                .setBoop(BoopEvent.newBuilder()
                        .setUser(plankton)
                        .build())
                .build();
        Event patrickBoop = Event.newBuilder()
                .setBoop(BoopEvent.newBuilder()
                        .setUser(BoopUser.newBuilder()
                                .setName("Patrick")
                                .build())
                        .build())
                .build();

        // send an event to initialize the consumer for Plankton
        sendStreamObserver.onNext(BoopEvent.newBuilder()
                .setUser(plankton)
                .build());

        // send some back from Friends and Self
        eventConsumer.receive(spongebobBoop);
        eventConsumer.receive(planktonBoop); // should be ignored
        eventConsumer.receive(patrickBoop);

        ArgumentCaptor<BoopEvent> eventCaptor = ArgumentCaptor.forClass(BoopEvent.class);

        verify(responsetreamObserver, times(2)).onNext(eventCaptor.capture());
        List<BoopEvent> events = eventCaptor.getAllValues();
        assertEquals(spongebobBoop.getBoop(), events.get(0));
        assertEquals(patrickBoop.getBoop(), events.get(1));
    }

}
