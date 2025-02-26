package com.michael.container;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {"etcd.leader.key=/leader"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTestExtension {
  private static RedisTestConfiguration redisTestConfiguration;
  @Autowired private RedisTemplate<?, ?> redisTemplate;

  @BeforeAll
  public static void beforeAll() {
    redisTestConfiguration = new RedisTestConfiguration();
    var etcdTestConfiguration = new EtcdTestConfiguration();

    RedisTestConfiguration.setup();
    etcdTestConfiguration.setUp();
  }

  @AfterEach
  public void afterEach() {
    redisTestConfiguration.tearDown(redisTemplate);
  }
}
