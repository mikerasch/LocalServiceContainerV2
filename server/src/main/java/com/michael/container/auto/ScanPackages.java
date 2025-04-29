package com.michael.container.auto;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@ComponentScan("com.michael.container")
@Configuration
@ConditionalOnProperty(value = "service.registry.enabled", havingValue = "true")
@EnableRedisRepositories(basePackages = "com.michael.container")
public class ScanPackages {}
