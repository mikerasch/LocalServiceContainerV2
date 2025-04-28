package com.michael.container.automation.auto;

import com.michael.container.automation.controller.AutomationEnvironmentController;
import com.michael.container.health.routines.HealthCheckRoutine;
import com.michael.container.notifications.service.RegisterNotificationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

public class AutomationAutoConfiguration {
  @Component
  public static class ControllerConfig {

    @Bean
    @ConditionalOnMissingBean
    public AutomationEnvironmentController automationEnvironmentController(
        RegisterNotificationService registerNotificationService,
        HealthCheckRoutine healthCheckRoutine)
        throws Exception {
      return new AutomationEnvironmentController(registerNotificationService, healthCheckRoutine);
    }
  }
}
