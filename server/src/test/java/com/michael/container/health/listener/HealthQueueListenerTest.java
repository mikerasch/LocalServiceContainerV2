package com.michael.container.health.listener;

import com.michael.container.distributed.election.enums.MethodAccess;
import com.michael.container.health.service.HealthCheckService;
import com.michael.container.registry.enums.Key;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HealthQueueListenerTest {
  @InjectMocks HealthQueueListener healthQueueListener;
  @Mock HealthCheckService healthCheckService;

  @Test
  void accessLevel_Unrestricted() {
    MethodAccess actual = healthQueueListener.accessLevel();

    Assertions.assertEquals(MethodAccess.UNRESTRICTED, actual);
  }

  @Test
  void supports_HealthQueueEntity() {
    Assertions.assertTrue(healthQueueListener.supports(Key.ADDED_HEALTH_QUEUE_ENTITY));
  }

  @Test
  void onMessage_DelegatesToHealthCheckService() {
    healthQueueListener.onMessage(null, null);

    Mockito.verify(healthCheckService).performCheck();
  }
}
