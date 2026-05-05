package com.bl.poc.aggregator.service;

import com.bl.poc.aggregator.model.PaymentEvent;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InMemoryEventBufferService {

    private final List<PaymentEvent> buffer = new ArrayList<>();

    public synchronized int addEvent(PaymentEvent paymentEvent) {
        buffer.add(paymentEvent);
        return buffer.size();
    }

    public synchronized int getBufferSize() {
        return buffer.size();
    }

    public synchronized List<PaymentEvent> getAllEvents() {
        return new ArrayList<>(buffer);
    }

    public synchronized void clearBuffer() {
        buffer.clear();
    }
}