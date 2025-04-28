package com.michael.container.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class AppConfig {

  @Bean("healthCheckExecutorService")
  public ExecutorService healthCheckExecutorService() {
    return Executors.newFixedThreadPool(10);
  }

  @Bean
  @ConditionalOnMissingBean
  public ConversionService conversionService() {
    return new DefaultConversionService();
  }
}
