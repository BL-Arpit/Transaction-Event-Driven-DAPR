package com.bl.poc.aggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PaymentKafkaConsumerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentKafkaConsumerServiceApplication.class, args);
    }
}