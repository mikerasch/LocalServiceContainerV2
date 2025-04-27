package com.michael.container;

import com.michael.container.config.RedisConfiguration;
import com.michael.container.registry.cache.listener.key.KeyOrchestrator;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

@DataRedisTest
@Import({RedisConfiguration.class, KeyOrchestrator.class})
@ExtendWith(DockerExtension.class)
public class RedisTestConfiguration {
  private static final GenericContainer<?> redisContainer =
      new GenericContainer<>("redis:6.2-alpine")
          .withExposedPorts(6379)
          .waitingFor(Wait.forListeningPort());

  @BeforeAll
  public static void setup() {
    redisContainer.start();
    System.setProperty("spring.redis.host", redisContainer.getHost());
    System.setProperty("spring.redis.port", redisContainer.getMappedPort(6379).toString());
  }

  @AfterEach
  public void tearDown() throws IOException, InterruptedException {
    redisContainer.execInContainer("redis-cli", "flushall");
  }
}
