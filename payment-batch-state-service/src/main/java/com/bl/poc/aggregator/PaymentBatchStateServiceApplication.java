package com.bl.poc.aggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PaymentBatchStateServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentBatchStateServiceApplication.class, args);
    }
}