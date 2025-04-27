package com.michael.container.automation.controller;

import com.michael.container.automation.enums.Result;
import com.michael.container.health.routines.HealthCheckRoutine;
import com.michael.container.notifications.service.RegisterNotificationService;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/automation-testing")
@ActiveProfiles("automation")
public class AutomationEnvironmentController {
  private final RegisterNotificationService registerNotificationService;
  private final HealthCheckRoutine healthCheckRoutine;

  public AutomationEnvironmentController(
      RegisterNotificationService registerNotificationService,
      HealthCheckRoutine healthCheckRoutine) {
    this.registerNotificationService =
        (RegisterNotificationService) translateToRealObject(registerNotificationService);
    this.healthCheckRoutine = (HealthCheckRoutine) translateToRealObject(healthCheckRoutine);
  }

  @PostMapping("/jobs/pending-notifications")
  public Result runNotification() {
    registerNotificationService.processPendingNotifications();
    return Result.RAN;
  }

  @PostMapping("/jobs/health-checks")
  public Result runHealthCheck() {
    healthCheckRoutine.populateHealthCheckQueue();
    return Result.RAN;
  }

  private Object translateToRealObject(Object object) {
    if (AopUtils.isAopProxy(object) && object instanceof Advised) {
      try {
        Advised advised = (Advised) object;
        return advised.getTargetSource().getTarget();
      } catch (Exception e) {
        throw new RuntimeException("Failed to get target object from proxy", e);
      }
    }
    return object;
  }
}
