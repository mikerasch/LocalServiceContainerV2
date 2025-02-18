package com.michael.container.config;

import com.michael.container.distributed.election.config.EtcdConfiguration;
import io.etcd.jetcd.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class AppConfig {

  @SuppressWarnings("java:S112") // Allow runtime exception since it is appropriate
  @Bean
  public Client etcdClient(EtcdConfiguration etcdConfiguration) {
    return Client.builder().endpoints(etcdConfiguration.getEtcdEndpoints()).build();
  }
}
