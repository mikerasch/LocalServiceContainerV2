package com.michael.container.registry.cache.entity;

import java.util.Objects;

public class PendingServiceNotificationEntity extends BaseInstance {
  private String dependencyApplicationName;

  public String getDependencyApplicationName() {
    return dependencyApplicationName;
  }

  public void setDependencyApplicationName(String dependencyApplicationName) {
    this.dependencyApplicationName = dependencyApplicationName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    PendingServiceNotificationEntity that = (PendingServiceNotificationEntity) o;
    return Objects.equals(dependencyApplicationName, that.dependencyApplicationName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), dependencyApplicationName);
  }
}
