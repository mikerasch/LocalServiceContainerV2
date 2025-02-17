package com.michael.container.notifications.service;

import com.michael.container.notifications.client.NotificationClient;
import com.michael.container.notifications.enums.NotificationType;
import com.michael.container.notifications.model.ServiceNotificationRequest;
import com.michael.container.registry.cache.RegistryCache;
import com.michael.container.registry.cache.crud.CrudRegistry;
import com.michael.container.registry.model.DurationValue;
import com.michael.container.registry.model.RegisterServiceResponse;
import java.time.Instant;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
// TODO CLEAN UP TO FIXTURES
class RegisterNotificationServiceTest {
  RegisterNotificationService service;

  CrudRegistry crudRegistry;
  RegistryCache registryCache;

  @Mock NotificationClient notificationClient;

  @Mock ApplicationEventPublisher publisher;

  @BeforeEach
  void setup() {
    registryCache = new RegistryCache();
    crudRegistry = new CrudRegistry(registryCache, publisher);
    service = new RegisterNotificationService(notificationClient, crudRegistry);
  }

  @Test
  void notify_NotifySingleDependentService_ServiceDoesNotDependOnAnything() {
    ServiceNotificationRequest serviceNotificationRequest =
        new ServiceNotificationRequest(
            NotificationType.SERVICE_REGISTERED, "applicationName", "10.10.10.10", 1, 8080);
    Map<String, Map<RegisterServiceResponse, DurationValue>> map = new HashMap<>();
    map.put(
        "applicationName",
        Map.of(
            new RegisterServiceResponse(
                "applicationName", 1, "10.10.10.10", 8080, new HashSet<>(), new HashMap<>()),
            new DurationValue(Instant.MAX)));
    map.put(
        "someOtherApplication",
        Map.of(
            new RegisterServiceResponse(
                "someOtherApplication",
                2,
                "10.10.10.11",
                8080,
                Set.of("applicationName"),
                new HashMap<>()),
            new DurationValue(Instant.MAX)));

    registryCache.getApplicationToRegisterServiceMap().putAll(map);

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
    Map<String, Map<RegisterServiceResponse, DurationValue>> map = new HashMap<>();
    map.put(
        "applicationName",
        Map.of(
            new RegisterServiceResponse(
                "applicationName", 1, "10.10.10.10", 8080, new HashSet<>(), new HashMap<>()),
            new DurationValue(Instant.MAX)));
    map.put(
        "someOtherApplication",
        Map.of(
            new RegisterServiceResponse(
                "someOtherApplication",
                2,
                "10.10.10.11",
                8080,
                Set.of("applicationName"),
                new HashMap<>()),
            new DurationValue(Instant.MAX)));
    map.put(
        "someOtherOtherApplication",
        Map.of(
            new RegisterServiceResponse(
                "someOtherOtherApplication",
                2,
                "10.10.10.12",
                8080,
                Set.of("applicationName"),
                new HashMap<>()),
            new DurationValue(Instant.MAX)));

    registryCache.getApplicationToRegisterServiceMap().putAll(map);

    service.notify(serviceNotificationRequest);

    Mockito.verify(notificationClient, Mockito.times(2))
        .sendNotification(Mockito.any(), Mockito.any());
  }

  @Test
  void notify_ServiceDependsOnSingleService() {
    ServiceNotificationRequest serviceNotificationRequest =
        new ServiceNotificationRequest(
            NotificationType.SERVICE_REGISTERED, "applicationName", "10.10.10.10", 1, 8080);
    Map<String, Map<RegisterServiceResponse, DurationValue>> map = new HashMap<>();
    map.put(
        "applicationName",
        Map.of(
            new RegisterServiceResponse(
                "applicationName",
                1,
                "10.10.10.10",
                8080,
                Set.of("someOtherApplication"),
                new HashMap<>()),
            new DurationValue(Instant.MAX)));
    map.put(
        "someOtherApplication",
        Map.of(
            new RegisterServiceResponse(
                "someOtherApplication", 2, "10.10.10.11", 8080, new HashSet<>(), new HashMap<>()),
            new DurationValue(Instant.MAX)));

    registryCache.getApplicationToRegisterServiceMap().putAll(map);

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
    Map<String, Map<RegisterServiceResponse, DurationValue>> map = new HashMap<>();
    map.put(
        "applicationName",
        Map.of(
            new RegisterServiceResponse(
                "applicationName",
                1,
                "10.10.10.10",
                8080,
                Set.of("someOtherApplication", "someOtherOtherApplication"),
                new HashMap<>()),
            new DurationValue(Instant.MAX)));
    map.put(
        "someOtherApplication",
        Map.of(
            new RegisterServiceResponse(
                "someOtherApplication", 2, "10.10.10.11", 8080, new HashSet<>(), new HashMap<>()),
            new DurationValue(Instant.MAX)));
    map.put(
        "someOtherOtherApplication",
        Map.of(
            new RegisterServiceResponse(
                "someOtherApplication", 2, "10.10.10.12", 8080, new HashSet<>(), new HashMap<>()),
            new DurationValue(Instant.MAX)));

    registryCache.getApplicationToRegisterServiceMap().putAll(map);

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
    Map<String, Map<RegisterServiceResponse, DurationValue>> map = new HashMap<>();
    map.put(
        "applicationName",
        Map.of(
            new RegisterServiceResponse(
                "applicationName",
                1,
                "10.10.10.10",
                8080,
                Set.of("someOtherApplication", "someOtherOtherApplication"),
                new HashMap<>()),
            new DurationValue(Instant.MAX)));
    map.put(
        "someOtherApplication",
        Map.of(
            new RegisterServiceResponse(
                "someOtherApplication", 2, "10.10.10.11", 8080, new HashSet<>(), new HashMap<>()),
            new DurationValue(Instant.MAX)));

    registryCache.getApplicationToRegisterServiceMap().putAll(map);

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
    Map<String, Map<RegisterServiceResponse, DurationValue>> map = new HashMap<>();
    map.put(
        "applicationName",
        Map.of(
            new RegisterServiceResponse(
                "applicationName",
                1,
                "10.10.10.10",
                8080,
                Set.of("someOtherApplication", "someOtherOtherApplication"),
                new HashMap<>()),
            new DurationValue(Instant.MAX)));
    map.put(
        "someOtherApplication",
        Map.of(
            new RegisterServiceResponse(
                "someOtherApplication", 2, "10.10.10.11", 8080, new HashSet<>(), new HashMap<>()),
            new DurationValue(Instant.MAX)));

    registryCache.getApplicationToRegisterServiceMap().putAll(map);

    service.notify(serviceNotificationRequest);

    map.put(
        "someOtherOtherApplication",
        Map.of(
            new RegisterServiceResponse(
                "someOtherApplication", 2, "10.10.10.12", 8080, new HashSet<>(), new HashMap<>()),
            new DurationValue(Instant.MAX)));
    registryCache.getApplicationToRegisterServiceMap().putAll(map);

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
