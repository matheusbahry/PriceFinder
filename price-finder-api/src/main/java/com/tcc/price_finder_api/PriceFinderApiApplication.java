package com.tcc.price_finder_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PriceFinderApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(PriceFinderApiApplication.class, args);
	}

}
