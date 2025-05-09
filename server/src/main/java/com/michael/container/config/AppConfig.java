package com.michael.container.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@EnableAspectJAutoProxy
public class AppConfig {

  @Bean("healthCheckExecutorService")
  public ExecutorService healthCheckExecutorService() {
    return Executors.newVirtualThreadPerTaskExecutor();
  }
}
