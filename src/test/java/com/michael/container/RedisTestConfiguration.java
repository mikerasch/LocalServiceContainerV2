package com.michael.container;

import com.michael.container.config.RedisConfiguration;
import com.michael.container.registry.cache.listener.KeyOrchestrator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

@DataRedisTest
@Import({RedisConfiguration.class, KeyOrchestrator.class})
public class RedisTestConfiguration {
  private static final GenericContainer<?> redisContainer =
      new GenericContainer<>("redis:6.2-alpine")
          .withExposedPorts(6379)
          .waitingFor(Wait.forListeningPort());

  @Autowired RedisTemplate<?, ?> redisTemplate;

  @BeforeAll
  public static void setup() {
    redisContainer.start();
    System.setProperty("spring.redis.host", redisContainer.getHost());
    System.setProperty("spring.redis.port", redisContainer.getMappedPort(6379).toString());
  }

  public void tearDown(RedisTemplate<?, ?> redisTemplate) {
    if (redisTemplate == null) {
      return;
    }
    redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
  }

  @AfterEach
  public void tearDown() {
    if (redisTemplate == null) {
      return;
    }
    redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
  }
}
