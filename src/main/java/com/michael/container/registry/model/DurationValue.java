package com.michael.container.registry.model;

import com.michael.container.registry.enums.Status;
import java.time.Instant;

public record DurationValue(Instant expiration) {

  public Status getStatus() {
    return Instant.now().isAfter(expiration) ? Status.EXPIRED : Status.UP;
  }
}
