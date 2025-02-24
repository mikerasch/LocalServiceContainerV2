package com.michael.container.health.repositories;

import com.michael.container.registry.cache.entity.HealthQueueEntity;
import com.michael.container.registry.cache.enums.Key;
import jakarta.annotation.Nonnull;
import java.util.Set;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class HealthQueueRepository {
  private final RedisTemplate<String, HealthQueueEntity> redisTemplate;
  private final StringRedisTemplate stringRedisTemplate;

  public HealthQueueRepository(
      RedisTemplate<String, HealthQueueEntity> redisTemplate,
      StringRedisTemplate stringRedisTemplate) {
    this.redisTemplate = redisTemplate;
    this.stringRedisTemplate = stringRedisTemplate;
  }

  /**
   * Adds a set of HealthQueueEntity objects to the Redis list.
   * This method pushes all elements from the provided set of HealthQueueEntity objects to
   * the Redis list corresponding to the key defined by {@link Key#HEALTH_QUEUE_ENTITY}.
   * After adding the entities to the list, it sends a message to the Redis pub/sub channel
   * indicating that the health queue has been populated.
   *
   * @param healthQueueEntity a set of {@link HealthQueueEntity}
   */
  public void enqueue(@Nonnull Set<HealthQueueEntity> healthQueueEntity) {
    redisTemplate.opsForList().rightPushAll(Key.HEALTH_QUEUE_ENTITY.getName(), healthQueueEntity);

    stringRedisTemplate.convertAndSend(
        Key.HEALTH_QUEUE_ENTITY.getName(), "Health Queue Populated.");
  }

  public HealthQueueEntity dequeue() {
    return redisTemplate.opsForList().leftPop(Key.HEALTH_QUEUE_ENTITY.getName());
  }
}
