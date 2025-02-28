package com.michael.container.notifications.event;

import com.michael.container.notifications.enums.NotificationType;
import com.michael.container.notifications.model.ServiceNotificationRequest;
import com.michael.container.notifications.service.NotificationService;
import com.michael.container.registry.model.DeregisterEvent;
import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class DeregisterEventListener {

  private final NotificationService notificationService;

  public DeregisterEventListener(
      @Qualifier("generalNotificationService") NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  /**
   * Listen for {@link DeregisterEvent} and notifies the service of a SERVICE_DEREGISTERED.
   */
  @EventListener(DeregisterEvent.class)
  public void deregisterEvent(@Nonnull DeregisterEvent event) {
    notificationService.notify(
        new ServiceNotificationRequest(
            NotificationType.SERVICE_DEREGISTERED,
            event.applicationName(),
            event.url(),
            event.version(),
            event.port()));
  }
}
