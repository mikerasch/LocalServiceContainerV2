package com.michael.container.notifications.repositories;

import static com.michael.container.utils.ContainerConstants.PENDING_SERVICE_QUEUE_PATTERN_NAME;

import com.michael.container.registry.cache.entity.PendingServiceNotificationEntity;
import com.michael.container.utils.ContainerConstants;
import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

@Repository
public class PendingServiceNotificationQueueRepository {
  private final RedisTemplate<String, PendingServiceNotificationEntity> redisTemplate;

  public PendingServiceNotificationQueueRepository(
      RedisTemplate<String, PendingServiceNotificationEntity> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  public void enqueue(@Nonnull PendingServiceNotificationEntity entity) {
    long delayedTime =
        System.currentTimeMillis()
            + ContainerConstants.MILLISECOND_DURATION_DELAY_OF_PENDING_SERVICE_NOTIFICATION;
    redisTemplate.opsForZSet().add(PENDING_SERVICE_QUEUE_PATTERN_NAME, entity, delayedTime);
  }

  public List<PendingServiceNotificationEntity> dequeue() {
    long currentTime = System.currentTimeMillis();

    return redisTemplate.execute(
        (RedisCallback<List<PendingServiceNotificationEntity>>)
            connection -> {
              List<PendingServiceNotificationEntity> pendingServiceNotificationEntities =
                  new ArrayList<>();

              connection.multi();

              Set<ZSetOperations.TypedTuple<PendingServiceNotificationEntity>> range =
                  redisTemplate
                      .opsForZSet()
                      .rangeByScoreWithScores(
                          PENDING_SERVICE_QUEUE_PATTERN_NAME,
                          Double.NEGATIVE_INFINITY,
                          currentTime);

              if (CollectionUtils.isNotEmpty(range)) {
                pendingServiceNotificationEntities =
                    range.stream().map(ZSetOperations.TypedTuple::getValue).toList();
                redisTemplate
                    .opsForZSet()
                    .remove(
                        PENDING_SERVICE_QUEUE_PATTERN_NAME,
                        (Object[])
                            pendingServiceNotificationEntities.toArray(
                                new PendingServiceNotificationEntity[0]));
              }

              connection.exec();
              return pendingServiceNotificationEntities;
            });
  }

  public void remove(PendingServiceNotificationEntity entity) {
    redisTemplate.opsForZSet().remove(PENDING_SERVICE_QUEUE_PATTERN_NAME, entity);
  }
}
