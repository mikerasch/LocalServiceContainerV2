package com.michael.container.notifications.fsm.status;

import com.michael.container.notifications.enums.StatusStateEvent;
import com.michael.container.registry.model.StatusChangeEvent;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Component;

@Component
public class RestartState implements StatusChange {
  @Override
  public void triggerEvent(@Nonnull StatusChangeEvent statusChangeEvent) {
    // NO-OP currently, in the future, we might have something for RESTARTS.
    // For now, it will go to STARTING, which does not require notifications
  }

  @Override
  public StatusStateEvent getStatusStateEvent() {
    return StatusStateEvent.RESTART;
  }
}
