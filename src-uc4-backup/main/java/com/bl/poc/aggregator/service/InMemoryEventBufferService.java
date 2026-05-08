package com.bl.poc.aggregator.service;

import com.bl.poc.aggregator.model.PaymentEvent;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class InMemoryEventBufferService {

    private final List<PaymentEvent> buffer = new ArrayList<>();
    private Instant firstEventBufferedAt;

    public synchronized int addEvent(PaymentEvent paymentEvent) {

        if (buffer.isEmpty()) {
            firstEventBufferedAt = Instant.now();
        }

        buffer.add(paymentEvent);
        return buffer.size();
    }

    public synchronized int getBufferSize() {
        return buffer.size();
    }

    public synchronized boolean isEmpty() {
        return buffer.isEmpty();
    }

    public synchronized Instant getFirstEventBufferedAt() {
        return firstEventBufferedAt;
    }

    public synchronized List<PaymentEvent> getAllEvents() {
        return new ArrayList<>(buffer);
    }

    public synchronized List<PaymentEvent> drainBuffer() {

        List<PaymentEvent> drainedEvents = new ArrayList<>(buffer);

        buffer.clear();
        firstEventBufferedAt = null;

        return drainedEvents;
    }

    public synchronized void clearBuffer() {
        buffer.clear();
        firstEventBufferedAt = null;
    }
}