package com.michael.container.config;

import com.michael.container.distributed.election.config.EtcdConfiguration;
import io.etcd.jetcd.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EtcdAppConfiguration {
  @Bean
  public Client etcdClient(
      EtcdConfiguration etcdConfiguration) {
    return Client.builder().endpoints(etcdConfiguration.getEtcdEndpoints()).build();
  }
}
