package com.michael.container.registry.controller;

import com.michael.container.registry.model.RegisterServiceRequest;
import com.michael.container.registry.model.RegisterServiceResponse;
import com.michael.container.registry.model.RemoveServiceRequest;
import com.michael.container.registry.model.UpdateStatusRequest;
import com.michael.container.registry.service.ServiceRegistryService;
import com.michael.spring.utils.logger.annotations.ExecutionTime;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary =
          "Register a service. If the service already exists matching down "
              + "to the unique identifier (application-name + url + port + version), it will update the TTL.")
  @ExecutionTime
  public void registerService(@RequestBody @Valid RegisterServiceRequest registerServiceRequest) {
    registryService.registerService(registerServiceRequest);
  }

  @GetMapping
  @Operation(
      summary = "Fetches all services, including: expired and not-expired registered services.")
  @ExecutionTime
  public Map<String, Set<RegisterServiceResponse>> retrieveAllServices() {
    return registryService.fetchAll();
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
      summary =
          "Deregister a service. Upon deregister, all dependent services will receive a DE_REGISTER event.")
  @ExecutionTime
  public void deregisterService(@RequestBody @Valid RemoveServiceRequest deregisterRequest) {
    registryService.removeService(deregisterRequest);
  }

  @PatchMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
      summary =
          "Updates the status of a service. All dependent services will become aware of this transition.")
  @ExecutionTime
  public void updateStatusOnService(@RequestBody @Valid UpdateStatusRequest updateStatusRequest) {
    registryService.updateStatusOnService(updateStatusRequest, true);
  }
}
