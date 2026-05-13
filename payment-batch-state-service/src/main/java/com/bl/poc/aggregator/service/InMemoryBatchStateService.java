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

    private static final String BUFFER_KEY =
            "payment-buffer";

    private static final String CREATED_BATCHES_KEY =
            "payment-created-batches";

    private static final String FIRST_EVENT_BUFFERED_AT_KEY =
            "payment-first-event-buffered-at";

    private final BatchProperties batchProperties;
    private final DaprStateStoreClient daprStateStoreClient;

    public InMemoryBatchStateService(
            BatchProperties batchProperties,
            DaprStateStoreClient daprStateStoreClient
    ) {
        this.batchProperties = batchProperties;
        this.daprStateStoreClient = daprStateStoreClient;
    }

    public synchronized Map<String, Object> addEvent(PaymentEvent event) {

        log.info(
                "UC8_EVENT_RECEIVED_FOR_DURABLE_BUFFER transactionId={}",
                event.getTransactionId()
        );

        List<PaymentEvent> buffer = loadBuffer();
        List<BatchEvent> createdBatches = loadCreatedBatches();

        String firstEventBufferedAt =
                daprStateStoreClient.getState(
                        FIRST_EVENT_BUFFERED_AT_KEY,
                        String.class,
                        null
                );

        if (buffer.isEmpty() || firstEventBufferedAt == null) {
            firstEventBufferedAt = Instant.now().toString();

            daprStateStoreClient.saveState(
                    FIRST_EVENT_BUFFERED_AT_KEY,
                    firstEventBufferedAt
            );

            log.info(
                    "UC8_FIRST_EVENT_BUFFER_TIME_SAVED firstEventBufferedAt={}",
                    firstEventBufferedAt
            );
        }

        buffer.add(event);

        saveBuffer(buffer);

        log.info(
                "UC8_EVENT_ADDED_TO_DAPR_STATE_BUFFER transactionId={} bufferSize={} durableStore=statestore",
                event.getTransactionId(),
                buffer.size()
        );

        if (buffer.size() >= batchProperties.getMaxBatchSize()) {
            createBatch(
                    "SIZE_LIMIT_REACHED",
                    buffer,
                    createdBatches
            );
        }

        int latestBufferSize = loadBuffer().size();
        int latestBatchCount = loadCreatedBatches().size();

        return Map.of(
                "service", "payment-batch-state-service",
                "status", "BUFFERED",
                "transactionId", event.getTransactionId(),
                "bufferSize", latestBufferSize,
                "createdBatchCount", latestBatchCount,
                "stateStore", "Dapr statestore",
                "limitation", "Durable buffer added, but idempotency is not implemented yet"
        );
    }

    public synchronized void createPartialBatchIfExpired() {

        List<PaymentEvent> buffer = loadBuffer();

        if (buffer.isEmpty()) {
            return;
        }

        String firstEventBufferedAtValue =
                daprStateStoreClient.getState(
                        FIRST_EVENT_BUFFERED_AT_KEY,
                        String.class,
                        null
                );

        if (firstEventBufferedAtValue == null) {
            return;
        }

        Instant firstEventBufferedAt =
                Instant.parse(firstEventBufferedAtValue);

        long elapsedMs =
                Duration.between(
                        firstEventBufferedAt,
                        Instant.now()
                ).toMillis();

        if (elapsedMs >= batchProperties.getMaxWaitMs()) {

            List<BatchEvent> createdBatches = loadCreatedBatches();

            createBatch(
                    "TIME_LIMIT_REACHED",
                    buffer,
                    createdBatches
            );
        }
    }

    private void createBatch(
            String reason,
            List<PaymentEvent> buffer,
            List<BatchEvent> createdBatches
    ) {

        List<PaymentEvent> batchRecords =
                new ArrayList<>(buffer);

        BatchEvent batch =
                new BatchEvent(
                        "BATCH-" + UUID.randomUUID(),
                        reason,
                        batchRecords.size(),
                        Instant.now(),
                        batchRecords
                );

        createdBatches.add(batch);

        daprStateStoreClient.saveState(
                CREATED_BATCHES_KEY,
                createdBatches
        );

        daprStateStoreClient.saveState(
                BUFFER_KEY,
                Collections.emptyList()
        );

        daprStateStoreClient.deleteState(
                FIRST_EVENT_BUFFERED_AT_KEY
        );

        log.info(
                "UC8_DURABLE_BATCH_CREATED batchId={} reason={} recordCount={} durableStore=statestore",
                batch.getBatchId(),
                batch.getReason(),
                batch.getRecordCount()
        );

        log.info(
                "UC8_DAPR_STATE_BATCH_HISTORY_SAVED createdBatchCount={}",
                createdBatches.size()
        );
    }

    public synchronized Map<String, Object> getBufferStatus() {

        List<PaymentEvent> buffer = loadBuffer();
        List<BatchEvent> createdBatches = loadCreatedBatches();

        String firstEventBufferedAt =
                daprStateStoreClient.getState(
                        FIRST_EVENT_BUFFERED_AT_KEY,
                        String.class,
                        null
                );

        return Map.of(
                "service", "payment-batch-state-service",
                "bufferSize", buffer.size(),
                "createdBatchCount", createdBatches.size(),
                "firstEventBufferedAt",
                firstEventBufferedAt == null ? "NA" : firstEventBufferedAt,
                "stateStore", "Dapr statestore",
                "limitation", "Durable buffer exists, but duplicate prevention comes in UC9"
        );
    }

    public synchronized List<BatchEvent> getCreatedBatches() {
        return loadCreatedBatches();
    }

    public synchronized void clearBuffer() {

        daprStateStoreClient.saveState(
                BUFFER_KEY,
                Collections.emptyList()
        );

        daprStateStoreClient.deleteState(
                FIRST_EVENT_BUFFERED_AT_KEY
        );

        log.info(
                "UC8_DAPR_STATE_BUFFER_CLEARED"
        );
    }

    public synchronized void clearBatches() {

        daprStateStoreClient.saveState(
                CREATED_BATCHES_KEY,
                Collections.emptyList()
        );

        log.info(
                "UC8_DAPR_STATE_BATCH_HISTORY_CLEARED"
        );
    }

    private List<PaymentEvent> loadBuffer() {

        List<PaymentEvent> buffer =
                daprStateStoreClient.getListState(
                        BUFFER_KEY,
                        PaymentEvent.class
                );

        log.info(
                "UC8_DAPR_STATE_BUFFER_LOADED bufferSize={}",
                buffer.size()
        );

        return new ArrayList<>(buffer);
    }

    private void saveBuffer(List<PaymentEvent> buffer) {

        daprStateStoreClient.saveState(
                BUFFER_KEY,
                buffer
        );

        log.info(
                "UC8_DAPR_STATE_BUFFER_SAVED bufferSize={}",
                buffer.size()
        );
    }

    private List<BatchEvent> loadCreatedBatches() {

        List<BatchEvent> createdBatches =
                daprStateStoreClient.getListState(
                        CREATED_BATCHES_KEY,
                        BatchEvent.class
                );

        return new ArrayList<>(createdBatches);
    }
}