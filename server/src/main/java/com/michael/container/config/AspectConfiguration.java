package com.michael.container.config;

import com.michael.spring.utils.logger.handlers.ExecutionTimeAspectHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "service.registry.logging.aspectJ.enabled", havingValue = "true")
public class AspectConfiguration {

  @Bean
  @ConditionalOnProperty(
      value = "service.registry.logging.aspectJ.timing.enabled",
      havingValue = "true")
  public ExecutionTimeAspectHandler executionTimeAspectHandler() {
    return new ExecutionTimeAspectHandler();
  }
}
