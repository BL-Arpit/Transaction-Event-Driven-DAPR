package com.bl.poc.aggregator.service;

import com.bl.poc.aggregator.config.BatchProperties;
import com.bl.poc.aggregator.model.BatchEvent;
import com.bl.poc.aggregator.model.PaymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BatchFormationService {

    private static final Logger log =
            LoggerFactory.getLogger(BatchFormationService.class);

    private final InMemoryEventBufferService bufferService;
    private final BatchProperties batchProperties;

    private final List<BatchEvent> createdBatches = new ArrayList<>();

    public BatchFormationService(
            InMemoryEventBufferService bufferService,
            BatchProperties batchProperties
    ) {
        this.bufferService = bufferService;
        this.batchProperties = batchProperties;
    }

    public synchronized void evaluateBatchCreationBySize() {

        int currentBufferSize = bufferService.getBufferSize();

        if (currentBufferSize >= batchProperties.getMaxBatchSize()) {
            createBatch("SIZE_LIMIT_REACHED");
        }
    }

    @Scheduled(fixedDelay = 5000)
    public synchronized void evaluateBatchCreationByTime() {

        if (bufferService.isEmpty()) {
            return;
        }

        Instant firstEventBufferedAt = bufferService.getFirstEventBufferedAt();

        if (firstEventBufferedAt == null) {
            return;
        }

        long elapsedMs =
                Duration.between(firstEventBufferedAt, Instant.now()).toMillis();

        if (elapsedMs >= batchProperties.getMaxWaitMs()) {
            createBatch("TIME_LIMIT_REACHED");
        }
    }

    public synchronized List<BatchEvent> getCreatedBatches() {
        return new ArrayList<>(createdBatches);
    }

    public synchronized void clearCreatedBatches() {
        createdBatches.clear();
    }

    private void createBatch(String reason) {

        List<PaymentEvent> records = bufferService.drainBuffer();

        if (records.isEmpty()) {
            return;
        }

        Instant firstEventTimestamp = records.get(0).getEventTimestamp();
        Instant lastEventTimestamp = records.get(records.size() - 1).getEventTimestamp();

        BatchEvent batchEvent = new BatchEvent(
                generateBatchId(),
                records.size(),
                firstEventTimestamp,
                lastEventTimestamp,
                Instant.now(),
                records
        );

        createdBatches.add(batchEvent);

        log.info(
                "UC3_BATCH_CREATED batchId={} reason={} recordCount={} firstEventTimestamp={} lastEventTimestamp={} totalCreatedBatches={}",
                batchEvent.getBatchId(),
                reason,
                batchEvent.getRecordCount(),
                batchEvent.getFirstEventTimestamp(),
                batchEvent.getLastEventTimestamp(),
                createdBatches.size()
        );
    }

    private String generateBatchId() {
        return "BATCH-" + UUID.randomUUID();
    }
}