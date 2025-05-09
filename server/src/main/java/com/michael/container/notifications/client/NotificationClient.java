package com.michael.container.notifications.client;

import com.michael.container.notifications.exception.NotificationException;
import com.michael.container.notifications.model.ServiceNotificationRequest;
import com.michael.spring.utils.logger.annotations.ExecutionTime;
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
   * Sends a service notification to the specified URL with a 2-second timeout for both read and connect operations.
   * <p>
   * This method ensures that long-running calls do not block the system by applying timeouts to both
   * the connection and read phases. If an exception occurs during the notification process, a {@link NotificationException}
   * is thrown.
   * </p>
   *
   * @param url the URL to which the service notification should be sent
   * @param serviceNotificationRequest the {@link ServiceNotificationRequest} containing the details of the notification
   */
  @ExecutionTime
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
