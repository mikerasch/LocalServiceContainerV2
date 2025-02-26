package com.michael.container.registry.cache.listener.status;

import com.michael.container.registry.enums.Status;
import com.michael.container.registry.enums.StatusStateEvent;
import com.michael.container.registry.model.StatusChangeEvent;
import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class StatusChangeOrchestrator {
  private static final Logger log = LoggerFactory.getLogger(StatusChangeOrchestrator.class);
  private final Map<StatusStateEvent, StatusChange> statusChangeMap;

  public StatusChangeOrchestrator(List<StatusChange> statusChanges) {
    this.statusChangeMap =
        statusChanges.stream()
            .collect(Collectors.toMap(StatusChange::getStatusStateEvent, Function.identity()));
  }

  @EventListener(StatusChangeEvent.class)
  public void onStatusChange(@Nonnull StatusChangeEvent statusChangeEvent) {
    Status previousStatus = statusChangeEvent.previousStatus();
    Status newStatus = statusChangeEvent.newStatus();

    // Previous state of null indicates the beginning of the state machine.
    // Nothing should happen as it has not switched to HEALTHY yet.
    if (previousStatus == null || (previousStatus == newStatus)) {
      return;
    }

    StatusStateEvent event = StatusStateEvent.from(previousStatus, newStatus).orElse(null);

    if (event == null) {
      log.warn(
          "A status change event has occurred. However, a valid state transition could not be identified. Received: previous {}, new {} status",
          previousStatus,
          newStatus);
    }

    triggerEvent(event, statusChangeEvent);
  }

  private void triggerEvent(StatusStateEvent event, StatusChangeEvent statusChangeEvent) {
    statusChangeMap.get(event).triggerEvent(statusChangeEvent);
  }
}
