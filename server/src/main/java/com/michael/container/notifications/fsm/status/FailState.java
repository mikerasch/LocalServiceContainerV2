package com.michael.container.notifications.fsm.status;

import com.michael.container.notifications.enums.NotificationType;
import com.michael.container.notifications.enums.StatusStateEvent;
import com.michael.container.notifications.model.ServiceNotificationRequest;
import com.michael.container.notifications.service.NotificationService;
import com.michael.container.registry.model.StatusChangeEvent;
import com.michael.container.registry.service.ServiceRegistryService;
import com.michael.contract.resources.validations.enums.Status;
import com.michael.contract.resources.validations.requests.UpdateStatusRequest;
import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class FailState implements StatusChangeHandler {
  private final ServiceRegistryService serviceRegistryService;
  private final NotificationService notificationService;

  public FailState(
      ServiceRegistryService serviceRegistryService,
      @Qualifier("outageNotificationService") NotificationService notificationService) {
    this.serviceRegistryService = serviceRegistryService;
    this.notificationService = notificationService;
  }

  @Override
  public void triggerEvent(@Nonnull StatusChangeEvent statusChangeEvent) {
    serviceRegistryService.updateStatusOnService(
        new UpdateStatusRequest(
            statusChangeEvent.applicationName(),
            statusChangeEvent.applicationVersion(),
            statusChangeEvent.url(),
            statusChangeEvent.port(),
            Status.DOWN),
        false);
    notificationService.notify(
        new ServiceNotificationRequest(
            NotificationType.SERVICE_OUTAGE,
            statusChangeEvent.applicationName(),
            statusChangeEvent.url(),
            statusChangeEvent.applicationVersion(),
            statusChangeEvent.port()));
  }

  @Override
  public StatusStateEvent getStatusStateEvent() {
    return StatusStateEvent.FAIL;
  }
}
