package com.bl.poc.aggregator.api;

import com.bl.poc.aggregator.model.BatchEvent;
import com.bl.poc.aggregator.model.PaymentEvent;
import com.bl.poc.aggregator.service.InMemoryBatchStateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/batch-state")
public class BatchStateController {

    private final InMemoryBatchStateService batchStateService;

    public BatchStateController(InMemoryBatchStateService batchStateService) {
        this.batchStateService = batchStateService;
    }

    @PostMapping("/events")
    public ResponseEntity<Map<String, Object>> addEvent(
            @RequestBody PaymentEvent paymentEvent
    ) {
        return ResponseEntity.ok(batchStateService.addEvent(paymentEvent));
    }

    @GetMapping("/buffer/status")
    public ResponseEntity<Map<String, Object>> getBufferStatus() {
        return ResponseEntity.ok(batchStateService.getBufferStatus());
    }

    @DeleteMapping("/buffer")
    public ResponseEntity<Map<String, Object>> clearBuffer() {
        batchStateService.clearBuffer();
        return ResponseEntity.ok(
                Map.of(
                        "service", "payment-batch-state-service",
                        "status", "BUFFER_CLEARED"
                )
        );
    }

    @GetMapping("/batches")
    public ResponseEntity<List<BatchEvent>> getBatches() {
        return ResponseEntity.ok(batchStateService.getCreatedBatches());
    }

    @DeleteMapping("/batches")
    public ResponseEntity<Map<String, Object>> clearBatches() {
        batchStateService.clearBatches();
        return ResponseEntity.ok(
                Map.of(
                        "service", "payment-batch-state-service",
                        "status", "BATCHES_CLEARED"
                )
        );
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(
                Map.of(
                        "service", "payment-batch-state-service",
                        "status", "UP",
                        "useCase", "UC5 - In-Memory Batch State Microservice",
                        "importantLimitation", "Restarting this service will erase buffer and batch history"
                )
        );
    }
}