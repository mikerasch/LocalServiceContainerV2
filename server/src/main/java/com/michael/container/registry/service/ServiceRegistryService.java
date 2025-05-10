package com.michael.container.registry.service;

import com.michael.container.registry.cache.crud.CrudRegistry;
import com.michael.contract.resources.validations.requests.RegisterServiceRequest;
import com.michael.contract.resources.validations.requests.RemoveServiceRequest;
import com.michael.contract.resources.validations.requests.UpdateStatusRequest;
import com.michael.contract.resources.validations.responses.RegisterServiceResponse;
import jakarta.annotation.Nonnull;
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

  public void registerService(@Nonnull RegisterServiceRequest registerServiceRequest) {
    crudRegistry.insert(
        Objects.requireNonNull(
            conversionService.convert(registerServiceRequest, RegisterServiceResponse.class)));
  }

  public Map<String, Set<RegisterServiceResponse>> fetchAll() {
    Map<String, Set<RegisterServiceResponse>> map = new ConcurrentHashMap<>();

    crudRegistry.fetchAll().forEach((key, value) -> map.put(key, new HashSet<>(value)));

    return map;
  }

  public void removeService(@Nonnull RemoveServiceRequest removeServiceRequest) {
    crudRegistry.remove(
        removeServiceRequest.applicationName(),
        removeServiceRequest.url(),
        removeServiceRequest.version(),
        removeServiceRequest.port());
  }

  public void updateStatusOnService(
      @Nonnull UpdateStatusRequest updateStatusRequest, boolean shouldFollowStateMachine) {
    crudRegistry.updateStatusOnService(
        updateStatusRequest.applicationName(),
        updateStatusRequest.url(),
        updateStatusRequest.applicationVersion(),
        updateStatusRequest.port(),
        updateStatusRequest.status(),
        shouldFollowStateMachine);
  }
}
