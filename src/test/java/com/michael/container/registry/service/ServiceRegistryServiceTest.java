package com.michael.container.registry.service;

import com.michael.container.registry.cache.crud.CrudRegistry;
import com.michael.container.registry.mapper.RegisterServiceRequestToRegisterServiceResponseMapper;
import com.michael.container.registry.model.RegisterServiceRequest;
import com.michael.container.registry.model.RegisterServiceResponse;
import com.michael.container.registry.model.RemoveServiceRequest;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.convert.ApplicationConversionService;

@ExtendWith(MockitoExtension.class)
class ServiceRegistryServiceTest {
  ServiceRegistryService registryService;

  @Mock CrudRegistry crudRegistry;

  @BeforeEach
  void setup() {
    var converter = new ApplicationConversionService();
    converter.addConverter(new RegisterServiceRequestToRegisterServiceResponseMapper());

    registryService = new ServiceRegistryService(converter, crudRegistry);
  }

  @Test
  void registerService_Success() {
    Mockito.doNothing().when(crudRegistry).insert(Mockito.any());

    registryService.registerService(
        new RegisterServiceRequest(
            "applicationName", 1, "localhost", 8080, new HashSet<>(), new HashMap<>()));

    Mockito.verify(crudRegistry).insert(Mockito.any());
  }

  @Test
  void fetchAll_Success() {
    var registerServiceResponse =
        new RegisterServiceResponse(
            "applicationName", 1, "localhost", 8080, new HashSet<>(), new HashMap<>());
    Mockito.when(crudRegistry.fetchAll())
        .thenReturn(Map.of("applicationName", Set.of(registerServiceResponse)));

    Set<RegisterServiceResponse> response =
        registryService.fetchAll().values().stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());

    Assertions.assertAll(
        () -> Assertions.assertEquals(1, response.size()),
        () -> Assertions.assertTrue(response.contains(registerServiceResponse)));
  }

  @Test
  void removeService_Success() {
    Mockito.doNothing()
        .when(crudRegistry)
        .remove(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());

    registryService.removeService(new RemoveServiceRequest("applicationName", "test", 1, 8080));

    Mockito.verify(crudRegistry)
        .remove(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());
  }
}
