package com.bl.poc.aggregator.model;

import java.time.Instant;
import java.util.List;

public class BatchEvent {

    private String batchId;
    private String reason;
    private int recordCount;
    private Instant createdAt;
    private List<PaymentEvent> events;

    public BatchEvent() {
    }

    public BatchEvent(
            String batchId,
            String reason,
            int recordCount,
            Instant createdAt,
            List<PaymentEvent> events
    ) {
        this.batchId = batchId;
        this.reason = reason;
        this.recordCount = recordCount;
        this.createdAt = createdAt;
        this.events = events;
    }

    public String getBatchId() {
        return batchId;
    }

    public String getReason() {
        return reason;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<PaymentEvent> getEvents() {
        return events;
    }
}