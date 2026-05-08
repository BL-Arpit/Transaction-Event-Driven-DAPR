package com.bl.poc.aggregator.api;

import com.bl.poc.aggregator.model.PaymentEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/kafka-test")
public class KafkaTestProducerController {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaTestProducerController(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/payment")
    public ResponseEntity<Map<String, Object>> publishPaymentEvent(
            @RequestBody PaymentEvent paymentEvent
    ) throws Exception {

        String json = objectMapper.writeValueAsString(paymentEvent);

        kafkaTemplate.send(
                "payment-events",
                paymentEvent.getTransactionId(),
                json
        );

        return ResponseEntity.ok(
                Map.of(
                        "service", "payment-producer-service",
                        "status", "PUBLISHED",
                        "topic", "payment-events",
                        "key", paymentEvent.getTransactionId(),
                        "message", "Payment event published to Kafka"
                )
        );
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(
                Map.of(
                        "service", "payment-producer-service",
                        "status", "UP",
                        "useCase", "UC5 - Producer Microservice"
                )
        );
    }
}