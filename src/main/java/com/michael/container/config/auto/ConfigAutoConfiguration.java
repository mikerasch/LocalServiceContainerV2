package com.michael.container.config.auto;

import com.michael.container.config.AppConfig;
import com.michael.container.config.EtcdAppConfiguration;
import com.michael.container.config.RedisConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ComponentScan("com.michael.container.config.auto.ConfigAutoConfiguration")
public class ConfigAutoConfiguration {
  @Configuration
  public static class Config {
    @Bean
    @ConditionalOnMissingBean
    public AppConfig appConfig() {
      return new AppConfig();
    }

    @Bean
    @ConditionalOnMissingBean
    public EtcdAppConfiguration etcdAppConfiguration() {
      return new EtcdAppConfiguration();
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisConfiguration redisConfiguration() {
      return new RedisConfiguration();
    }
  }
}
