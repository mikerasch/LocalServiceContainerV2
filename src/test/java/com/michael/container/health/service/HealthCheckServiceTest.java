package com.michael.container.health.service;

import static org.mockito.ArgumentMatchers.any;

import com.michael.container.health.client.HealthCheckClient;
import com.michael.container.health.exception.HealthCheckInvalidException;
import com.michael.container.health.repositories.HealthQueueRepository;
import com.michael.container.registry.cache.entity.BaseInstance;
import com.michael.container.registry.cache.entity.HealthQueueEntity;
import com.michael.container.registry.model.StatusChangeEvent;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class HealthCheckServiceTest {
  HealthCheckService routine;

  @Mock HealthCheckClient healthCheckClient;
  @Mock ApplicationEventPublisher eventPublisher;
  @Mock HealthQueueRepository healthQueueRepository;
  @Mock ExecutorService executorService;

  @BeforeEach
  void setup() {
    routine =
        new HealthCheckService(
            healthCheckClient, healthQueueRepository, executorService, eventPublisher);
  }

  @Test
  void checkHealth_HealthCheckFailed_RemoveFromRegistry() throws InterruptedException {
    HealthQueueEntity healthQueueEntity = new HealthQueueEntity();
    healthQueueEntity.setBaseInstanceList(
        Set.of(new BaseInstance("applicationName", 1, "test", 8080)));
    Mockito.when(healthQueueRepository.dequeue()).thenReturn(healthQueueEntity).thenReturn(null);

    Mockito.doThrow(new HealthCheckInvalidException("sad"))
        .when(healthCheckClient)
        .checkHealth("test:8080/health");
    Mockito.doNothing().when(eventPublisher).publishEvent(any(StatusChangeEvent.class));

    CountDownLatch latch = new CountDownLatch(1);

    Mockito.when(executorService.submit(any(Runnable.class)))
        .thenAnswer(
            invocation -> {
              Runnable task = invocation.getArgument(0);
              task.run();
              latch.countDown();
              return null;
            });

    routine.performCheck();

    latch.await();

    Mockito.verify(eventPublisher).publishEvent(any(StatusChangeEvent.class));
    Mockito.verify(healthCheckClient).checkHealth(any());
  }

  @Test
  void checkHealth_HealthCheckSucceeded_DoNothing() throws InterruptedException {
    HealthQueueEntity healthQueueEntity = new HealthQueueEntity();
    healthQueueEntity.setBaseInstanceList(
        Set.of(new BaseInstance("applicationName", 1, "test", 8080)));
    Mockito.when(healthQueueRepository.dequeue()).thenReturn(healthQueueEntity).thenReturn(null);

    Mockito.doNothing().when(healthCheckClient).checkHealth("test:8080/health");

    CountDownLatch latch = new CountDownLatch(1);

    Mockito.when(executorService.submit(any(Runnable.class)))
        .thenAnswer(
            invocation -> {
              Runnable task = invocation.getArgument(0);
              task.run();
              latch.countDown();
              return null;
            });

    routine.performCheck();

    latch.await();

    Mockito.verify(eventPublisher, Mockito.times(0)).publishEvent(any());
    Mockito.verify(healthCheckClient).checkHealth(any());
  }
}
