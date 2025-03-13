package com.michael.container.config;

import static com.michael.container.utils.ContainerConstants.HEALTH_QUEUE_PATTERN_NAME;

import com.michael.container.registry.cache.entity.HealthQueueEntity;
import com.michael.container.registry.cache.entity.PendingServiceNotificationEntity;
import com.michael.container.registry.cache.listener.key.KeyOrchestrator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfiguration {
  @Bean
  public RedisConnectionFactory redisConnectionFactory(
      @Value("${spring.redis.host}") String host, @Value("${spring.redis.port}") int port) {
    return new LettuceConnectionFactory(host, port);
  }

  @Bean
  @SuppressWarnings("java:S1452")
  public RedisTemplate<String, HealthQueueEntity> redisTemplateHealthQueue(RedisConnectionFactory redisConnectionFactory) {
    RedisTemplate<String, HealthQueueEntity> template = new RedisTemplate<>();
    template.setKeySerializer(new StringRedisSerializer());
    template.setConnectionFactory(redisConnectionFactory);
    Jackson2JsonRedisSerializer<HealthQueueEntity> healthQueueEntityJackson2JsonRedisSerializer =
        new Jackson2JsonRedisSerializer<>(HealthQueueEntity.class);
    template.setValueSerializer(healthQueueEntityJackson2JsonRedisSerializer);
    template.setHashValueSerializer(healthQueueEntityJackson2JsonRedisSerializer);
    return template;
  }

  @Bean
  public RedisTemplate<String, PendingServiceNotificationEntity> pendingServiceNotificationEntityRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
    RedisTemplate<String, PendingServiceNotificationEntity> template = new RedisTemplate<>();
    template.setKeySerializer(new StringRedisSerializer());
    template.setConnectionFactory(redisConnectionFactory);
    Jackson2JsonRedisSerializer<PendingServiceNotificationEntity> pendingServiceNotificationEntityJackson2JsonRedisSerializer =
            new Jackson2JsonRedisSerializer<>(PendingServiceNotificationEntity.class);
    template.setValueSerializer(pendingServiceNotificationEntityJackson2JsonRedisSerializer);
    template.setHashValueSerializer(pendingServiceNotificationEntityJackson2JsonRedisSerializer);
    return template;
  }

  @Bean
  public RedisMessageListenerContainer messageListenerContainer(
      RedisConnectionFactory redisConnectionFactory, KeyOrchestrator keyOrchestrator) {
    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(redisConnectionFactory);
    container.addMessageListener(keyOrchestrator, new ChannelTopic("__keyevent@0__:expired"));
    container.addMessageListener(keyOrchestrator, new PatternTopic(HEALTH_QUEUE_PATTERN_NAME));
    return container;
  }
}
