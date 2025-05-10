package com.michael.container.distributed.election.config;

import com.michael.container.exceptions.UncheckedURISyntaxException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EtcdConfiguration {
  private final String etcdLeaderKey;
  private final String baseUrl;
  private final URI[] etcdEndpoints;

  public EtcdConfiguration(
      @Value("${etcd.leader.key}") String etcLeaderKey,
      @Value("${app.base.url}") String baseUrl,
      @Value("${etcd.urls}") String etcdUrls) {
    this.etcdLeaderKey = etcLeaderKey;
    this.baseUrl = baseUrl;
    etcdEndpoints =
        Arrays.stream(etcdUrls.split(","))
            .map(
                url -> {
                  try {
                    return new URI(url);
                  } catch (URISyntaxException e) {
                    throw new UncheckedURISyntaxException(e);
                  }
                })
            .toList()
            .toArray(new URI[0]);
  }

  public String getEtcdLeaderKey() {
    return etcdLeaderKey;
  }

  public URI[] getEtcdEndpoints() {
    return etcdEndpoints;
  }

  public String getBaseUrl() {
    return baseUrl;
  }
}
