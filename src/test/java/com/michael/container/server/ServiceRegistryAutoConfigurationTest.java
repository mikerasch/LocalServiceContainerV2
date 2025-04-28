package com.michael.container.server;

import com.michael.container.DockerExtension;
import com.michael.container.EtcdTestConfiguration;
import com.michael.container.RedisTestConfiguration;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@TestPropertySource(properties = {"etcd.leader.key=/leader"})
@ExtendWith({SpringExtension.class, DockerExtension.class})
@ContextConfiguration(classes = ServiceRegistryAutoConfigurationTest.Config.class)
class ServiceRegistryAutoConfigurationTest {
  private static RedisTestConfiguration redisTestConfiguration;

  @Autowired private ApplicationContext applicationContext;

  @TestConfiguration
  @EnableServiceRegistry
  @ComponentScan(basePackages = "com.michael.container")
  static class Config {}

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

  @Test
  void contextLoads() {}
}
