package com.michael.container;

import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {"etcd.leader.key=/leader", "app.base.url=https://localhost:8080"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(DockerExtension.class)
public class IntegrationTestExtension {
  private static RedisTestConfiguration redisTestConfiguration;
  @Autowired private RedisTemplate<?, ?> redisTemplate;

  @BeforeAll
  public static void beforeAll() {
    redisTestConfiguration = new RedisTestConfiguration();
    var etcdTestConfiguration = new EtcdTestConfiguration();

    RedisTestConfiguration.setup();
    etcdTestConfiguration.setup();
  }

  @AfterEach
  public void afterEach() throws IOException, InterruptedException {
    redisTestConfiguration.tearDown();
  }
}
