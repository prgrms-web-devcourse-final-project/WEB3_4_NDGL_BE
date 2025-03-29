package com.ndgl.spotfinder.global.security.redis.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableRedisRepositories(basePackages = "com.ndgl.spotfinder.global.security.redis.repository")
public class RedisConfig {
}
