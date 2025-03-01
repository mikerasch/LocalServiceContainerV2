package com.michael.container.health.client;

import com.michael.container.health.exception.HealthCheckInvalidException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;

@RestClientTest(HealthCheckClient.class)
class HealthCheckClientTest {
  @Autowired HealthCheckClient client;

  @Test
  void checkHealth_NotSuccess_Errors() {
    Assertions.assertThrows(
        HealthCheckInvalidException.class,
        () -> client.checkHealth("http://localhost:8080/health"));
  }
}
