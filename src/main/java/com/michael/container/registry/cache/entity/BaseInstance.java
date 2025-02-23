package com.michael.container.registry.cache.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseInstance {
  private String applicationName;
  private int applicationVersion;
  private String url;
  private int port;

  public BaseInstance() {}

  public BaseInstance(String applicationName, int applicationVersion, String url, int port) {
    this.applicationName = applicationName;
    this.applicationVersion = applicationVersion;
    this.url = url;
    this.port = port;
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
}
