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

  /**
   * Adds a {@link PendingServiceNotificationEntity} to the Redis sorted set for delayed processing.
   * <p>
   * The entity is added to the sorted set with a score equal to the current system time plus a
   * predefined delay (configured in {@link ContainerConstants#MILLISECOND_DURATION_DELAY_OF_PENDING_SERVICE_NOTIFICATION}).
   * This ensures that the notification will be processed after the specified delay.
   * </p>
   *
   * @param entity the {@link PendingServiceNotificationEntity} to be added to the Redis sorted set
   */
  public void enqueue(@Nonnull PendingServiceNotificationEntity entity) {
    long delayedTime =
        System.currentTimeMillis()
            + ContainerConstants.MILLISECOND_DURATION_DELAY_OF_PENDING_SERVICE_NOTIFICATION;
    redisTemplate.opsForZSet().add(PENDING_SERVICE_QUEUE_PATTERN_NAME, entity, delayedTime);
  }

  /**
   * Dequeues pending service notifications from the Redis sorted set.
   * <p>
   * This method retrieves all {@link PendingServiceNotificationEntity} objects from the sorted set
   * in Redis where the score is less than or equal to the current system time (i.e., pending notifications
   * that are ready to be processed). The method then removes the dequeued entities from the Redis sorted set.
   * </p>
   *
   * @return a list of {@link PendingServiceNotificationEntity} objects that are ready to be processed
   */
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
