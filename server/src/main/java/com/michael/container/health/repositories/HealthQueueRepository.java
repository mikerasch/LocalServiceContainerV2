package com.michael.container.health.repositories;

import static com.michael.container.utils.ContainerConstants.HEALTH_QUEUE_PATTERN_NAME;

import com.michael.container.registry.cache.entity.HealthQueueEntity;
import com.michael.container.registry.enums.Key;
import com.michael.container.utils.ContainerConstants;
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
   * Adds a set of {@link HealthQueueEntity} objects to the Redis list.
   * <p>
   * This method pushes all elements from the provided set of {@link HealthQueueEntity} objects
   * to the Redis list corresponding to the key defined by {@link Key#ADDED_HEALTH_QUEUE_ENTITY}.
   * After adding the entities to the list, it sends a message to the Redis pub/sub channel to notify
   * that the health queue has been populated.
   * </p>
   *
   * @param healthQueueEntity a set of {@link HealthQueueEntity} objects to be added to the Redis list
   */
  public void enqueue(@Nonnull Set<HealthQueueEntity> healthQueueEntity) {
    redisTemplate.opsForList().rightPushAll(HEALTH_QUEUE_PATTERN_NAME, healthQueueEntity);

    stringRedisTemplate.convertAndSend(
        HEALTH_QUEUE_PATTERN_NAME, Key.ADDED_HEALTH_QUEUE_ENTITY.getBody());
  }

  /**
   * Removes and returns the first {@link HealthQueueEntity} object from the Redis list.
   * <p>
   * This method pops the first element from the Redis list corresponding to the key
   * defined by {@link ContainerConstants#HEALTH_QUEUE_PATTERN_NAME}. If the list is empty, it returns {@code null}.
   * </p>
   *
   * @return the first {@link HealthQueueEntity} object from the Redis list, or {@code null} if the list is empty
   */
  public HealthQueueEntity dequeue() {
    return redisTemplate.opsForList().leftPop(HEALTH_QUEUE_PATTERN_NAME);
  }
}
