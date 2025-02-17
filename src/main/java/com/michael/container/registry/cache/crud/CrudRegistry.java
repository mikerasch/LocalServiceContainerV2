package com.michael.container.registry.cache.crud;

import com.michael.container.registry.cache.RegistryCache;
import com.michael.container.registry.model.DeregisterEvent;
import com.michael.container.registry.model.DurationValue;
import com.michael.container.registry.model.RegisterEvent;
import com.michael.container.registry.model.RegisterServiceResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class CrudRegistry {
  private final RegistryCache registryCache;
  private final ApplicationEventPublisher eventPublisher;

  public CrudRegistry(RegistryCache registryCache, ApplicationEventPublisher eventPublisher) {
    this.registryCache = registryCache;
    this.eventPublisher = eventPublisher;
  }

  public void insert(RegisterServiceResponse registerServiceResponse) {
    String applicationName = registerServiceResponse.applicationName();
    getCache()
        .computeIfPresent(
            applicationName,
            (key, existingServices) -> {
              existingServices.put(
                  registerServiceResponse,
                  new DurationValue(RegistryCache.generateNewExpiration()));
              return existingServices;
            });
    getCache()
        .putIfAbsent(
            applicationName,
            new HashMap<>(
                Map.of(
                    registerServiceResponse,
                    new DurationValue(RegistryCache.generateNewExpiration()))));
    eventPublisher.publishEvent(
        new RegisterEvent(
            registerServiceResponse.applicationName(),
            registerServiceResponse.url(),
            registerServiceResponse.applicationVersion(),
            registerServiceResponse.port()));
  }

  public Map<String, Map<RegisterServiceResponse, DurationValue>> fetchAll() {
    return new HashMap<>(registryCache.getApplicationToRegisterServiceMap());
  }

  public Optional<RegisterServiceResponse> findOne(
      String applicationName, String url, int port, int version) {
    return getCache().getOrDefault(applicationName, new HashMap<>()).keySet().stream()
        .filter(
            serviceResponse ->
                serviceResponse.url().equals(url)
                    && serviceResponse.port() == port
                    && serviceResponse.applicationVersion() == version)
        .findFirst();
  }

  public void remove(String applicationName, String url, int applicationVersion, int port) {
    getCache()
        .getOrDefault(applicationName, new ConcurrentHashMap<>())
        .entrySet()
        .removeIf(
            entry ->
                entry.getKey().url().equals(url)
                    && entry.getKey().port() == port
                    && entry.getKey().applicationVersion() == applicationVersion);
    if (CollectionUtils.isEmpty(getCache().get(applicationName))) {
      getCache().remove(applicationName);
    }
    eventPublisher.publishEvent(
        new DeregisterEvent(applicationName, url, applicationVersion, port));
  }

  private Map<String, Map<RegisterServiceResponse, DurationValue>> getCache() {
    return registryCache.getApplicationToRegisterServiceMap();
  }
}
