package com.michael.container.registry.cache.listener.status;

import com.michael.container.registry.enums.StatusStateEvent;
import com.michael.container.registry.model.StatusChangeEvent;

public interface StatusChange {
  void triggerEvent(StatusChangeEvent statusChangeEvent);

  StatusStateEvent getStatusStateEvent();
}
