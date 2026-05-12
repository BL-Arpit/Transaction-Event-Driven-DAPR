package com.bl.poc.aggregator.api;

import com.bl.poc.aggregator.model.PaymentEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dapr")
public class DaprPaymentEventSubscriberController {

    private static final Logger log =
            LoggerFactory.getLogger(DaprPaymentEventSubscriberController.class);

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${services.validation.url}")
    private String validationUrl;

    @Value("${services.batch-state.url}")
    private String batchStateUrl;

    public DaprPaymentEventSubscriberController(
            ObjectMapper objectMapper,
            RestTemplate restTemplate
    ) {
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    @PostMapping(
            value = "/payment-events",
            consumes = MediaType.ALL_VALUE
    )
    public ResponseEntity<Map<String, Object>> receivePaymentEventFromDapr(
            @RequestBody byte[] bodyBytes,
            @RequestHeader Map<String, String> headers
    ) {

        String body = new String(bodyBytes, StandardCharsets.UTF_8);

        try {
            log.info(
                    "UC7_DAPR_PUBSUB_EVENT_RECEIVED rawBody={} headers={}",
                    body,
                    headers
            );

            PaymentEvent paymentEvent = extractPaymentEvent(body);

            log.info(
                    "UC7_PAYMENT_EVENT_EXTRACTED transactionId={} orderId={} merchantId={} amount={} currency={} paymentMode={} paymentStatus={}",
                    paymentEvent.getTransactionId(),
                    paymentEvent.getOrderId(),
                    paymentEvent.getMerchantId(),
                    paymentEvent.getAmount(),
                    paymentEvent.getCurrency(),
                    paymentEvent.getPaymentMode(),
                    paymentEvent.getPaymentStatus()
            );

            restTemplate.postForEntity(
                    validationUrl,
                    paymentEvent,
                    String.class
            );

            log.info(
                    "UC7_PAYMENT_VALIDATED_THROUGH_DAPR transactionId={} validationUrl={}",
                    paymentEvent.getTransactionId(),
                    validationUrl
            );

            restTemplate.postForEntity(
                    batchStateUrl,
                    paymentEvent,
                    String.class
            );

            log.info(
                    "UC7_PAYMENT_SENT_TO_BATCH_STATE_THROUGH_DAPR transactionId={} batchStateUrl={}",
                    paymentEvent.getTransactionId(),
                    batchStateUrl
            );

            return ResponseEntity.ok(
                    Map.of(
                            "status", "SUCCESS",
                            "service", "payment-kafka-consumer-service",
                            "useCase", "UC7 - Dapr Kafka Pub/Sub Event Ingestion",
                            "transactionId", paymentEvent.getTransactionId()
                    )
            );

        } catch (Exception exception) {

            log.error(
                    "UC7_DAPR_PUBSUB_EVENT_PROCESSING_FAILED reason={} body={}",
                    exception.getMessage(),
                    body,
                    exception
            );

            return ResponseEntity
                    .status(500)
                    .body(
                            Map.of(
                                    "status", "RETRY",
                                    "service", "payment-kafka-consumer-service",
                                    "reason", exception.getMessage()
                            )
                    );
        }
    }

    private PaymentEvent extractPaymentEvent(String body) throws Exception {

        JsonNode root = objectMapper.readTree(body);

        /*
         * Case 1:
         * Dapr delivers CloudEvent-style wrapper:
         *
         * {
         *   "data": {
         *     "transactionId": "...",
         *     ...
         *   }
         * }
         */
        if (root.has("data")) {
            JsonNode dataNode = root.get("data");

            if (dataNode.isTextual()) {
                return objectMapper.readValue(
                        dataNode.asText(),
                        PaymentEvent.class
                );
            }

            return objectMapper.treeToValue(
                    dataNode,
                    PaymentEvent.class
            );
        }

        /*
         * Case 2:
         * Dapr raw payload delivery may contain base64 data:
         *
         * {
         *   "data_base64": "eyJ0cmFuc2FjdGlvbklkIjoi..."
         * }
         */
        if (root.has("data_base64")) {
            String encoded = root.get("data_base64").asText();

            String decodedJson = new String(
                    Base64.getDecoder().decode(encoded),
                    StandardCharsets.UTF_8
            );

            return objectMapper.readValue(
                    decodedJson,
                    PaymentEvent.class
            );
        }

        /*
         * Case 3:
         * Direct raw JSON body:
         *
         * {
         *   "transactionId": "...",
         *   "orderId": "...",
         *   ...
         * }
         */
        return objectMapper.treeToValue(
                root,
                PaymentEvent.class
        );
    }
}