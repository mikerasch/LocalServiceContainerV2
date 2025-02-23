package com.michael.container.registry.service;

import com.michael.container.registry.cache.crud.CrudRegistry;
import com.michael.container.registry.model.RegisterServiceRequest;
import com.michael.container.registry.model.RegisterServiceResponse;
import com.michael.container.registry.model.RemoveServiceRequest;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

@Service
public class ServiceRegistryService {
  private final ConversionService conversionService;
  private final CrudRegistry crudRegistry;

  public ServiceRegistryService(ConversionService conversionService, CrudRegistry crudRegistry) {
    this.conversionService = conversionService;
    this.crudRegistry = crudRegistry;
  }

  public void registerService(RegisterServiceRequest registerServiceRequest) {
    crudRegistry.insert(
        Objects.requireNonNull(
            conversionService.convert(registerServiceRequest, RegisterServiceResponse.class)));
  }

  public Map<String, Set<RegisterServiceResponse>> fetchAll() {
    Map<String, Set<RegisterServiceResponse>> map = new ConcurrentHashMap<>();

    crudRegistry.fetchAll().forEach((key, value) -> map.put(key, new HashSet<>(value)));

    return map;
  }

  public void removeService(RemoveServiceRequest removeServiceRequest) {
    crudRegistry.remove(
        removeServiceRequest.applicationName(),
        removeServiceRequest.url(),
        removeServiceRequest.version(),
        removeServiceRequest.port());
  }
}
