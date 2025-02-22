package com.michael.container.registry.cache.listener;

import com.michael.container.health.service.HealthCheckService;
import com.michael.container.registry.cache.enums.Key;
import org.springframework.data.redis.connection.Message;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class HealthQueueListener implements KeyListener {
  private static final Set<Key> SUPPORTED_KEYS = Set.of(Key.HEALTH_QUEUE_ENTITY);
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
}
