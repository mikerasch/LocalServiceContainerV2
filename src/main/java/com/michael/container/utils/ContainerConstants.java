package com.michael.container.utils;

public class ContainerConstants {
  private ContainerConstants() {}

  public static final String HEALTH_QUEUE_PATTERN_NAME = "__keyevent@?__:healthAdded";
  public static final String PENDING_SERVICE_QUEUE_PATTERN_NAME = "__keyevent@?__pendingServiceAdded";
  public static final Long INSTANCE_ENTITY_MAINTENANCE_TIME_TO_LIVE = 5400L;
  public static final Long INSTANCE_ENTITY_DEFAULT_TIME_TO_LIVE = 20L;
}
