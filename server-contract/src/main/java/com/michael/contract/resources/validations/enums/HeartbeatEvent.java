package com.michael.contract.resources.validations.enums;

public enum HeartbeatEvent {
  FOUND("Heartbeat Found"),
  RE_REGISTER("Heartbeat not found, registry required");

  private final String description;

  HeartbeatEvent(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
