package com.michael.container.registry.cache.listener.status;

import com.michael.container.registry.enums.StatusStateEvent;
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
