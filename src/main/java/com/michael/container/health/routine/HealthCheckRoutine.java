package com.michael.container.health.routine;

import com.michael.container.health.client.HealthCheckClient;
import com.michael.container.registry.service.ServiceRegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HealthCheckRoutine {
  private static final String HEALTH_CHECK_URL = "%s:%s/health";
  private static final Logger logger = LoggerFactory.getLogger(HealthCheckRoutine.class);
  private final ServiceRegistryService registryService;
  private final HealthCheckClient healthCheckClient;

  public HealthCheckRoutine(
      ServiceRegistryService registryService, HealthCheckClient healthCheckClient) {
    this.registryService = registryService;
    this.healthCheckClient = healthCheckClient;
  }

  @Scheduled(fixedRate = 30000)
  public void checkHealth() {
    //    logger.info("Starting health check routine.");
    //    registryService.fetchAll().values().stream()
    //        .flatMap(Collection::stream)
    //        .collect(Collectors.toSet())
    //        .parallelStream()
    //        .forEach(
    //            serviceResponse -> {
    //              String healthCheckUrl =
    //                  HEALTH_CHECK_URL.formatted(serviceResponse.url(), serviceResponse.port());
    //              try {
    //                healthCheckClient.checkHealth(healthCheckUrl);
    //              } catch (HealthCheckInvalidException healthCheckInvalidException) {
    //                logger.error(
    //                    "Health check failed for service '{}'. Error: {}",
    //                    healthCheckUrl,
    //                    healthCheckInvalidException.getMessage());
    //                registryService.removeService(
    //                    new RemoveServiceRequest(
    //                        serviceResponse.applicationName(),
    //                        serviceResponse.url(),
    //                        serviceResponse.applicationVersion(),
    //                        serviceResponse.port()));
    //              }
    //            });
  }
}
