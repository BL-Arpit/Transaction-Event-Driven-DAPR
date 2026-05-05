package com.bl.poc.aggregator.consumer;

import com.bl.poc.aggregator.model.PaymentEvent;
import com.bl.poc.aggregator.service.BatchFormationService;
import com.bl.poc.aggregator.service.InMemoryEventBufferService;
import com.bl.poc.aggregator.service.PaymentValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventKafkaConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentEventKafkaConsumer.class);

    private final ObjectMapper objectMapper;
    private final PaymentValidationService validationService;
    private final InMemoryEventBufferService bufferService;
    private final BatchFormationService batchFormationService;

    public PaymentEventKafkaConsumer(
            ObjectMapper objectMapper,
            PaymentValidationService validationService,
            InMemoryEventBufferService bufferService,
            BatchFormationService batchFormationService
    ) {
        this.objectMapper = objectMapper;
        this.validationService = validationService;
        this.bufferService = bufferService;
        this.batchFormationService = batchFormationService;
    }

    @KafkaListener(
            topics = "payment-events",
            groupId = "transaction-aggregator-uc4-group"
    )
    public void consume(ConsumerRecord<String, String> record) {

        try {
            log.info(
                    "UC4_KAFKA_EVENT_RECEIVED topic={} partition={} offset={} key={} value={}",
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.key(),
                    record.value()
            );

            PaymentEvent paymentEvent =
                    objectMapper.readValue(record.value(), PaymentEvent.class);

            validationService.validate(paymentEvent);

            int bufferSize = bufferService.addEvent(paymentEvent);

            log.info(
                    "UC4_KAFKA_EVENT_BUFFERED topic={} partition={} offset={} transactionId={} orderId={} merchantId={} amount={} currency={} paymentMode={} paymentStatus={} bufferSize={}",
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    paymentEvent.getTransactionId(),
                    paymentEvent.getOrderId(),
                    paymentEvent.getMerchantId(),
                    paymentEvent.getAmount(),
                    paymentEvent.getCurrency(),
                    paymentEvent.getPaymentMode(),
                    paymentEvent.getPaymentStatus(),
                    bufferSize
            );

            batchFormationService.evaluateBatchCreationBySize();

        } catch (IllegalArgumentException validationException) {

            log.warn(
                    "UC4_KAFKA_EVENT_VALIDATION_FAILED topic={} partition={} offset={} reason={} value={}",
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    validationException.getMessage(),
                    record.value()
            );

        } catch (Exception exception) {

            log.error(
                    "UC4_KAFKA_EVENT_PROCESSING_FAILED topic={} partition={} offset={} reason={} value={}",
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    exception.getMessage(),
                    record.value(),
                    exception
            );
        }
    }
}