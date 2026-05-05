package com.bl.poc.aggregator.model;

import java.time.Instant;

public class ErrorResponse {

    private String status;
    private String errorCode;
    private String message;
    private Instant failedAt;

    public ErrorResponse() {
    }

    public ErrorResponse(String status, String errorCode, String message, Instant failedAt) {
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
        this.failedAt = failedAt;
    }

    public static ErrorResponse validationFailed(String message) {
        return new ErrorResponse(
                "REJECTED",
                "VALIDATION_FAILED",
                message,
                Instant.now()
        );
    }

    public String getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public Instant getFailedAt() {
        return failedAt;
    }
}