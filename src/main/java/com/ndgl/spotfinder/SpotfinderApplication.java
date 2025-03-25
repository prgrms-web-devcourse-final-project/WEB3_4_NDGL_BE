package com.ndgl.spotfinder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SpotfinderApplication {
	public static void main(String[] args) {
		SpringApplication.run(SpotfinderApplication.class, args);
	}
}
