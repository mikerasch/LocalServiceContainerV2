package com.michael.container.notifications.client;

import com.michael.container.notifications.exception.NotificationException;
import com.michael.container.notifications.model.ServiceNotificationRequest;
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
    clientHttpRequestFactory.setReadTimeout(Duration.ofSeconds(2));
    clientHttpRequestFactory.setConnectTimeout(Duration.ofSeconds(2));
    this.restClient = restClientBuilder.requestFactory(clientHttpRequestFactory).build();
  }

  public void sendNotification(String url, ServiceNotificationRequest serviceNotificationRequest) {
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
