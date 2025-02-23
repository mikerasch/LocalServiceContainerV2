package com.michael.container.config;

import com.michael.container.distributed.election.config.EtcdConfiguration;
import io.etcd.jetcd.Client;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class AppConfig {

  @Bean
  public Client etcdClient(EtcdConfiguration etcdConfiguration) {
    return Client.builder().endpoints(etcdConfiguration.getEtcdEndpoints()).build();
  }

  @Bean("healthCheckExecutorService")
  public ExecutorService healthCheckExecutorService() {
    return Executors.newFixedThreadPool(10);
  }
}
