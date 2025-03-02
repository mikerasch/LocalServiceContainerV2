package com.michael.container.registry.cache.entity;

import java.util.Objects;
import java.util.Set;

public class HealthQueueEntity {
  private Set<BaseInstance> baseInstanceList;

  public Set<BaseInstance> getBaseInstanceList() {
    return baseInstanceList;
  }

  public void setBaseInstanceList(Set<BaseInstance> baseInstanceList) {
    this.baseInstanceList = baseInstanceList;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    HealthQueueEntity that = (HealthQueueEntity) o;
    return Objects.equals(baseInstanceList, that.baseInstanceList);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(baseInstanceList);
  }
}
