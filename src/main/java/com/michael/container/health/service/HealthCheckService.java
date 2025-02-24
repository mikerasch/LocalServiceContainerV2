package com.michael.container.health.service;

import com.michael.container.health.client.HealthCheckClient;
import com.michael.container.health.exception.HealthCheckInvalidException;
import com.michael.container.health.repositories.HealthQueueRepository;
import com.michael.container.registry.cache.entity.HealthQueueEntity;
import com.michael.container.registry.model.RemoveServiceRequest;
import com.michael.container.registry.service.ServiceRegistryService;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class HealthCheckService {
  private static final String HEALTH_CHECK_URL = "%s:%s/health";
  private static final Logger logger = LoggerFactory.getLogger(HealthCheckService.class);
  private final ServiceRegistryService registryService;
  private final HealthCheckClient healthCheckClient;
  private final HealthQueueRepository healthQueueRepository;
  private final ExecutorService healthCheckExecutorService;

  public HealthCheckService(
      ServiceRegistryService registryService,
      HealthCheckClient healthCheckClient,
      HealthQueueRepository healthQueueRepository,
      @Qualifier("healthCheckExecutorService") ExecutorService healthCheckExecutorService) {
    this.registryService = registryService;
    this.healthCheckClient = healthCheckClient;
    this.healthQueueRepository = healthQueueRepository;
    this.healthCheckExecutorService = healthCheckExecutorService;
  }

  /**
   * Continuously dequeues health check entities and submits them for health-check processing.
   * The method runs in a loop until there are no more health check entities in the queue (i.e., the
   * dequeued entity is null)
   */
  public void performCheck() {
    HealthQueueEntity healthQueueEntity;
    do {
      healthQueueEntity = healthQueueRepository.dequeue();
      HealthQueueEntity finalHealthQueueEntity = healthQueueEntity;
      healthCheckExecutorService.submit(() -> sendRequest(finalHealthQueueEntity));

    } while (healthQueueEntity != null);
  }

  private void sendRequest(HealthQueueEntity healthQueueEntity) {
    if (healthQueueEntity == null) {
      return;
    }
    healthQueueEntity.getBaseInstanceList().stream()
        .parallel()
        .forEach(
            serviceResponse -> {
              String healthCheckUrl =
                  HEALTH_CHECK_URL.formatted(serviceResponse.getUrl(), serviceResponse.getPort());
              try {
                healthCheckClient.checkHealth(healthCheckUrl);
              } catch (HealthCheckInvalidException healthCheckInvalidException) {
                logger.error(
                    "Health check failed for service '{}'. Error: {}",
                    healthCheckUrl,
                    healthCheckInvalidException.getMessage());
                registryService.removeService(
                    new RemoveServiceRequest(
                        serviceResponse.getApplicationName(),
                        serviceResponse.getUrl(),
                        serviceResponse.getApplicationVersion(),
                        serviceResponse.getPort()));
              }
            });
  }
}
