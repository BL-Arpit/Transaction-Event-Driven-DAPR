package com.bl.poc.aggregator.model;

import java.time.Instant;

public class PaymentEventResponse {

    private String status;
    private String transactionId;
    private String message;
    private int bufferSize;
    private Instant processedAt;

    public PaymentEventResponse() {
    }

    public PaymentEventResponse(
            String status,
            String transactionId,
            String message,
            int bufferSize,
            Instant processedAt
    ) {
        this.status = status;
        this.transactionId = transactionId;
        this.message = message;
        this.bufferSize = bufferSize;
        this.processedAt = processedAt;
    }

    public static PaymentEventResponse accepted(String transactionId, int bufferSize) {
        return new PaymentEventResponse(
                "ACCEPTED",
                transactionId,
                "Payment event accepted and added to temporary buffer",
                bufferSize,
                Instant.now()
        );
    }

    public String getStatus() {
        return status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getMessage() {
        return message;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}