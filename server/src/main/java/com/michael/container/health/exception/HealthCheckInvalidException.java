package com.michael.container.health.exception;

public class HealthCheckInvalidException extends RuntimeException {
  public HealthCheckInvalidException(String message) {
    super(message);
  }
}
