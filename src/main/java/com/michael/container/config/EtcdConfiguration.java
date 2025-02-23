package com.michael.container.config;

import io.etcd.jetcd.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EtcdConfiguration {
  @Bean
  public Client etcdClient(
      com.michael.container.distributed.election.config.EtcdConfiguration etcdConfiguration) {
    return Client.builder().endpoints(etcdConfiguration.getEtcdEndpoints()).build();
  }
}
