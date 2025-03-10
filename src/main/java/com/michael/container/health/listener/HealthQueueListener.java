package com.michael.container.health.listener;

import com.michael.container.distributed.election.enums.MethodAccess;
import com.michael.container.health.service.HealthCheckService;
import com.michael.container.registry.cache.listener.key.KeyListener;
import com.michael.container.registry.enums.Key;
import java.util.Set;
import org.springframework.data.redis.connection.Message;
import org.springframework.stereotype.Service;

@Service
public class HealthQueueListener implements KeyListener {
  private static final Set<Key> SUPPORTED_KEYS = Set.of(Key.ADDED_HEALTH_QUEUE_ENTITY);
  private final HealthCheckService healthCheckService;

  public HealthQueueListener(HealthCheckService healthCheckService) {
    this.healthCheckService = healthCheckService;
  }

  @Override
  public void onMessage(Message message, byte[] pattern) {
    healthCheckService.performCheck();
  }

  @Override
  public boolean supports(Key key) {
    return SUPPORTED_KEYS.contains(key);
  }

  @Override
  public MethodAccess accessLevel() {
    return MethodAccess.UNRESTRICTED;
  }
}
