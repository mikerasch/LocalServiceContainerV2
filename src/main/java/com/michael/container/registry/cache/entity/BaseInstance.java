package com.michael.container.registry.cache.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Objects;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BaseInstance that = (BaseInstance) o;
    return applicationVersion == that.applicationVersion
        && port == that.port
        && Objects.equals(applicationName, that.applicationName)
        && Objects.equals(url, that.url);
  }

  @Override
  public int hashCode() {
    return Objects.hash(applicationName, applicationVersion, url, port);
  }

  @Override
  public String toString() {
    return "BaseInstance{"
        + "applicationName='"
        + applicationName
        + '\''
        + ", applicationVersion="
        + applicationVersion
        + ", url='"
        + url
        + '\''
        + ", port="
        + port
        + '}';
  }
}
