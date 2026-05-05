package com.bl.poc.aggregator.service;

import com.bl.poc.aggregator.model.PaymentEvent;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;

@Service
public class PaymentValidationService {

    private static final Set<String> ALLOWED_CURRENCIES = Set.of("INR", "USD", "EUR");

    private static final Set<String> ALLOWED_PAYMENT_STATUSES =
            Set.of("SUCCESS", "FAILED", "PENDING");

    public void validate(PaymentEvent event) {

        if (event == null) {
            throw new IllegalArgumentException("event body is required");
        }

        if (isBlank(event.getTransactionId())) {
            throw new IllegalArgumentException("transactionId is required");
        }

        if (isBlank(event.getOrderId())) {
            throw new IllegalArgumentException("orderId is required");
        }

        if (isBlank(event.getMerchantId())) {
            throw new IllegalArgumentException("merchantId is required");
        }

        if (event.getAmount() == null) {
            throw new IllegalArgumentException("amount is required");
        }

        if (event.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }

        if (isBlank(event.getCurrency())) {
            throw new IllegalArgumentException("currency is required");
        }

        if (!ALLOWED_CURRENCIES.contains(event.getCurrency())) {
            throw new IllegalArgumentException("currency must be one of: INR, USD, EUR");
        }

        if (isBlank(event.getPaymentMode())) {
            throw new IllegalArgumentException("paymentMode is required");
        }

        if (isBlank(event.getPaymentStatus())) {
            throw new IllegalArgumentException("paymentStatus is required");
        }

        if (!ALLOWED_PAYMENT_STATUSES.contains(event.getPaymentStatus())) {
            throw new IllegalArgumentException("paymentStatus must be one of: SUCCESS, FAILED, PENDING");
        }

        if (event.getEventTimestamp() == null) {
            throw new IllegalArgumentException("eventTimestamp is required");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}