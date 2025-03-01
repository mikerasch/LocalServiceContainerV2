package com.michael.container.distributed.election.enums;

public enum MethodAccess {
  LEADER_ONLY,
  FOLLOWER_ONLY,
  UNRESTRICTED;

  public boolean canSend(Role role) {
    switch (this) {
      case LEADER_ONLY -> {
        return role == Role.LEADER;
      }
      case FOLLOWER_ONLY -> {
        return role == Role.FOLLOWER;
      }
      case UNRESTRICTED -> {
        return true;
      }
      default -> {
        return false;
      }
    }
  }
}
