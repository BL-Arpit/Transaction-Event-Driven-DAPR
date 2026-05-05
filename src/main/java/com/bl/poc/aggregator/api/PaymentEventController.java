package com.bl.poc.aggregator.api;

import com.bl.poc.aggregator.model.BatchStatusResponse;
import com.bl.poc.aggregator.model.BufferStatusResponse;
import com.bl.poc.aggregator.model.ErrorResponse;
import com.bl.poc.aggregator.model.PaymentEvent;
import com.bl.poc.aggregator.model.PaymentEventResponse;
import com.bl.poc.aggregator.service.BatchFormationService;
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
    private final BatchFormationService batchFormationService;

    public PaymentEventController(
            PaymentValidationService validationService,
            InMemoryEventBufferService bufferService,
            BatchFormationService batchFormationService
    ) {
        this.validationService = validationService;
        this.bufferService = bufferService;
        this.batchFormationService = batchFormationService;
    }

    @PostMapping("/payment")
    public ResponseEntity<?> receivePaymentEvent(@RequestBody PaymentEvent paymentEvent) {

        try {
            validationService.validate(paymentEvent);

            int bufferSize = bufferService.addEvent(paymentEvent);

            log.info(
                    "UC3_PAYMENT_EVENT_BUFFERED transactionId={} orderId={} merchantId={} amount={} currency={} paymentMode={} paymentStatus={} eventTimestamp={} bufferSize={}",
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

            batchFormationService.evaluateBatchCreationBySize();

            return ResponseEntity.ok(
                    PaymentEventResponse.accepted(
                            paymentEvent.getTransactionId(),
                            bufferService.getBufferSize()
                    )
            );

        } catch (IllegalArgumentException validationException) {

            log.warn(
                    "UC3_PAYMENT_EVENT_REJECTED reason={} payloadTransactionId={}",
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

        log.info("UC3_BUFFER_STATUS_REQUESTED bufferSize={}", bufferSize);

        return ResponseEntity.ok(
                BufferStatusResponse.currentStatus(bufferSize)
        );
    }

    @DeleteMapping("/buffer")
    public ResponseEntity<BufferStatusResponse> clearBuffer() {

        bufferService.clearBuffer();

        log.info("UC3_BUFFER_CLEARED");

        return ResponseEntity.ok(
                BufferStatusResponse.cleared()
        );
    }

    @GetMapping("/batches")
    public ResponseEntity<BatchStatusResponse> getBatches() {

        return ResponseEntity.ok(
                BatchStatusResponse.currentStatus(
                        batchFormationService.getCreatedBatches()
                )
        );
    }

    @DeleteMapping("/batches")
    public ResponseEntity<Map<String, Object>> clearBatches() {

        batchFormationService.clearCreatedBatches();

        log.info("UC3_BATCH_HISTORY_CLEARED");

        return ResponseEntity.ok(
                Map.of(
                        "status", "SUCCESS",
                        "message", "Created batch history cleared successfully"
                )
        );
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(
                Map.of(
                        "status", "UP",
                        "service", "transaction-aggregator-service",
                        "useCase", "UC3 - Basic Batch Formation",
                        "bufferSize", bufferService.getBufferSize(),
                        "createdBatchCount", batchFormationService.getCreatedBatches().size()
                )
        );
    }
}