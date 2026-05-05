package com.bl.poc.aggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication
public class TransactionAggregatorServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransactionAggregatorServiceApplication.class, args);
	}

}
