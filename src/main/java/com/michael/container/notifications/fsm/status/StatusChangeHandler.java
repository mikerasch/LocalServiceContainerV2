package com.michael.container.notifications.fsm.status;

import com.michael.container.notifications.enums.StatusStateEvent;
import com.michael.container.registry.model.StatusChangeEvent;

public interface StatusChangeHandler {
  void triggerEvent(StatusChangeEvent statusChangeEvent);

  StatusStateEvent getStatusStateEvent();
}
