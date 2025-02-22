package com.michael.container.notifications.service;

import com.michael.container.notifications.client.NotificationClient;
import com.michael.container.notifications.exception.NotificationException;
import com.michael.container.notifications.model.ServiceNotificationRequest;
import com.michael.container.registry.cache.crud.CrudRegistry;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class NotificationService {
  protected static final String NOTIFICATION_URL = "%s:%s/service-registration/notify";
  private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
  private final NotificationClient notificationClient;
  private final CrudRegistry crudRegistry;

  protected NotificationService(NotificationClient notificationClient, CrudRegistry crudRegistry) {
    this.notificationClient = notificationClient;
    this.crudRegistry = crudRegistry;
  }

  public abstract void notify(ServiceNotificationRequest serviceNotificationRequest);

  protected void sendNotification(
      String url, ServiceNotificationRequest serviceNotificationRequest) {
    try {
      notificationClient.sendNotification(url, serviceNotificationRequest);
    } catch (NotificationException e) {
      logger.warn("Failed to send notification to service {}. Reason: {}", url, e.getMessage());
    }
  }

  protected void notifyServicesOfEvent(ServiceNotificationRequest serviceNotificationRequest) {
    crudRegistry.fetchAll().values().parallelStream()
        .flatMap(Collection::parallelStream)
        .filter(
            registerServiceResponse ->
                registerServiceResponse
                    .dependsOn()
                    .contains(serviceNotificationRequest.applicationName()))
        .distinct()
        .forEach(
            serviceToBeNotified -> {
              String url =
                  NOTIFICATION_URL.formatted(serviceToBeNotified.url(), serviceToBeNotified.port());
              sendNotification(url, serviceNotificationRequest);
            });
  }
}
