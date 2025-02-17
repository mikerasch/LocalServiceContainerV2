package com.michael.container.notifications.service;

import com.michael.container.notifications.client.NotificationClient;
import com.michael.container.notifications.model.ServiceNotificationRequest;
import com.michael.container.registry.cache.crud.CrudRegistry;
import org.springframework.stereotype.Service;

@Service
public class DeregisterNotificationService extends NotificationService {
  protected DeregisterNotificationService(
      NotificationClient notificationClient, CrudRegistry crudRegistry) {
    super(notificationClient, crudRegistry);
  }

  @Override
  public void notify(ServiceNotificationRequest serviceNotificationRequest) {
    notifyServicesOfEvent(serviceNotificationRequest);
  }
}
