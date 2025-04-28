package com.michael.container.server;

import com.michael.container.automation.auto.AutomationAutoConfiguration;
import com.michael.container.config.auto.ConfigAutoConfiguration;
import com.michael.container.distributed.election.auto.DistributedAutoConfiguration;
import com.michael.container.health.auto.HealthAutoConfiguration;
import com.michael.container.heartbeat.auto.HeartbeatAutoConfiguration;
import com.michael.container.notifications.auto.NotificationsAutoConfiguration;
import com.michael.container.registry.auto.RegistryAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBean(ServiceRegistryMarkerConfiguration.Marker.class)
public class ServiceRegistryAutoConfiguration {
  @Bean
  public RegistryAutoConfiguration registryAutoConfiguration() {
    return new RegistryAutoConfiguration();
  }

  @Bean
  public NotificationsAutoConfiguration notificationsAutoConfiguration() {
    return new NotificationsAutoConfiguration();
  }

  @Bean
  public HeartbeatAutoConfiguration heartbeatAutoConfiguration() {
    return new HeartbeatAutoConfiguration();
  }

  @Bean
  public HealthAutoConfiguration healthAutoConfiguration() {
    return new HealthAutoConfiguration();
  }

  @Bean
  public DistributedAutoConfiguration distributedAutoConfiguration() {
    return new DistributedAutoConfiguration();
  }

  @Bean
  public ConfigAutoConfiguration configAutoConfiguration() {
    return new ConfigAutoConfiguration();
  }

  @Bean
  @ConditionalOnProperty(name = "automation.controller.enabled", havingValue = "true")
  public AutomationAutoConfiguration automationAutoConfiguration() {
    return new AutomationAutoConfiguration();
  }
}
