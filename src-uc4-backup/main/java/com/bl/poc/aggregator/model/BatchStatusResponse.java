package com.bl.poc.aggregator.model;

import java.time.Instant;
import java.util.List;

public class BatchStatusResponse {

    private String status;
    private int totalBatches;
    private List<BatchEvent> batches;
    private Instant checkedAt;

    public BatchStatusResponse() {
    }

    public BatchStatusResponse(
            String status,
            int totalBatches,
            List<BatchEvent> batches,
            Instant checkedAt
    ) {
        this.status = status;
        this.totalBatches = totalBatches;
        this.batches = batches;
        this.checkedAt = checkedAt;
    }

    public static BatchStatusResponse currentStatus(List<BatchEvent> batches) {
        return new BatchStatusResponse(
                "SUCCESS",
                batches.size(),
                batches,
                Instant.now()
        );
    }

    public String getStatus() {
        return status;
    }

    public int getTotalBatches() {
        return totalBatches;
    }

    public List<BatchEvent> getBatches() {
        return batches;
    }

    public Instant getCheckedAt() {
        return checkedAt;
    }
}