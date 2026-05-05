package com.bl.poc.aggregator.api;

import com.bl.poc.aggregator.model.BufferStatusResponse;
import com.bl.poc.aggregator.model.ErrorResponse;
import com.bl.poc.aggregator.model.PaymentEvent;
import com.bl.poc.aggregator.model.PaymentEventResponse;
import com.bl.poc.aggregator.service.InMemoryEventBufferService;
import com.bl.poc.aggregator.service.PaymentValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/events")
public class PaymentEventController {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentEventController.class);

    private final PaymentValidationService validationService;
    private final InMemoryEventBufferService bufferService;

    public PaymentEventController(
            PaymentValidationService validationService,
            InMemoryEventBufferService bufferService
    ) {
        this.validationService = validationService;
        this.bufferService = bufferService;
    }

    @PostMapping("/payment")
    public ResponseEntity<?> receivePaymentEvent(@RequestBody PaymentEvent paymentEvent) {

        try {
            validationService.validate(paymentEvent);

            int bufferSize = bufferService.addEvent(paymentEvent);

            log.info(
                    "UC2_PAYMENT_EVENT_BUFFERED transactionId={} orderId={} merchantId={} amount={} currency={} paymentMode={} paymentStatus={} eventTimestamp={} bufferSize={}",
                    paymentEvent.getTransactionId(),
                    paymentEvent.getOrderId(),
                    paymentEvent.getMerchantId(),
                    paymentEvent.getAmount(),
                    paymentEvent.getCurrency(),
                    paymentEvent.getPaymentMode(),
                    paymentEvent.getPaymentStatus(),
                    paymentEvent.getEventTimestamp(),
                    bufferSize
            );

            return ResponseEntity.ok(
                    PaymentEventResponse.accepted(
                            paymentEvent.getTransactionId(),
                            bufferSize
                    )
            );

        } catch (IllegalArgumentException validationException) {

            log.warn(
                    "UC2_PAYMENT_EVENT_REJECTED reason={} payloadTransactionId={}",
                    validationException.getMessage(),
                    paymentEvent != null ? paymentEvent.getTransactionId() : null
            );

            return ResponseEntity.badRequest().body(
                    ErrorResponse.validationFailed(validationException.getMessage())
            );
        }
    }

    @GetMapping("/buffer/status")
    public ResponseEntity<BufferStatusResponse> getBufferStatus() {

        int bufferSize = bufferService.getBufferSize();

        log.info("UC2_BUFFER_STATUS_REQUESTED bufferSize={}", bufferSize);

        return ResponseEntity.ok(
                BufferStatusResponse.currentStatus(bufferSize)
        );
    }

    @DeleteMapping("/buffer")
    public ResponseEntity<BufferStatusResponse> clearBuffer() {

        bufferService.clearBuffer();

        log.info("UC2_BUFFER_CLEARED");

        return ResponseEntity.ok(
                BufferStatusResponse.cleared()
        );
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(
                Map.of(
                        "status", "UP",
                        "service", "transaction-aggregator-service",
                        "useCase", "UC2 - Temporary Event Buffering",
                        "bufferSize", bufferService.getBufferSize()
                )
        );
    }
}