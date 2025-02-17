package com.michael.container.registry.controller;

import com.michael.container.registry.model.RegisterServiceRequest;
import com.michael.container.registry.model.RegisterServiceResponse;
import com.michael.container.registry.model.RemoveServiceRequest;
import com.michael.container.registry.service.ServiceRegistryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/service-registry")
public class ServiceRegistryController {
  private final ServiceRegistryService registryService;

  public ServiceRegistryController(ServiceRegistryService registryService) {
    this.registryService = registryService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
      summary =
          "Register a service. If the service already exists matching down "
              + "to the unique identifier (application-name + url + port + version), it will update the TTL.")
  public void registerService(@RequestBody @Valid RegisterServiceRequest registerServiceRequest) {
    registryService.registerService(registerServiceRequest);
  }

  // TODO fetch all needs to know if expired or not.
  @GetMapping
  @Operation(
      summary = "Fetches all services, including: expired and not-expired registered services.")
  public Map<String, Set<RegisterServiceResponse>> retrieveAllServices() {
    return registryService.fetchAll();
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
      summary =
          "Deregister a service. Upon deregister, all dependent services will receive a DE_REGISTER event.")
  public void deregisterService(@RequestBody @Valid RemoveServiceRequest deregisterRequest) {
    registryService.removeService(deregisterRequest);
  }
}
