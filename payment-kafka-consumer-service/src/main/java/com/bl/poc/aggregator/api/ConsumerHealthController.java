package com.bl.poc.aggregator.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/consumer")
public class ConsumerHealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(
                Map.of(
                        "service", "payment-kafka-consumer-service",
                        "status", "UP",
                        "useCase", "UC5 - Kafka Consumer Microservice"
                )
        );
    }
}