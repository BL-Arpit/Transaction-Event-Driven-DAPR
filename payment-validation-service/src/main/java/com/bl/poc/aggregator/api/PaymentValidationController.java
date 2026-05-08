package com.bl.poc.aggregator.api;

import com.bl.poc.aggregator.model.PaymentEvent;
import com.bl.poc.aggregator.service.PaymentValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/validation")
public class PaymentValidationController {

    private final PaymentValidationService validationService;

    public PaymentValidationController(PaymentValidationService validationService) {
        this.validationService = validationService;
    }

    @PostMapping("/payment")
    public ResponseEntity<Map<String, Object>> validatePayment(
            @RequestBody PaymentEvent paymentEvent
    ) {
        validationService.validate(paymentEvent);

        return ResponseEntity.ok(
                Map.of(
                        "service", "payment-validation-service",
                        "status", "VALID",
                        "transactionId", paymentEvent.getTransactionId()
                )
        );
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(
                Map.of(
                        "service", "payment-validation-service",
                        "status", "UP",
                        "useCase", "UC5 - Validation Microservice"
                )
        );
    }
}