package com.michael.container.notifications.service;

import com.michael.container.annotations.SkipIfAutomationEnvironment;
import com.michael.container.annotations.SkipIfFollower;
import com.michael.container.notifications.client.NotificationClient;
import com.michael.container.notifications.enums.NotifyEvent;
import com.michael.container.notifications.model.ServiceNotificationRequest;
import com.michael.container.notifications.repositories.PendingServiceNotificationQueueRepository;
import com.michael.container.registry.cache.crud.CrudRegistry;
import com.michael.container.registry.cache.entity.PendingServiceNotificationEntity;
import com.michael.container.registry.model.RegisterServiceResponse;
import com.michael.spring.utils.logger.annotations.ExecutionTime;
import jakarta.annotation.Nonnull;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class RegisterNotificationService extends NotificationService {
  private final CrudRegistry crudRegistry;
  private final PendingServiceNotificationQueueRepository pendingServiceNotificationQueueRepository;
  private final ConversionService conversionService;
  private static final Logger logger = LoggerFactory.getLogger(RegisterNotificationService.class);

  public RegisterNotificationService(
      NotificationClient notificationClient,
      CrudRegistry crudRegistry,
      PendingServiceNotificationQueueRepository pendingServiceNotificationQueueRepository,
      ConversionService conversionService) {
    super(notificationClient, crudRegistry);
    this.crudRegistry = crudRegistry;
    this.pendingServiceNotificationQueueRepository = pendingServiceNotificationQueueRepository;
    this.conversionService = conversionService;
  }

  @Override
  public void notify(@Nonnull ServiceNotificationRequest serviceNotificationRequest) {
    notifyServicesOfEvent(serviceNotificationRequest);
    // since this is a new service, we also need to notify it of its dependencies
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
                sendInformationOnDependency(
                    serviceNotificationRequest, dependencyApplicationName, NotifyEvent.GENERAL));
  }

  @Scheduled(fixedRate = 4000L)
  @SkipIfFollower
  @SkipIfAutomationEnvironment
  @ExecutionTime
  public void processPendingNotifications() {
    pendingServiceNotificationQueueRepository.dequeue().parallelStream()
        .forEach(
            pendingServiceNotificationEntity ->
                sendInformationOnDependency(
                    conversionService.convert(
                        pendingServiceNotificationEntity, ServiceNotificationRequest.class),
                    pendingServiceNotificationEntity.getDependencyApplicationName(),
                    NotifyEvent.SCHEDULED));
  }

  private void sendInformationOnDependency(
      ServiceNotificationRequest serviceNotificationRequest,
      String dependencyApplicationName,
      NotifyEvent event) {
    Set<RegisterServiceResponse> dependencies =
        crudRegistry.findByApplicationName(dependencyApplicationName);

    if (dependencies.isEmpty()) {
      logger.info(
          "Dependency {} is currently unregistered, will be notified once available.",
          dependencyApplicationName);
      addToPendingNotification(serviceNotificationRequest, dependencyApplicationName);
      return;
    }

    if (event == NotifyEvent.GENERAL) {
      removeFromPendingNotifications(serviceNotificationRequest, dependencyApplicationName);
    }

    String url =
        NOTIFICATION_URL.formatted(
            serviceNotificationRequest.url(), serviceNotificationRequest.port());

    dependencies.parallelStream()
        .forEach(registerServiceResponse -> sendNotification(url, serviceNotificationRequest));
  }

  private void removeFromPendingNotifications(
      ServiceNotificationRequest serviceNotificationRequest, String dependencyApplicationName) {
    var entity =
        conversionService.convert(
            serviceNotificationRequest, PendingServiceNotificationEntity.class);
    entity.setDependencyApplicationName(dependencyApplicationName);
    pendingServiceNotificationQueueRepository.remove(entity);
  }

  private void addToPendingNotification(
      ServiceNotificationRequest serviceNotificationRequest, String dependencyApplicationName) {
    var entity =
        conversionService.convert(
            serviceNotificationRequest, PendingServiceNotificationEntity.class);
    entity.setDependencyApplicationName(dependencyApplicationName);
    pendingServiceNotificationQueueRepository.enqueue(entity);
  }
}
