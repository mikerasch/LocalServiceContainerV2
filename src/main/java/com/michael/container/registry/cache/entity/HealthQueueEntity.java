package com.michael.container.registry.cache.entity;

import java.util.Set;

public class HealthQueueEntity {
  private Set<BaseInstance> baseInstanceList;

  public Set<BaseInstance> getBaseInstanceList() {
    return baseInstanceList;
  }

  public void setBaseInstanceList(Set<BaseInstance> baseInstanceList) {
    this.baseInstanceList = baseInstanceList;
  }
}
