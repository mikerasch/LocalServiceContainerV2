package com.michael.container.notifications.auto;

import com.michael.container.notifications.client.NotificationClient;
import com.michael.container.notifications.event.DeregisterEventListener;
import com.michael.container.notifications.fsm.status.BeginState;
import com.michael.container.notifications.fsm.status.FailState;
import com.michael.container.notifications.fsm.status.MaintenanceState;
import com.michael.container.notifications.fsm.status.RestartState;
import com.michael.container.notifications.fsm.status.StatusChangeHandler;
import com.michael.container.notifications.fsm.status.StatusChangeOrchestrator;
import com.michael.container.notifications.mapper.PendingServiceNotificationEntityToServiceNotificationRequestMapper;
import com.michael.container.notifications.mapper.ServiceNotificationRequestToPendingServiceNotificationEntityMapper;
import com.michael.container.notifications.repositories.PendingServiceNotificationQueueRepository;
import com.michael.container.notifications.service.GeneralNotificationService;
import com.michael.container.notifications.service.NotificationService;
import com.michael.container.notifications.service.OutageNotificationService;
import com.michael.container.notifications.service.RegisterNotificationService;
import com.michael.container.registry.cache.crud.CrudRegistry;
import com.michael.container.registry.service.ServiceRegistryService;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.web.client.RestClient;

public class NotificationsAutoConfiguration {

  @Configuration
  @EnableRedisRepositories("com.michael.container.notifications.repositories")
  public static class RepositoryConfig {}

  @Configuration
  public static class ClientConfig {
    @ConditionalOnMissingBean
    @Bean
    public NotificationClient notificationClient(RestClient.Builder restClientBuilder) {
      return new NotificationClient(restClientBuilder);
    }
  }

  @Configuration
  public static class EventsConfig {
    @ConditionalOnMissingBean
    @Bean
    public DeregisterEventListener deregisterEventListener(
        NotificationService notificationService) {
      return new DeregisterEventListener(notificationService);
    }
  }

  @Configuration
  public static class StatusChangeConfig {
    @ConditionalOnMissingBean
    @Bean
    public StatusChangeHandler beginState(NotificationService notificationService) {
      return new BeginState(notificationService);
    }

    @ConditionalOnMissingBean
    @Bean
    public StatusChangeHandler failState(
        NotificationService notificationService, ServiceRegistryService serviceRegistryService) {
      return new FailState(serviceRegistryService, notificationService);
    }

    @ConditionalOnMissingBean
    @Bean
    public StatusChangeHandler maintenanceState(
        NotificationService notificationService, CrudRegistry crudRegistry) {
      return new MaintenanceState(notificationService, crudRegistry);
    }

    @ConditionalOnMissingBean
    @Bean
    public StatusChangeHandler restartState() {
      return new RestartState();
    }

    @ConditionalOnMissingBean
    @Bean
    public StatusChangeOrchestrator statusChangeOrchestrator(
        Set<StatusChangeHandler> statusChangeHandlerSet) {
      return new StatusChangeOrchestrator(statusChangeHandlerSet);
    }
  }

  @Configuration
  public static class MapperConfig {

    @ConditionalOnMissingBean
    @Bean
    public PendingServiceNotificationEntityToServiceNotificationRequestMapper
        pendingServiceNotificationEntityToServiceNotificationRequestMapper() {
      return new PendingServiceNotificationEntityToServiceNotificationRequestMapper();
    }

    @ConditionalOnMissingBean
    @Bean
    public ServiceNotificationRequestToPendingServiceNotificationEntityMapper
        serviceNotificationRequestToPendingServiceNotificationEntityMapper() {
      return new ServiceNotificationRequestToPendingServiceNotificationEntityMapper();
    }
  }

  @Configuration
  public static class ServiceConfig {
    @ConditionalOnMissingBean
    @Bean
    public NotificationService generalNotificationService(
        NotificationClient notificationClient, CrudRegistry crudRegistry) {
      return new GeneralNotificationService(notificationClient, crudRegistry);
    }

    @ConditionalOnMissingBean
    @Bean
    public NotificationService outageNotificationService(
        NotificationClient notificationClient, CrudRegistry crudRegistry) {
      return new OutageNotificationService(notificationClient, crudRegistry);
    }

    @ConditionalOnMissingBean
    @Bean
    public RegisterNotificationService registerNotificationService(
        NotificationClient notificationClient,
        CrudRegistry crudRegistry,
        PendingServiceNotificationQueueRepository pendingServiceNotificationQueueRepository,
        ConversionService conversionService) {
      return new RegisterNotificationService(
          notificationClient,
          crudRegistry,
          pendingServiceNotificationQueueRepository,
          conversionService);
    }
  }
}
