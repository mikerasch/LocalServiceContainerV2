package com.michael.container.notifications.service;

import com.michael.container.notifications.client.NotificationClient;
import com.michael.container.notifications.model.ServiceNotificationRequest;
import com.michael.container.registry.cache.crud.CrudRegistry;
import com.michael.container.registry.model.RegisterServiceResponse;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class RegisterNotificationService extends NotificationService {
  private final CrudRegistry crudRegistry;
  private final Map<String, Set<ServiceNotificationRequest>> pendingServiceNotifications =
      new ConcurrentHashMap<>();
  private static final Logger logger = LoggerFactory.getLogger(RegisterNotificationService.class);

  protected RegisterNotificationService(
      NotificationClient notificationClient, CrudRegistry crudRegistry) {
    super(notificationClient, crudRegistry);
    this.crudRegistry = crudRegistry;
  }

  @Override
  public void notify(ServiceNotificationRequest serviceNotificationRequest) {
    notifyServicesOfEvent(serviceNotificationRequest);
    // since this is a new service, we also need to notify it of it's dependencies
    RegisterServiceResponse registerServiceResponse =
        crudRegistry
            .findOne(
                serviceNotificationRequest.applicationName(),
                serviceNotificationRequest.url(),
                serviceNotificationRequest.port(),
                serviceNotificationRequest.applicationVersion())
            .orElseThrow();

    if (registerServiceResponse.dependsOn().isEmpty()) {
      return;
    }

    registerServiceResponse
        .dependsOn()
        .forEach(
            dependencyApplicationName ->
                sendInformationOnDependency(serviceNotificationRequest, dependencyApplicationName));
  }

  @Scheduled(fixedRate = 4000L)
  public void processPendingNotifications() {
    pendingServiceNotifications.forEach((key, value) -> sendInformationOnDependency(value, key));
  }

  private void sendInformationOnDependency(
      Set<ServiceNotificationRequest> serviceNotificationRequests,
      String dependencyApplicationName) {
    serviceNotificationRequests.parallelStream()
        .forEach(
            notificationRequest ->
                sendInformationOnDependency(notificationRequest, dependencyApplicationName));
  }

  private void sendInformationOnDependency(
      ServiceNotificationRequest serviceNotificationRequest, String dependencyApplicationName) {
    Set<RegisterServiceResponse> dependencies =
        crudRegistry.findByApplicationName(dependencyApplicationName);

    if (dependencies.isEmpty()) {
      logger.info(
          "Dependency {} is currently unregistered, will be notified once available.",
          dependencyApplicationName);
      addToPendingNotification(dependencyApplicationName, serviceNotificationRequest);
      return;
    }

    shouldRemoveFromPendingNotificationMap(dependencyApplicationName, serviceNotificationRequest);

    String url =
        NOTIFICATION_URL.formatted(
            serviceNotificationRequest.url(), serviceNotificationRequest.port());

    dependencies.parallelStream()
        .forEach(registerServiceResponse -> sendNotification(url, serviceNotificationRequest));
  }

  private void shouldRemoveFromPendingNotificationMap(
      String dependencyApplicationName, ServiceNotificationRequest serviceNotificationRequest) {
    Set<ServiceNotificationRequest> pendingRequests =
        pendingServiceNotifications.getOrDefault(dependencyApplicationName, new HashSet<>());

    if (pendingRequests.isEmpty()) {
      return;
    }

    boolean removed = pendingRequests.remove(serviceNotificationRequest);

    if (removed) {
      logger.info(
          "Removed pending notification for service: {} due to dependency: {} being registered.",
          serviceNotificationRequest.applicationName(),
          dependencyApplicationName);
    }

    if (pendingRequests.isEmpty()) {
      logger.info(
          "No more pending notifications for dependency: {}, removed from pending list.",
          dependencyApplicationName);
      pendingServiceNotifications.remove(dependencyApplicationName);
    }
  }

  // TODO THIS WILL CAUSE DOUBLE INSERTS. NOT A BIG DEAL - BUT IS INEFFICIENT
  private void addToPendingNotification(
      String dependencyApplicationName, ServiceNotificationRequest serviceNotificationRequest) {
    pendingServiceNotifications
        .computeIfAbsent(dependencyApplicationName, k -> ConcurrentHashMap.newKeySet())
        .add(serviceNotificationRequest);
  }
}
