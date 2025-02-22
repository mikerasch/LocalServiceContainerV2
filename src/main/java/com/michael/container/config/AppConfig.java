package com.michael.container.config;

import com.michael.container.distributed.election.config.EtcdConfiguration;
import com.michael.container.registry.cache.entity.ApplicationEntity;
import com.michael.container.registry.cache.enums.Key;
import com.michael.container.registry.cache.listener.KeyOrchestrator;
import io.etcd.jetcd.Client;
import jakarta.validation.constraints.Pattern;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@EnableScheduling
public class AppConfig {

  @Bean
  public Client etcdClient(EtcdConfiguration etcdConfiguration) {
    return Client.builder().endpoints(etcdConfiguration.getEtcdEndpoints()).build();
  }

  @Bean
  @SuppressWarnings("java:S1452") // Redis handles serialization
  public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<?, ?> template = new RedisTemplate<>();
    template.setKeySerializer(new StringRedisSerializer());
    template.setConnectionFactory(connectionFactory);
    Jackson2JsonRedisSerializer<ApplicationEntity> serializer = new Jackson2JsonRedisSerializer<>(ApplicationEntity.class);
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
    container.addMessageListener(keyOrchestrator, new PatternTopic(Key.HEALTH_QUEUE_ENTITY.getName()));
    return container;
  }

  @Bean("healthCheckExecutorService")
  public ExecutorService healthCheckExecutorService() {
    return Executors.newFixedThreadPool(10);
  }
}
