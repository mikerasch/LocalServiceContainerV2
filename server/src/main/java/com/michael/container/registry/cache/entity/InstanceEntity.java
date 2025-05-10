package com.michael.container.registry.cache.entity;

import com.michael.container.utils.ContainerConstants;
import com.michael.contract.resources.validations.enums.Status;
import com.michael.contract.resources.validations.enums.Tag;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@RedisHash("instanceEntity")
public class InstanceEntity extends BaseInstance {

  // Composite key field (derived from applicationName + applicationVersion + url + port)
  @Id private String compositeKey;
  private String status;
  private Set<String> dependsOn;
  private Map<Tag, String> metaData;
  @TimeToLive private Long timeToLive;

  public InstanceEntity() {}

  public InstanceEntity(String applicationName, int applicationVersion, String url, int port) {
    super(applicationName, applicationVersion, url, port);
    this.compositeKey = formCompositeKey(applicationName, applicationVersion, url, port);
    refreshTTL();
  }

  public Status getStatus() {
    if (status == null) {
      return null;
    }
    return Status.valueOf(status);
  }

  public void setStatus(Status status) {
    this.status = status.name();
  }

  public void refreshTTL() {
    timeToLive = ContainerConstants.INSTANCE_ENTITY_DEFAULT_TIME_TO_LIVE;
  }

  public static String formCompositeKey(
      String applicationName, int applicationVersion, String url, int port) {
    return applicationName + ":" + applicationVersion + ":" + url + ":" + port;
  }

  public String getCompositeKey() {
    return compositeKey;
  }

  public void setCompositeKey(String compositeKey) {
    this.compositeKey = compositeKey;
  }

  public Set<String> getDependsOn() {
    return dependsOn == null ? new HashSet<>() : dependsOn;
  }

  public void setDependsOn(Set<String> dependsOn) {
    this.dependsOn = dependsOn;
  }

  public Map<Tag, String> getMetaData() {
    return metaData == null ? new HashMap<>() : metaData;
  }

  public void setMetaData(Map<Tag, String> metaData) {
    this.metaData = metaData;
  }

  public void setTimeToLive(Long timeToLive) {
    this.timeToLive = timeToLive;
  }
}
