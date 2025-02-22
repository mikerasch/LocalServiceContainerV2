package com.michael.container.config;

import com.michael.container.distributed.election.config.EtcdConfiguration;
import com.michael.container.registry.cache.listener.KeyOrchestrator;
import io.etcd.jetcd.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

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
    template.setConnectionFactory(connectionFactory);

    return template;
  }

  @Bean
  public RedisMessageListenerContainer messageListenerContainer(
      RedisConnectionFactory redisConnectionFactory, KeyOrchestrator keyOrchestrator) {
    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(redisConnectionFactory);
    container.addMessageListener(keyOrchestrator, new ChannelTopic("__keyevent@0__:expired"));
    return container;
  }
}
