package com.example.payment_gateway_poc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PaymentGatewayPocApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentGatewayPocApplication.class, args);
		System.out.println("Application has started successfully!");
	}

}
