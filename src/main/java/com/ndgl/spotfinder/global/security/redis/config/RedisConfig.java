package com.ndgl.spotfinder.global.security.redis.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableRedisRepositories(basePackages = "com.ndgl.spotfinder.global.security.redis.repository")
public class RedisConfig {
	@Bean
	public RedisTemplate<String, List<Long>> postCachdRedisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, List<Long>> template = new RedisTemplate<>();

		template.setConnectionFactory(connectionFactory);
		template.setKeySerializer(new StringRedisSerializer());

		template.setValueSerializer(new Jackson2JsonRedisSerializer<>(List.class));
		return template;
	}
}
