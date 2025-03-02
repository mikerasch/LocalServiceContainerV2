package com.michael.container.health.repositories;

import com.michael.container.RedisTestConfiguration;
import com.michael.container.distributed.election.enums.Role;
import com.michael.container.distributed.election.state.ElectionState;
import com.michael.container.registry.cache.entity.BaseInstance;
import com.michael.container.registry.cache.entity.HealthQueueEntity;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ExtendWith(MockitoExtension.class)
@Import(HealthQueueRepository.class)
class HealthQueueRepositoryTest extends RedisTestConfiguration {
  @MockitoBean StringRedisTemplate stringRedisTemplate;
  @Autowired HealthQueueRepository healthQueueRepository;

  @TestConfiguration
  static class TestConfig {
    @Bean
    public ElectionState electionState() {
      var electionState = new ElectionState();
      electionState.setRole(Role.LEADER);
      return electionState;
    }
  }

  @Test
  void enqueue_Success_MessageSentToTemplate() {
    healthQueueRepository.enqueue(Set.of(new HealthQueueEntity()));

    Mockito.verify(stringRedisTemplate).convertAndSend(Mockito.anyString(), Mockito.any());
  }

  @Test
  void enqueue_ThenDequeue_RetrieveSameObject() {
    var healthQueueEntity = new HealthQueueEntity();
    BaseInstance baseInstance = new BaseInstance();
    baseInstance.setApplicationName("APPLICATION_NAME");
    baseInstance.setApplicationVersion(1);
    baseInstance.setUrl("URL");
    baseInstance.setPort(2);
    healthQueueEntity.setBaseInstanceList(Set.of(baseInstance));

    healthQueueRepository.enqueue(Set.of(healthQueueEntity));

    HealthQueueEntity actual = healthQueueRepository.dequeue();

    Assertions.assertEquals(healthQueueEntity, actual);
  }
}
