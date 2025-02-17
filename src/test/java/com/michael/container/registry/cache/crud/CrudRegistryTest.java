package com.michael.container.registry.cache.crud;

import com.michael.container.registry.cache.RegistryCache;
import com.michael.container.registry.model.DurationValue;
import com.michael.container.registry.model.RegisterServiceResponse;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class CrudRegistryTest {
  CrudRegistry crudRegistry;
  RegistryCache registryCache;
  @Mock ApplicationEventPublisher eventPublisher;

  @BeforeEach
  void setup() {
    registryCache = new RegistryCache();
    crudRegistry = new CrudRegistry(registryCache, eventPublisher);
  }

  @Test
  void insert_InsertsIntoCache() {
    var registerServiceResponse =
        new RegisterServiceResponse(
            "applicationName", 1, "localhost", 8080, new HashSet<>(), new HashMap<>());

    crudRegistry.insert(registerServiceResponse);

    Assertions.assertTrue(
        registryCache.getApplicationToRegisterServiceMap().values().stream()
            .flatMap(x -> x.keySet().stream())
            .collect(Collectors.toSet())
            .contains(registerServiceResponse));
  }

  @Test
  void fetchAll_RetrievesNewMap() {
    var registerServiceResponse =
        new RegisterServiceResponse(
            "applicationName", 1, "localhost", 8080, new HashSet<>(), new HashMap<>());
    crudRegistry.insert(registerServiceResponse);

    Map<RegisterServiceResponse, DurationValue> response =
        crudRegistry.fetchAll().get("applicationName");

    Assertions.assertAll(
        () -> Assertions.assertEquals(1, response.size()),
        () -> Assertions.assertTrue(response.containsKey(registerServiceResponse)));

    response.put(
        new RegisterServiceResponse(
            "applicationName", 1, "test", 9090, new HashSet<>(), new HashMap<>()),
        new DurationValue(Instant.now()));

    Assertions.assertEquals(1, crudRegistry.fetchAll().size());
  }

  @Test
  void findOne_ResponseNotFound() {
    RegisterServiceResponse response =
        crudRegistry.findOne("applicationName", "test", 9090, 1).orElse(null);

    Assertions.assertNull(response);
  }

  @Test
  void findOne_ResponseFound() {
    crudRegistry.insert(
        new RegisterServiceResponse(
            "applicationName", 1, "test", 9090, new HashSet<>(), new HashMap<>()));

    RegisterServiceResponse response =
        crudRegistry.findOne("applicationName", "test", 9090, 1).orElse(null);

    Assertions.assertAll(
        () -> Assertions.assertNotNull(response),
        () -> Assertions.assertEquals("test", response.url()),
        () -> Assertions.assertEquals(9090, response.port()));
  }

  @Test
  void remove_WithHostNameAndPort() {
    crudRegistry.insert(
        new RegisterServiceResponse(
            "applicationName", 1, "test", 9090, new HashSet<>(), new HashMap<>()));

    crudRegistry.remove("applicationName", "test", 1, 9090);

    Assertions.assertTrue(
        crudRegistry.fetchAll().values().stream()
            .flatMap(x -> x.keySet().stream())
            .collect(Collectors.toSet())
            .isEmpty());
  }

  @Test
  void remove_WithResponse() {
    var registerServiceResponse =
        new RegisterServiceResponse(
            "applicationName", 1, "url", 9090, new HashSet<>(), new HashMap<>());

    crudRegistry.remove(
        registerServiceResponse.applicationName(),
        registerServiceResponse.url(),
        registerServiceResponse.applicationVersion(),
        registerServiceResponse.port());

    Assertions.assertTrue(crudRegistry.fetchAll().isEmpty());
  }
}
