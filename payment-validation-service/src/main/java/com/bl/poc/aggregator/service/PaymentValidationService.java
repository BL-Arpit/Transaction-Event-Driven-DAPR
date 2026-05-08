package com.bl.poc.aggregator.service;

import com.bl.poc.aggregator.model.PaymentEvent;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;

@Service
public class PaymentValidationService {

    private static final Set<String> ALLOWED_CURRENCIES =
            Set.of("INR", "USD", "EUR");

    private static final Set<String> ALLOWED_PAYMENT_STATUS =
            Set.of("SUCCESS", "FAILED", "PENDING");

    public void validate(PaymentEvent event) {

        if (event.getTransactionId() == null || event.getTransactionId().isBlank()) {
            throw new IllegalArgumentException("transactionId is required");
        }

        if (event.getOrderId() == null || event.getOrderId().isBlank()) {
            throw new IllegalArgumentException("orderId is required");
        }

        if (event.getMerchantId() == null || event.getMerchantId().isBlank()) {
            throw new IllegalArgumentException("merchantId is required");
        }

        if (event.getAmount() == null || event.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }

        if (event.getCurrency() == null || !ALLOWED_CURRENCIES.contains(event.getCurrency())) {
            throw new IllegalArgumentException("currency must be one of INR, USD, EUR");
        }

        if (event.getPaymentMode() == null || event.getPaymentMode().isBlank()) {
            throw new IllegalArgumentException("paymentMode is required");
        }

        if (event.getPaymentStatus() == null || !ALLOWED_PAYMENT_STATUS.contains(event.getPaymentStatus())) {
            throw new IllegalArgumentException("paymentStatus must be SUCCESS, FAILED, or PENDING");
        }

        if (event.getEventTimestamp() == null) {
            throw new IllegalArgumentException("eventTimestamp is required");
        }
    }
}