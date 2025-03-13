package com.michael.container.notifications.repositories;

import com.michael.container.registry.cache.entity.PendingServiceNotificationEntity;
import jakarta.annotation.Nonnull;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;


import static com.michael.container.utils.ContainerConstants.PENDING_SERVICE_QUEUE_PATTERN_NAME;

@Repository
public class PendingServiceNotificationQueueRepository {
    private final RedisTemplate<String, PendingServiceNotificationEntity> redisTemplate;


    public PendingServiceNotificationQueueRepository(RedisTemplate<String, PendingServiceNotificationEntity> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void enqueue(@Nonnull PendingServiceNotificationEntity entity, long delayInMillis) {
        long delayedTime = System.currentTimeMillis() + delayInMillis;
        redisTemplate.opsForZSet().add(PENDING_SERVICE_QUEUE_PATTERN_NAME, entity, delayedTime);
    }

    public PendingServiceNotificationEntity dequeue() {
        long currentTime = System.currentTimeMillis();

        ZSetOperations.TypedTuple<PendingServiceNotificationEntity> task = redisTemplate.opsForZSet().popMin(PENDING_SERVICE_QUEUE_PATTERN_NAME);

        if (task != null && task.getScore() <= currentTime) {
            return task.getValue();
        }
        return null;
    }
}
