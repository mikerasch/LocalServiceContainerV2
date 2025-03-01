package com.michael.container.health.service;

import com.michael.container.health.client.HealthCheckClient;
import com.michael.container.health.exception.HealthCheckInvalidException;
import com.michael.container.health.repositories.HealthQueueRepository;
import com.michael.container.registry.cache.entity.HealthQueueEntity;
import com.michael.container.registry.enums.Status;
import com.michael.container.registry.model.StatusChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class HealthCheckService {
  private static final String HEALTH_CHECK_URL = "%s:%s/health";
  private static final Logger logger = LoggerFactory.getLogger(HealthCheckService.class);
  private final HealthCheckClient healthCheckClient;
  private final HealthQueueRepository healthQueueRepository;
  private final ExecutorService healthCheckExecutorService;
  private final ApplicationEventPublisher eventPublisher;

  public HealthCheckService(
      HealthCheckClient healthCheckClient,
      HealthQueueRepository healthQueueRepository,
      @Qualifier("healthCheckExecutorService") ExecutorService healthCheckExecutorService,
      ApplicationEventPublisher eventPublisher) {
    this.healthCheckClient = healthCheckClient;
    this.healthQueueRepository = healthQueueRepository;
    this.healthCheckExecutorService = healthCheckExecutorService;
    this.eventPublisher = eventPublisher;
  }

  /**
   * Continuously dequeues health check entities and submits them for health-check processing.
   * The method runs in a loop until there are no more health check entities in the queue (i.e., the
   * dequeued entity is null)
   */
  public List<Future<?>> performCheck() {
    List<Future<?>> futures = new ArrayList<>();
    HealthQueueEntity healthQueueEntity;
    do {
      healthQueueEntity = healthQueueRepository.dequeue();
      HealthQueueEntity finalHealthQueueEntity = healthQueueEntity;
      futures.add(healthCheckExecutorService.submit(() -> sendRequest(finalHealthQueueEntity)));

    } while (healthQueueEntity != null);
    return futures;
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
                eventPublisher.publishEvent(
                    new StatusChangeEvent(
                        serviceResponse.getApplicationName(),
                        serviceResponse.getUrl(),
                        serviceResponse.getApplicationVersion(),
                        serviceResponse.getPort(),
                        Status.HEALTHY,
                        Status.DOWN));
              }
            });
  }
}
