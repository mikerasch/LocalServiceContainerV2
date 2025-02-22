package com.michael.container.health.repositories;

import com.michael.container.registry.cache.entity.ApplicationEntity;
import com.michael.container.registry.cache.enums.Key;
import java.util.List;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class HealthQueueRepository {
  private final RedisTemplate<String, ApplicationEntity> redisTemplate;
  private final StringRedisTemplate stringRedisTemplate;

  public HealthQueueRepository(
      RedisTemplate<String, ApplicationEntity> redisTemplate,
      StringRedisTemplate stringRedisTemplate) {
    this.redisTemplate = redisTemplate;
    this.stringRedisTemplate = stringRedisTemplate;
  }

  public void enqueue(List<ApplicationEntity> applicationEntityList) {
    redisTemplate
        .opsForList()
        .rightPushAll(Key.HEALTH_QUEUE_ENTITY.getName(), applicationEntityList);

    stringRedisTemplate.convertAndSend(
        Key.HEALTH_QUEUE_ENTITY.getName(), "Health Queue Populated.");
  }

  public ApplicationEntity dequeue() {
    return redisTemplate.opsForList().leftPop(Key.HEALTH_QUEUE_ENTITY.getName());
  }
}
