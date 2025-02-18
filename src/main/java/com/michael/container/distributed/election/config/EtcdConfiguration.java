package com.michael.container.distributed.election.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EtcdConfiguration {
  private final String etcdLeaderKey;
  private final UUID serviceUniqueIdentifier;
  private final URI[] etcdEndpoints;

  public EtcdConfiguration(
      @Value("${etcd.leader.key}") String etcLeaderKey, @Value("${etcd.urls}") String etcdUrls) {
    this.etcdLeaderKey = etcLeaderKey;
    serviceUniqueIdentifier = UUID.randomUUID();
    etcdEndpoints =
        Arrays.stream(etcdUrls.split(","))
            .map(
                url -> {
                  try {
                    return new URI(url);
                  } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                  }
                })
            .toList()
            .toArray(new URI[0]);
  }

  public String getEtcdLeaderKey() {
    return etcdLeaderKey;
  }

  public UUID getServiceUniqueIdentifier() {
    return serviceUniqueIdentifier;
  }

  public URI[] getEtcdEndpoints() {
    return etcdEndpoints;
  }
}
