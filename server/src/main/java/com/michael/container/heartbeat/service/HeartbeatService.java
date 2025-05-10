package com.michael.container.heartbeat.service;

import com.michael.container.registry.cache.crud.CrudRegistry;
import com.michael.contract.resources.validations.enums.HeartbeatEvent;
import com.michael.contract.resources.validations.enums.Status;
import com.michael.contract.resources.validations.requests.HeartbeatRequest;
import com.michael.contract.resources.validations.responses.HeartbeatResponse;
import com.michael.contract.resources.validations.responses.RegisterServiceResponse;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Service;

@Service
public class HeartbeatService {
  private final CrudRegistry crudRegistry;

  public HeartbeatService(CrudRegistry crudRegistry) {
    this.crudRegistry = crudRegistry;
  }

  /**
   * Processes a heartbeat request from a service and responds with the appropriate status.
   * <p>
   * If the application is not found in the registry, this method responds with {@link HeartbeatEvent#RE_REGISTER}.
   * If the application is found, it refreshes the Time-to-Live (TTL) of the service in the registry and returns
   * {@link HeartbeatEvent#FOUND}. Additionally, the service is transitioned from either the {@link com.michael.container.registry.enums.Status#STARTING}
   * or {@link Status#DOWN} state to {@link Status#HEALTHY}.
   * </p>
   *
   * @param heartbeatRequest the {@link HeartbeatRequest} containing details about the service
   * @return a {@link HeartbeatResponse} indicating whether the service needs to be re-registered or has been found in the registry
   */
  public HeartbeatResponse heartbeat(@Nonnull HeartbeatRequest heartbeatRequest) {
    RegisterServiceResponse registerServiceResponse =
        crudRegistry
            .findOne(
                heartbeatRequest.applicationName(),
                heartbeatRequest.url(),
                heartbeatRequest.port(),
                heartbeatRequest.applicationVersion())
            .orElse(null);

    if (registerServiceResponse == null) {
      return new HeartbeatResponse(
          HeartbeatEvent.RE_REGISTER, HeartbeatEvent.RE_REGISTER.getDescription());
    }

    crudRegistry.updateTTL(
        heartbeatRequest.applicationName(),
        heartbeatRequest.url(),
        heartbeatRequest.applicationVersion(),
        heartbeatRequest.port());

    return new HeartbeatResponse(HeartbeatEvent.FOUND, HeartbeatEvent.FOUND.getDescription());
  }
}
