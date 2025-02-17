package com.michael.container.notifications.event;

import com.michael.container.notifications.enums.NotificationType;
import com.michael.container.notifications.model.ServiceNotificationRequest;
import com.michael.container.notifications.service.NotificationService;
import com.michael.container.registry.model.RegisterEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class RegisterEventListener {
  private final NotificationService notificationService;

  public RegisterEventListener(
      @Qualifier("registerNotificationService") NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @EventListener(RegisterEvent.class)
  public void registerEvent(RegisterEvent event) {
    notificationService.notify(
        new ServiceNotificationRequest(
            NotificationType.SERVICE_REGISTERED,
            event.applicationName(),
            event.url(),
            event.version(),
            event.port()));
  }
}
