package com.bl.poc.aggregator.service;

import com.bl.poc.aggregator.config.BatchProperties;
import com.bl.poc.aggregator.model.BatchEvent;
import com.bl.poc.aggregator.model.PaymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class InMemoryBatchStateService {

    private static final Logger log =
            LoggerFactory.getLogger(InMemoryBatchStateService.class);

    private final BatchProperties batchProperties;

    private final List<PaymentEvent> buffer = new ArrayList<>();
    private final List<BatchEvent> createdBatches = new ArrayList<>();

    private Instant firstEventBufferedAt;

    public InMemoryBatchStateService(BatchProperties batchProperties) {
        this.batchProperties = batchProperties;
    }

    public synchronized Map<String, Object> addEvent(PaymentEvent event) {

        if (buffer.isEmpty()) {
            firstEventBufferedAt = Instant.now();
        }

        buffer.add(event);

        log.info(
                "UC5_EVENT_ADDED_TO_IN_MEMORY_BUFFER transactionId={} bufferSize={} limitation=state_not_durable",
                event.getTransactionId(),
                buffer.size()
        );

        if (buffer.size() >= batchProperties.getMaxBatchSize()) {
            createBatch("SIZE_LIMIT_REACHED");
        }

        return Map.of(
                "service", "payment-batch-state-service",
                "status", "BUFFERED",
                "transactionId", event.getTransactionId(),
                "bufferSize", buffer.size(),
                "createdBatchCount", createdBatches.size(),
                "limitation", "Buffer and batch history are in memory only"
        );
    }

    public synchronized void createPartialBatchIfExpired() {

        if (buffer.isEmpty() || firstEventBufferedAt == null) {
            return;
        }

        long elapsedMs =
                Duration.between(firstEventBufferedAt, Instant.now()).toMillis();

        if (elapsedMs >= batchProperties.getMaxWaitMs()) {
            createBatch("TIME_LIMIT_REACHED");
        }
    }

    private void createBatch(String reason) {

        List<PaymentEvent> batchRecords = new ArrayList<>(buffer);

        BatchEvent batch = new BatchEvent(
                "BATCH-" + UUID.randomUUID(),
                reason,
                batchRecords.size(),
                Instant.now(),
                batchRecords
        );

        createdBatches.add(batch);
        buffer.clear();
        firstEventBufferedAt = null;

        log.info(
                "UC5_BATCH_CREATED batchId={} reason={} recordCount={} limitation=batch_history_in_memory_only",
                batch.getBatchId(),
                batch.getReason(),
                batch.getRecordCount()
        );
    }

    public synchronized Map<String, Object> getBufferStatus() {
        return Map.of(
                "service", "payment-batch-state-service",
                "bufferSize", buffer.size(),
                "createdBatchCount", createdBatches.size(),
                "firstEventBufferedAt", firstEventBufferedAt == null ? "NA" : firstEventBufferedAt.toString(),
                "limitation", "This buffer and batch history will be lost after service restart"
        );
    }

    public synchronized List<BatchEvent> getCreatedBatches() {
        return new ArrayList<>(createdBatches);
    }

    public synchronized void clearBuffer() {
        buffer.clear();
        firstEventBufferedAt = null;
        log.info("UC5_IN_MEMORY_BUFFER_CLEARED");
    }

    public synchronized void clearBatches() {
        createdBatches.clear();
        log.info("UC5_IN_MEMORY_BATCH_HISTORY_CLEARED");
    }
}