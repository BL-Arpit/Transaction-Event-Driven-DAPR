package com.bl.poc.aggregator.model;

import java.time.Instant;

public class BufferStatusResponse {

    private String status;
    private int bufferSize;
    private String message;
    private Instant checkedAt;

    public BufferStatusResponse() {
    }

    public BufferStatusResponse(String status, int bufferSize, String message, Instant checkedAt) {
        this.status = status;
        this.bufferSize = bufferSize;
        this.message = message;
        this.checkedAt = checkedAt;
    }

    public static BufferStatusResponse currentStatus(int bufferSize) {
        return new BufferStatusResponse(
                "SUCCESS",
                bufferSize,
                "Current temporary buffer size fetched successfully",
                Instant.now()
        );
    }

    public static BufferStatusResponse cleared() {
        return new BufferStatusResponse(
                "SUCCESS",
                0,
                "Temporary buffer cleared successfully",
                Instant.now()
        );
    }

    public String getStatus() {
        return status;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCheckedAt() {
        return checkedAt;
    }
}