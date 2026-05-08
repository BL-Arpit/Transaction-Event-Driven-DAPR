package com.bl.poc.aggregator.scheduler;

import com.bl.poc.aggregator.service.InMemoryBatchStateService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BatchScheduler {

    private final InMemoryBatchStateService batchStateService;

    public BatchScheduler(InMemoryBatchStateService batchStateService) {
        this.batchStateService = batchStateService;
    }

    @Scheduled(fixedDelay = 5000)
    public void checkPartialBatchTimeout() {
        batchStateService.createPartialBatchIfExpired();
    }
}