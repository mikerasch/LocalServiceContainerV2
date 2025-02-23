package com.michael.container.registry.cache.enums;

import java.util.Arrays;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public enum Key {
  INSTANCE_ENTITY("instanceEntity"),
  HEALTH_QUEUE_ENTITY("healthQueueChannel");

  final String name;

  Key(String name) {
    this.name = name;
  }

  public static Optional<Key> from(String from) {
    return Arrays.stream(Key.values())
        .filter(key -> StringUtils.equalsIgnoreCase(key.getName(), from))
        .findFirst();
  }

  public String getName() {
    return name;
  }
}
