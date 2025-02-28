package com.michael.container.config;

import com.michael.container.registry.cache.entity.HealthQueueEntity;
import com.michael.container.registry.cache.listener.key.KeyOrchestrator;
import com.michael.container.registry.enums.Key;
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
  @SuppressWarnings("java:S1452") // Redis handles serialization
  public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
    RedisTemplate<?, ?> template = new RedisTemplate<>();
    template.setKeySerializer(new StringRedisSerializer());
    template.setConnectionFactory(redisConnectionFactory);
    Jackson2JsonRedisSerializer<HealthQueueEntity> serializer =
        new Jackson2JsonRedisSerializer<>(HealthQueueEntity.class);
    template.setValueSerializer(serializer);
    template.setHashValueSerializer(serializer);
    return template;
  }

  @Bean
  public RedisMessageListenerContainer messageListenerContainer(
      RedisConnectionFactory redisConnectionFactory, KeyOrchestrator keyOrchestrator) {
    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(redisConnectionFactory);
    container.addMessageListener(keyOrchestrator, new ChannelTopic("__keyevent@0__:expired"));
    container.addMessageListener(
        keyOrchestrator, new PatternTopic(Key.HEALTH_QUEUE_ENTITY.getName()));
    return container;
  }
}
