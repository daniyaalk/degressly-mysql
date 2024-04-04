package com.degressly.proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.atomic.AtomicLong;

@SpringBootApplication
public class MysqlApplication {

	public static void main(String[] args) {
		SpringApplication.run(MysqlApplication.class, args);
	}

	@Bean
	public AtomicLong taskCounter() {
		return new AtomicLong();
	}

}
