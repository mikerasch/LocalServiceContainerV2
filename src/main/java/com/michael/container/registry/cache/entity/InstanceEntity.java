package com.michael.container.registry.cache.entity;

import com.michael.container.registry.enums.Tag;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@RedisHash("instanceEntity")
public class InstanceEntity {

  // Composite key field (derived from applicationName + applicationVersion + url + port)
  @Id private String compositeKey;

  private String applicationName;
  private int applicationVersion;
  private String url;
  private int port;
  private Set<String> dependsOn;
  private Map<Tag, String> metaData;
  @TimeToLive private Long timeToLive;

  public InstanceEntity() {}

  public InstanceEntity(String applicationName, int applicationVersion, String url, int port) {
    this.compositeKey = formCompositeKey(applicationName, applicationVersion, url, port);
    this.applicationName = applicationName;
    this.applicationVersion = applicationVersion;
    this.url = url;
    this.port = port;
    refreshTTL();
  }

  public void refreshTTL() {
    timeToLive = 5L;
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

  public String getApplicationName() {
    return applicationName;
  }

  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }

  public int getApplicationVersion() {
    return applicationVersion;
  }

  public void setApplicationVersion(int applicationVersion) {
    this.applicationVersion = applicationVersion;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
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
}
