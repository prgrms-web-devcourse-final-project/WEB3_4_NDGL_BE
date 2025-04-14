package com.ndgl.spotfinder.global.async;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

	@Bean(name = "imageCleanupExecutor")
	public Executor imageCleanupExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);					// 기본 스레드 수
		executor.setMaxPoolSize(5);						// 확장 가능 최대 스레드 갯수
		executor.setQueueCapacity(10);					// 대기 최대 보관 갯수
		executor.setThreadNamePrefix("ImageClean-");	// 접두어
		executor.initialize();							// 빈 생성 시 스레드 풀 초기화
		return executor;
	}
}
