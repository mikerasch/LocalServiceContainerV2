package com.michael.container.notifications.event;

import com.michael.container.notifications.enums.NotificationType;
import com.michael.container.notifications.model.ServiceNotificationRequest;
import com.michael.container.notifications.service.NotificationService;
import com.michael.container.registry.model.DeregisterEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class DeregisterEventListener {

  private final NotificationService notificationService;

  public DeregisterEventListener(
      @Qualifier("deregisterNotificationService") NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @EventListener(DeregisterEvent.class)
  public void deregisterEvent(DeregisterEvent event) {
    notificationService.notify(
        new ServiceNotificationRequest(
            NotificationType.SERVICE_DEREGISTERED,
            event.applicationName(),
            event.url(),
            event.version(),
            event.port()));
  }
}
