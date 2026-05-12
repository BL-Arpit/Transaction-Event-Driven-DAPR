package com.bl.poc.aggregator.consumer;

import com.bl.poc.aggregator.model.PaymentEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PaymentEventKafkaConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentEventKafkaConsumer.class);

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${services.validation.url}")
    private String validationUrl;

    @Value("${services.batch-state.url}")
    private String batchStateUrl;

    public PaymentEventKafkaConsumer(
            ObjectMapper objectMapper,
            RestTemplate restTemplate
    ) {
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    @KafkaListener(
            topics = "payment-events",
            groupId = "payment-processing-uc6-group"
    )
    public void consume(ConsumerRecord<String, String> record) {

        try {
            log.info(
                    "UC5_KAFKA_EVENT_RECEIVED service=payment-kafka-consumer-service topic={} partition={} offset={} key={} value={}",
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.key(),
                    record.value()
            );

            PaymentEvent paymentEvent =
                    objectMapper.readValue(record.value(), PaymentEvent.class);

            restTemplate.postForEntity(
                    validationUrl,
                    paymentEvent,
                    String.class
            );

            log.info(
                    "UC6_PAYMENT_VALIDATED transactionId={} validationService={}",
                    paymentEvent.getTransactionId(),
                    validationUrl
            );

            restTemplate.postForEntity(
                    batchStateUrl,
                    paymentEvent,
                    String.class
            );

            log.info(
                    "UC6_PAYMENT_SENT_TO_BATCH_STATE transactionId={} batchStateService={} limitation=no_durable_batch_membership",
                    paymentEvent.getTransactionId(),
                    batchStateUrl
            );

        } catch (Exception exception) {

            log.error(
                    "UC6_KAFKA_EVENT_PROCESSING_FAILED topic={} partition={} offset={} key={} reason={}",
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.key(),
                    exception.getMessage(),
                    exception
            );
        }
    }
}