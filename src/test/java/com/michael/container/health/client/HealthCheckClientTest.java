package com.michael.container.health.client;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;

import com.michael.container.health.exception.HealthCheckInvalidException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.test.web.client.MockRestServiceServer;

@RestClientTest(HealthCheckClient.class)
class HealthCheckClientTest {
  @Autowired HealthCheckClient client;

  @Autowired MockRestServiceServer server;

  // TODO I DON'T THINK THIS IS ACTUALLY RESPONDING TO THE MOCK SERVER...
  @Test
  void checkHealth_NotSuccess_Errors() {
    server.expect(requestTo("http://localhost:8080/health")).andRespond(withBadRequest());

    Assertions.assertThrows(
        HealthCheckInvalidException.class,
        () -> client.checkHealth("http://localhost:8080/health"));
  }
}
