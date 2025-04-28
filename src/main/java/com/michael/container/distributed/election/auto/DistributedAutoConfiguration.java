package com.michael.container.distributed.election.auto;

import com.michael.container.distributed.election.config.EtcdConfiguration;
import com.michael.container.distributed.election.service.ElectionOrchestrator;
import com.michael.container.distributed.election.service.ElectionProcess;
import com.michael.container.distributed.election.service.EtcdElectionProcess;
import com.michael.container.distributed.election.service.LockProcess;
import com.michael.container.distributed.election.state.ElectionState;
import io.etcd.jetcd.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class DistributedAutoConfiguration {
  @Configuration
  public static class Config {
    @ConditionalOnMissingBean
    @Bean
    public EtcdConfiguration etcdConfiguration(
        @Value("${etcd.leader.key}") String etcLeaderKey, @Value("${etcd.urls}") String etcdUrls) {
      return new EtcdConfiguration(etcLeaderKey, etcdUrls);
    }
  }

  @Configuration
  public static class ServiceConfig {
    @ConditionalOnMissingBean
    @Bean
    public ElectionOrchestrator electionOrchestrator(ElectionProcess electionProcess) {
      return new ElectionOrchestrator(electionProcess);
    }

    @ConditionalOnMissingBean
    @Bean
    public ElectionProcess electionProcess(
        Client etcdClient,
        EtcdConfiguration etcdConfiguration,
        ApplicationEventPublisher eventPublisher,
        ElectionState electionState,
        LockProcess lockProcess) {
      return new EtcdElectionProcess(
          etcdClient, etcdConfiguration, eventPublisher, electionState, lockProcess);
    }

    @ConditionalOnMissingBean
    @Bean
    public LockProcess lockProcess(Client etcdClient) {
      return new LockProcess(etcdClient);
    }

    @ConditionalOnMissingBean
    @Bean
    public ElectionState electionState() {
      return new ElectionState();
    }
  }
}
