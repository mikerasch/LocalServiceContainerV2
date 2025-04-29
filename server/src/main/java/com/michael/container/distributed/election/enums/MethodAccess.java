package com.michael.container.distributed.election.enums;

/**
 * Enum representing the different access levels for methods based on the role.
 * <ul>
 *   <li>{@link #LEADER_ONLY} - Allows access only for the LEADER role.</li>
 *   <li>{@link #FOLLOWER_ONLY} - Allows access only for the FOLLOWER role.</li>
 *   <li>{@link #UNRESTRICTED} - No restrictions on access, allows any role.</li>
 * </ul>
 */
public enum MethodAccess {
  LEADER_ONLY,
  FOLLOWER_ONLY,
  UNRESTRICTED;

  /**
   * Determines whether the given role is allowed to send the request based on the current method access restriction.
   *
   * @param role the {@link Role} to check against the method access restriction
   * @return {@code true} if the role is allowed to send, {@code false} otherwise
   */
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
