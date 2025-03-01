package com.michael.container.notifications.enums;

import com.michael.container.registry.enums.Status;
import jakarta.annotation.Nonnull;
import java.util.Optional;

public enum StatusStateEvent {
  BEGIN, // STARTING -> HEALTHY
  FAIL, // Any state -> DOWN
  FIX, // From DOWN -> UNDER_MAINTENANCE
  MAINTENANCE, // from any status to UNDER_MAINTENANCE
  RESTART // From UNDER_MAINTENANCE to STARTING
;

  public static Optional<StatusStateEvent> from(
      @Nonnull Status previousStatus, @Nonnull Status newStatus) {
    switch (newStatus) {
      case STARTING:
        if (previousStatus == Status.UNDER_MAINTENANCE) {
          return Optional.of(RESTART);
        }
        break;
      case HEALTHY:
        if (previousStatus == Status.STARTING) {
          return Optional.of(StatusStateEvent.BEGIN);
        } else if (previousStatus == Status.DOWN || previousStatus == Status.UNDER_MAINTENANCE) {
          return Optional.of(FIX);
        }
        break;
      case DOWN:
        return Optional.of(FAIL);
      case UNDER_MAINTENANCE:
        return Optional.of(MAINTENANCE);
      default:
        break;
    }
    return Optional.empty();
  }
}
