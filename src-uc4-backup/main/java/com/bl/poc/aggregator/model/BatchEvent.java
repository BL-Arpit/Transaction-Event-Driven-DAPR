package com.bl.poc.aggregator.model;

import java.time.Instant;
import java.util.List;

public class BatchEvent {

    private String batchId;
    private int recordCount;
    private Instant firstEventTimestamp;
    private Instant lastEventTimestamp;
    private Instant createdAt;
    private List<PaymentEvent> records;

    public BatchEvent() {
    }

    public BatchEvent(
            String batchId,
            int recordCount,
            Instant firstEventTimestamp,
            Instant lastEventTimestamp,
            Instant createdAt,
            List<PaymentEvent> records
    ) {
        this.batchId = batchId;
        this.recordCount = recordCount;
        this.firstEventTimestamp = firstEventTimestamp;
        this.lastEventTimestamp = lastEventTimestamp;
        this.createdAt = createdAt;
        this.records = records;
    }

    public String getBatchId() {
        return batchId;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public Instant getFirstEventTimestamp() {
        return firstEventTimestamp;
    }

    public Instant getLastEventTimestamp() {
        return lastEventTimestamp;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<PaymentEvent> getRecords() {
        return records;
    }
}