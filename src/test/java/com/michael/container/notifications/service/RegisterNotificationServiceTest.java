package com.michael.container.notifications.service;

import com.michael.container.RedisTestConfiguration;
import com.michael.container.distributed.election.enums.Role;
import com.michael.container.distributed.election.state.ElectionState;
import com.michael.container.notifications.client.NotificationClient;
import com.michael.container.notifications.enums.NotificationType;
import com.michael.container.notifications.model.ServiceNotificationRequest;
import com.michael.container.registry.cache.crud.CrudRegistry;
import com.michael.container.registry.cache.repositories.ApplicationRepository;
import com.michael.container.registry.cache.repositories.InstanceRepository;
import com.michael.container.registry.enums.Status;
import com.michael.container.registry.mapper.InstanceEntityToRegisterServiceResponseMapper;
import com.michael.container.registry.mapper.RegisterServiceResponseToInstanceEntityMapper;
import com.michael.container.registry.model.RegisterServiceResponse;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DataRedisTest
// TODO CLEAN UP TO FIXTURES
class RegisterNotificationServiceTest extends RedisTestConfiguration {
  RegisterNotificationService service;

  CrudRegistry crudRegistry;

  @Mock NotificationClient notificationClient;

  @Mock ApplicationEventPublisher publisher;
  @Autowired ApplicationRepository applicationRepository;
  @Autowired InstanceRepository instanceRepository;

  @TestConfiguration
  static class TestConfig {
    @Bean
    public ElectionState electionState() {
      var electionState = new ElectionState();
      electionState.setRole(Role.LEADER);
      return electionState;
    }
  }

  @BeforeEach
  void beforeEach() {
    DefaultConversionService defaultConversionService = new DefaultConversionService();
    defaultConversionService.addConverter(new RegisterServiceResponseToInstanceEntityMapper());
    defaultConversionService.addConverter(new InstanceEntityToRegisterServiceResponseMapper());
    crudRegistry =
        new CrudRegistry(
            publisher, applicationRepository, instanceRepository, defaultConversionService);
    service = new RegisterNotificationService(notificationClient, crudRegistry);
  }

  @Test
  void notify_NotifySingleDependentService_ServiceDoesNotDependOnAnything() {
    ServiceNotificationRequest serviceNotificationRequest =
        new ServiceNotificationRequest(
            NotificationType.SERVICE_REGISTERED, "applicationName", "10.10.10.10", 1, 8080);
    Set<RegisterServiceResponse> set = new HashSet<>();
    set.add(
        new RegisterServiceResponse(
            "applicationName",
            1,
            "10.10.10.10",
            8080,
            Status.STARTING,
            new HashSet<>(),
            new HashMap<>()));
    set.add(
        new RegisterServiceResponse(
            "someOtherApplication",
            2,
            "10.10.10.11",
            8080,
            Status.STARTING,
            Set.of("applicationName"),
            new HashMap<>()));

    set.forEach(response -> crudRegistry.insert(response));

    service.notify(serviceNotificationRequest);

    Mockito.verify(notificationClient, Mockito.times(1))
        .sendNotification(
            Mockito.eq("10.10.10.11:8080/service-registration/notify"), Mockito.any());
  }

  @Test
  void notify_NotifyMultipleDependentService_ServiceDoesNotDependOnAnything() {
    ServiceNotificationRequest serviceNotificationRequest =
        new ServiceNotificationRequest(
            NotificationType.SERVICE_REGISTERED, "applicationName", "10.10.10.10", 1, 8080);
    Set<RegisterServiceResponse> set = new HashSet<>();
    set.add(
        new RegisterServiceResponse(
            "applicationName",
            1,
            "10.10.10.10",
            8080,
            Status.STARTING,
            new HashSet<>(),
            new HashMap<>()));
    set.add(
        new RegisterServiceResponse(
            "someOtherApplication",
            2,
            "10.10.10.11",
            8080,
            Status.STARTING,
            Set.of("applicationName"),
            new HashMap<>()));
    set.add(
        new RegisterServiceResponse(
            "someOtherOtherApplication",
            2,
            "10.10.10.12",
            8080,
            Status.STARTING,
            Set.of("applicationName"),
            new HashMap<>()));

    set.forEach(response -> crudRegistry.insert(response));

    service.notify(serviceNotificationRequest);

    Mockito.verify(notificationClient, Mockito.times(2))
        .sendNotification(Mockito.any(), Mockito.any());
  }

  @Test
  void notify_ServiceDependsOnSingleService() {
    ServiceNotificationRequest serviceNotificationRequest =
        new ServiceNotificationRequest(
            NotificationType.SERVICE_REGISTERED, "applicationName", "10.10.10.10", 1, 8080);
    Set<RegisterServiceResponse> set = new HashSet<>();
    set.add(
        new RegisterServiceResponse(
            "applicationName",
            1,
            "10.10.10.10",
            8080,
            Status.STARTING,
            Set.of("someOtherApplication"),
            new HashMap<>()));
    set.add(
        new RegisterServiceResponse(
            "someOtherApplication",
            2,
            "10.10.10.11",
            8080,
            Status.STARTING,
            new HashSet<>(),
            new HashMap<>()));

    set.forEach(response -> crudRegistry.insert(response));

    service.notify(serviceNotificationRequest);

    Mockito.verify(notificationClient, Mockito.times(1))
        .sendNotification(
            Mockito.eq("10.10.10.10:8080/service-registration/notify"), Mockito.any());
  }

  @Test
  void notify_ServiceDependsOnMultipleServices() {
    ServiceNotificationRequest serviceNotificationRequest =
        new ServiceNotificationRequest(
            NotificationType.SERVICE_REGISTERED, "applicationName", "10.10.10.10", 1, 8080);
    Set<RegisterServiceResponse> set = new HashSet<>();
    set.add(
        new RegisterServiceResponse(
            "applicationName",
            1,
            "10.10.10.10",
            8080,
            Status.STARTING,
            Set.of("someOtherApplication", "someOtherOtherApplication"),
            new HashMap<>()));
    set.add(
        new RegisterServiceResponse(
            "someOtherApplication",
            2,
            "10.10.10.11",
            8080,
            Status.STARTING,
            new HashSet<>(),
            new HashMap<>()));
    set.add(
        new RegisterServiceResponse(
            "someOtherApplication",
            2,
            "10.10.10.12",
            8080,
            Status.STARTING,
            new HashSet<>(),
            new HashMap<>()));

    set.forEach(response -> crudRegistry.insert(response));

    service.notify(serviceNotificationRequest);

    Mockito.verify(notificationClient, Mockito.times(2))
        .sendNotification(Mockito.any(), Mockito.any());
  }

  @Test
  @SuppressWarnings("unchecked")
  void notify_ServiceDependsOnMultipleServices_OneServiceNotRegisteredYet() {
    ServiceNotificationRequest serviceNotificationRequest =
        new ServiceNotificationRequest(
            NotificationType.SERVICE_REGISTERED, "applicationName", "10.10.10.10", 1, 8080);
    Set<RegisterServiceResponse> set = new HashSet<>();
    set.add(
        new RegisterServiceResponse(
            "applicationName",
            1,
            "10.10.10.10",
            8080,
            Status.STARTING,
            Set.of("someOtherApplication", "someOtherOtherApplication"),
            new HashMap<>()));
    set.add(
        new RegisterServiceResponse(
            "someOtherApplication",
            2,
            "10.10.10.11",
            8080,
            Status.STARTING,
            new HashSet<>(),
            new HashMap<>()));

    set.forEach(response -> crudRegistry.insert(response));

    service.notify(serviceNotificationRequest);

    Mockito.verify(notificationClient, Mockito.times(1))
        .sendNotification(Mockito.any(), Mockito.any());
    Assertions.assertTrue(
        ((Map<String, Object>)
                Objects.requireNonNull(
                    ReflectionTestUtils.getField(service, "pendingServiceNotifications")))
            .containsKey("someOtherOtherApplication"));
  }

  @Test
  @SuppressWarnings("unchecked")
  void
      notify_ServiceDependsOnMultipleServices_OneServiceNotRegisteredYet_ScheduledJobRuns_FindsService() {
    ServiceNotificationRequest serviceNotificationRequest =
        new ServiceNotificationRequest(
            NotificationType.SERVICE_REGISTERED, "applicationName", "10.10.10.10", 1, 8080);
    Set<RegisterServiceResponse> set = new HashSet<>();
    set.add(
        new RegisterServiceResponse(
            "applicationName",
            1,
            "10.10.10.10",
            8080,
            Status.STARTING,
            Set.of("someOtherApplication", "someOtherOtherApplication"),
            new HashMap<>()));
    set.add(
        new RegisterServiceResponse(
            "someOtherApplication",
            2,
            "10.10.10.11",
            8080,
            Status.STARTING,
            new HashSet<>(),
            new HashMap<>()));

    set.forEach(response -> crudRegistry.insert(response));

    service.notify(serviceNotificationRequest);

    set.add(
        new RegisterServiceResponse(
            "someOtherOtherApplication",
            2,
            "10.10.10.12",
            8080,
            Status.STARTING,
            new HashSet<>(),
            new HashMap<>()));
    set.forEach(response -> crudRegistry.insert(response));

    service.processPendingNotifications();

    Mockito.verify(notificationClient, Mockito.times(2))
        .sendNotification(Mockito.any(), Mockito.any());
    Assertions.assertTrue(
        ((Map<String, Object>)
                Objects.requireNonNull(
                    ReflectionTestUtils.getField(service, "pendingServiceNotifications")))
            .isEmpty());
  }
}
