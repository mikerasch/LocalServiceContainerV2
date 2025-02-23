package com.michael.container.registry.cache.entity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("applicationEntity")
public class ApplicationEntity {
  @Id private String applicationName;

  @Reference private Set<InstanceEntity> instanceEntities = new HashSet<>();

  public String getApplicationName() {
    return applicationName;
  }

  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }

  public Set<InstanceEntity> getInstanceEntities() {
    return instanceEntities;
  }

  public void setInstanceEntities(Set<InstanceEntity> instanceEntities) {
    this.instanceEntities = instanceEntities;
  }

  public void addAllInstanceEntities(InstanceEntity... entity) {
    this.instanceEntities.addAll(List.of(entity));
  }
}
