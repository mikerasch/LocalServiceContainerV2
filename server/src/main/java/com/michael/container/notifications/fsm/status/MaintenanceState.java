package com.michael.container.notifications.fsm.status;

import com.michael.container.notifications.enums.NotificationType;
import com.michael.container.notifications.enums.StatusStateEvent;
import com.michael.container.notifications.model.ServiceNotificationRequest;
import com.michael.container.notifications.service.NotificationService;
import com.michael.container.registry.cache.crud.CrudRegistry;
import com.michael.container.registry.model.StatusChangeEvent;
import com.michael.container.utils.ContainerConstants;
import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class MaintenanceState implements StatusChangeHandler {
  private final NotificationService notificationService;
  private final CrudRegistry crudRegistry;

  public MaintenanceState(
      @Qualifier("generalNotificationService") NotificationService notificationService,
      CrudRegistry crudRegistry) {
    this.notificationService = notificationService;
    this.crudRegistry = crudRegistry;
  }

  @Override
  public void triggerEvent(@Nonnull StatusChangeEvent statusChangeEvent) {
    crudRegistry.updateTTL(
        statusChangeEvent.applicationName(),
        statusChangeEvent.url(),
        statusChangeEvent.applicationVersion(),
        statusChangeEvent.port(),
        ContainerConstants.INSTANCE_ENTITY_MAINTENANCE_TIME_TO_LIVE);
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
    return StatusStateEvent.MAINTENANCE;
  }
}
