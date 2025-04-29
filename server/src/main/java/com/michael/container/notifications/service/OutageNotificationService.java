package com.michael.container.notifications.service;

import com.michael.container.notifications.client.NotificationClient;
import com.michael.container.notifications.model.ServiceNotificationRequest;
import com.michael.container.registry.cache.crud.CrudRegistry;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Service;

@Service
public class OutageNotificationService extends NotificationService {

  public OutageNotificationService(
      NotificationClient notificationClient, CrudRegistry crudRegistry) {
    super(notificationClient, crudRegistry);
  }

  @Override
  public void notify(@Nonnull ServiceNotificationRequest serviceNotificationRequest) {
    notifyServicesOfEvent(serviceNotificationRequest);
  }
}
