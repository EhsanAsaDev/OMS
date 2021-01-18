package com.oms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
public class OmsEventsConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(OmsEventsConsumerApplication.class, args);
	}

}
