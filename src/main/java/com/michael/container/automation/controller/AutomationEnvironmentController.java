package com.michael.container.automation.controller;

import com.michael.container.automation.enums.Result;
import com.michael.container.health.routines.HealthCheckRoutine;
import com.michael.container.notifications.service.RegisterNotificationService;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Class meant to provide an API into managing routines.
 * Specifically designed to work in automation testing, ensuring less wait time for tests which depend on
 * routines running.
 */
@RestController
@RequestMapping("/automation-testing")
@ActiveProfiles("automation")
public class AutomationEnvironmentController {
  private final RegisterNotificationService registerNotificationService;
  private final HealthCheckRoutine healthCheckRoutine;

  public AutomationEnvironmentController(
      RegisterNotificationService registerNotificationService,
      HealthCheckRoutine healthCheckRoutine) throws Exception {
    // We want to bypass the spring proxy to avoid the AOP restrictions placed on these classes.
    this.registerNotificationService =
        (RegisterNotificationService) translateToRealObject(registerNotificationService);
    this.healthCheckRoutine = (HealthCheckRoutine) translateToRealObject(healthCheckRoutine);
  }

  @PostMapping("/jobs/pending-notifications")
  @Hidden
  public Result runNotification() {
    registerNotificationService.processPendingNotifications();
    return Result.RAN;
  }

  @PostMapping("/jobs/health-checks")
  @Hidden
  public Result runHealthCheck() {
    healthCheckRoutine.populateHealthCheckQueue();
    return Result.RAN;
  }

  private Object translateToRealObject(Object object) throws Exception {
    if (AopUtils.isAopProxy(object) && object instanceof Advised advised) {
        return advised.getTargetSource().getTarget();
    }
    return object;
  }
}
