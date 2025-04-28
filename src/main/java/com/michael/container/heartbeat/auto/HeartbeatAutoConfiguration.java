package com.michael.container.heartbeat.auto;

import com.michael.container.heartbeat.controller.HeartbeatController;
import com.michael.container.heartbeat.service.HeartbeatService;
import com.michael.container.registry.cache.crud.CrudRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class HeartbeatAutoConfiguration {
  @Configuration
  public static class ControllerConfig {
    @ConditionalOnMissingBean
    @Bean
    public HeartbeatController heartbeatController(HeartbeatService heartbeatService) {
      return new HeartbeatController(heartbeatService);
    }
  }

  @Configuration
  public static class ServiceConfig {
    @ConditionalOnMissingBean
    @Bean
    public HeartbeatService heartbeatService(CrudRegistry crudRegistry) {
      return new HeartbeatService(crudRegistry);
    }
  }
}
