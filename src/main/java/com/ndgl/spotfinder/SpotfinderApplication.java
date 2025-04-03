package com.ndgl.spotfinder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class SpotfinderApplication {
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(SpotfinderApplication.class);
		//app.setAdditionalProfiles("test"); // ✅ 여기에 프로필 명시
		app.run(args);
	}
}
