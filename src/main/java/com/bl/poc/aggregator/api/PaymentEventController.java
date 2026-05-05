package com.bl.poc.aggregator.api;

import com.bl.poc.aggregator.model.ErrorResponse;
import com.bl.poc.aggregator.model.PaymentEvent;
import com.bl.poc.aggregator.model.PaymentEventResponse;
import com.bl.poc.aggregator.service.PaymentValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/events")
public class PaymentEventController {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventController.class);

    private final PaymentValidationService validationService;

    public PaymentEventController(PaymentValidationService validationService) {
        this.validationService = validationService;
    }

    @PostMapping("/payment")
    public ResponseEntity<?> receivePaymentEvent(@RequestBody PaymentEvent paymentEvent) {

        try {
            validationService.validate(paymentEvent);

            log.info(
                    "UC1_PAYMENT_EVENT_ACCEPTED transactionId={} orderId={} merchantId={} amount={} currency={} paymentMode={} paymentStatus={} eventTimestamp={}",
                    paymentEvent.getTransactionId(),
                    paymentEvent.getOrderId(),
                    paymentEvent.getMerchantId(),
                    paymentEvent.getAmount(),
                    paymentEvent.getCurrency(),
                    paymentEvent.getPaymentMode(),
                    paymentEvent.getPaymentStatus(),
                    paymentEvent.getEventTimestamp()
            );

            return ResponseEntity.ok(
                    PaymentEventResponse.accepted(paymentEvent.getTransactionId())
            );

        } catch (IllegalArgumentException validationException) {

            log.warn(
                    "UC1_PAYMENT_EVENT_REJECTED reason={} payloadTransactionId={}",
                    validationException.getMessage(),
                    paymentEvent != null ? paymentEvent.getTransactionId() : null
            );

            return ResponseEntity.badRequest().body(
                    ErrorResponse.validationFailed(validationException.getMessage())
            );
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(
                Map.of(
                        "status", "UP",
                        "service", "transaction-aggregator-service",
                        "useCase", "UC1 - Basic Payment Event Ingestion"
                )
        );
    }
}