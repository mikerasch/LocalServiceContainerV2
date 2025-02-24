package com.michael.container.notifications.client;

import com.michael.container.notifications.exception.NotificationException;
import com.michael.container.notifications.model.ServiceNotificationRequest;
import jakarta.annotation.Nonnull;
import java.time.Duration;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NotificationClient {
  private final RestClient restClient;

  public NotificationClient(RestClient.Builder restClientBuilder) {
    var clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
    // In an ideal world, the read/connection should be very quick since this is for a notification.
    // If it takes longer than 2 seconds to read or to connect, something is probably wrong.
    clientHttpRequestFactory.setReadTimeout(Duration.ofSeconds(2));
    clientHttpRequestFactory.setConnectTimeout(Duration.ofSeconds(2));
    this.restClient = restClientBuilder.requestFactory(clientHttpRequestFactory).build();
  }

  /**
   * Sends a service notification to the specified URL.
   * A 2-second timeout is on both read/connect timeouts to stop long-winded calls from bogging down the system.
   */
  public void sendNotification(
      @Nonnull String url, @Nonnull ServiceNotificationRequest serviceNotificationRequest) {
    try {
      restClient
          .post()
          .uri(url)
          .contentType(MediaType.APPLICATION_JSON)
          .body(serviceNotificationRequest)
          .retrieve()
          .toBodilessEntity();
    } catch (Exception e) {
      throw new NotificationException(e.getMessage());
    }
  }
}
