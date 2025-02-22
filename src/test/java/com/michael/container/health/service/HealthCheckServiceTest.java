package com.michael.container.health.service;

import static org.mockito.ArgumentMatchers.any;

import com.michael.container.health.client.HealthCheckClient;
import com.michael.container.health.exception.HealthCheckInvalidException;
import com.michael.container.registry.model.RegisterServiceResponse;
import com.michael.container.registry.service.ServiceRegistryService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HealthCheckServiceTest {
  @InjectMocks HealthCheckService routine;

  @Mock ServiceRegistryService registryService;

  @Mock HealthCheckClient healthCheckClient;

  @Test
  void checkHealth_HealthCheckFailed_RemoveFromRegistry() {
    Mockito.when(registryService.fetchAll())
        .thenReturn(
            Map.of(
                "applicationName",
                Set.of(
                    new RegisterServiceResponse(
                        "applicationName", 1, "test", 8080, new HashSet<>(), new HashMap<>()))));

    Mockito.doThrow(new HealthCheckInvalidException("sad"))
        .when(healthCheckClient)
        .checkHealth("test:8080/health");
    Mockito.doNothing().when(registryService).removeService(any());

    routine.checkHealth();

    Mockito.verify(registryService).fetchAll();
    Mockito.verify(registryService).removeService(any());
    Mockito.verify(healthCheckClient).checkHealth(any());
  }

  @Test
  void checkHealth_HealthCheckSucceeded_DoNothing() {
    Mockito.when(registryService.fetchAll())
        .thenReturn(
            Map.of(
                "applicationName",
                Set.of(
                    new RegisterServiceResponse(
                        "applicationName", 1, "test", 8080, new HashSet<>(), new HashMap<>()))));

    Mockito.doNothing().when(healthCheckClient).checkHealth("test:8080/health");

    routine.checkHealth();

    Mockito.verify(registryService).fetchAll();
    Mockito.verify(registryService, Mockito.times(0)).removeService(any());
    Mockito.verify(healthCheckClient).checkHealth(any());
  }
}
