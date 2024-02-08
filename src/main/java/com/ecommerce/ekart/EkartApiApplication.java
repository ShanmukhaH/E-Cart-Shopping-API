package com.ecommerce.ekart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class EkartApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(EkartApiApplication.class, args);
		System.out.println("Application Started");
	}

}
