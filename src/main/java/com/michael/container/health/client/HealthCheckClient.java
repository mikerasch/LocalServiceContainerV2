package com.michael.container.health.client;

import com.michael.container.health.exception.HealthCheckInvalidException;
import jakarta.annotation.Nonnull;
import java.time.Duration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class HealthCheckClient {
  private final RestClient restClient;

  public HealthCheckClient(RestClient.Builder restClientBuilder) {
    var clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
    clientHttpRequestFactory.setReadTimeout(Duration.ofSeconds(2));
    clientHttpRequestFactory.setConnectTimeout(Duration.ofSeconds(2));
    this.restClient = restClientBuilder.requestFactory(clientHttpRequestFactory).build();
  }

  /**
   * Sends a GET request to the health check endpoint.
   * Any exception will result in a {@link HealthCheckInvalidException}
   */
  public void checkHealth(@Nonnull String url) {
    try {
      restClient.get().uri(url).retrieve().toBodilessEntity();
    } catch (Exception e) {
      throw new HealthCheckInvalidException(e.getMessage());
    }
  }
}
