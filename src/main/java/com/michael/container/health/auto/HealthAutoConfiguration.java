package com.michael.container.health.auto;

import com.michael.container.health.client.HealthCheckClient;
import com.michael.container.health.listener.HealthQueueListener;
import com.michael.container.health.repositories.HealthQueueRepository;
import com.michael.container.health.routines.HealthCheckRoutine;
import com.michael.container.health.service.HealthCheckService;
import com.michael.container.registry.cache.listener.key.KeyListener;
import com.michael.container.registry.cache.repositories.ApplicationRepository;
import java.util.concurrent.ExecutorService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.web.client.RestClient;

public class HealthAutoConfiguration {
  @Configuration
  public static class ClientConfig {
    @ConditionalOnMissingBean
    @Bean
    public HealthCheckClient healthCheckClient(RestClient.Builder builder) {
      return new HealthCheckClient(builder);
    }
  }

  @Configuration
  public static class KeyListenerConfig {
    @ConditionalOnMissingBean
    @Bean
    public KeyListener healthQueueListener(HealthCheckService healthCheckService) {
      return new HealthQueueListener(healthCheckService);
    }
  }

  @Configuration
  @EnableRedisRepositories("com.michael.container.health.repositories")
  public static class RepositoryConfig {}

  @Configuration
  public static class RoutineConfig {
    @ConditionalOnMissingBean
    @Bean
    public HealthCheckRoutine healthCheckRoutine(
        HealthQueueRepository healthQueueRepository, ApplicationRepository applicationRepository) {
      return new HealthCheckRoutine(healthQueueRepository, applicationRepository);
    }
  }

  @Configuration
  public static class ServiceConfig {
    @ConditionalOnMissingClass
    @Bean
    public HealthCheckService healthCheckService(
        HealthCheckClient healthCheckClient,
        HealthQueueRepository healthQueueRepository,
        @Qualifier("healthCheckExecutorService") ExecutorService healthCheckExecutorService,
        ApplicationEventPublisher eventPublisher) {
      return new HealthCheckService(
          healthCheckClient, healthQueueRepository, healthCheckExecutorService, eventPublisher);
    }
  }
}
