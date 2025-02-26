package com.michael.container.registry.enums;

import java.util.Set;

public enum Status {
  STARTING,
  HEALTHY,
  DOWN,
  UNDER_MAINTENANCE;

  public static final Set<Status> HEARTBEAT_STATUS_TO_HEALTHY_TRANSITIONS = Set.of(STARTING, DOWN);
  public static final Set<Status> STATUSES_TO_SKIP_HEARTBEAT = Set.of(DOWN, UNDER_MAINTENANCE);
}
