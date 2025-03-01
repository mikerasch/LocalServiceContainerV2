package com.michael.container.registry.enums;

import java.util.Arrays;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public enum Key {
  EXPIRED_INSTANCE_ENTITY("instanceEntity", "expired"),
  ADDED_HEALTH_QUEUE_ENTITY("healthQueueChannel", "healthAdded");

  final String body;
  final String eventType;

  Key(String body, String eventType) {
    this.body = body;
    this.eventType = eventType;
  }

  public static Optional<Key> from(String eventType, String body) {
    return Arrays.stream(Key.values())
        .filter(
            key ->
                StringUtils.equalsIgnoreCase(key.getBody(), body)
                    && StringUtils.equalsIgnoreCase(eventType, key.getEventType()))
        .findFirst();
  }

  public String getBody() {
    return body;
  }

  public String getEventType() {
    return eventType;
  }
}
