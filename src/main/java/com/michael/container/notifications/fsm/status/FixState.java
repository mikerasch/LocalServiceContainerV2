package com.michael.container.notifications.fsm.status;

import com.michael.container.notifications.enums.NotificationType;
import com.michael.container.notifications.enums.StatusStateEvent;
import com.michael.container.notifications.model.ServiceNotificationRequest;
import com.michael.container.notifications.service.NotificationService;
import com.michael.container.registry.model.StatusChangeEvent;
import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class FixState implements StatusChangeManager {
  private final NotificationService notificationService;

  public FixState(
      @Qualifier("generalNotificationService") NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @Override
  public void triggerEvent(@Nonnull StatusChangeEvent statusChangeEvent) {
    notificationService.notify(
        new ServiceNotificationRequest(
            NotificationType.SERVICE_MAINTENANCE,
            statusChangeEvent.applicationName(),
            statusChangeEvent.url(),
            statusChangeEvent.applicationVersion(),
            statusChangeEvent.port()));
  }

  @Override
  public StatusStateEvent getStatusStateEvent() {
    return StatusStateEvent.FIX;
  }
}
