package com.michael.container.notifications.service;

import com.michael.container.notifications.client.NotificationClient;
import com.michael.container.notifications.exception.NotificationException;
import com.michael.container.notifications.model.ServiceNotificationRequest;
import com.michael.container.registry.cache.crud.CrudRegistry;
import jakarta.annotation.Nonnull;
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

  /**
   * Sends a notification to the specified service URL using the provided notification request.
   */
  protected void sendNotification(
      @Nonnull String url, @Nonnull ServiceNotificationRequest serviceNotificationRequest) {
    try {
      notificationClient.sendNotification(url, serviceNotificationRequest);
    } catch (NotificationException e) {
      logger.warn("Failed to send notification to service {}. Reason: {}", url, e.getMessage());
    }
  }

  /**
   * Fetches all registered services and filters the services that depend on
   * the application name contained in the notification request. For each such service,
   * it constructs the notification URL and sends the notification.
   */
  protected void notifyServicesOfEvent(
      @Nonnull ServiceNotificationRequest serviceNotificationRequest) {
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
