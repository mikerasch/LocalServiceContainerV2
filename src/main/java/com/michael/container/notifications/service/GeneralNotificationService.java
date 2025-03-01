package com.michael.container.notifications.service;

import com.michael.container.notifications.client.NotificationClient;
import com.michael.container.notifications.model.ServiceNotificationRequest;
import com.michael.container.registry.cache.crud.CrudRegistry;
import org.springframework.stereotype.Service;

@Service
public class GeneralNotificationService extends NotificationService {
  protected GeneralNotificationService(
      NotificationClient notificationClient, CrudRegistry crudRegistry) {
    super(notificationClient, crudRegistry);
  }

  @Override
  public void notify(ServiceNotificationRequest serviceNotificationRequest) {
    notifyServicesOfEvent(serviceNotificationRequest);
  }
}
