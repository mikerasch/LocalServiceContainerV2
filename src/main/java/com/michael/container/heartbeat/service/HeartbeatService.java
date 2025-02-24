package com.michael.container.heartbeat.service;

import com.michael.container.heartbeat.enums.HeartbeatEvent;
import com.michael.container.heartbeat.model.HeartbeatRequest;
import com.michael.container.heartbeat.model.HeartbeatResponse;
import com.michael.container.registry.cache.crud.CrudRegistry;
import com.michael.container.registry.model.RegisterServiceResponse;
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
   * If the application is NOT found in the registry, it responds with RE_REGISTER.
   * Otherwise, it refreshes the TTL of the service and returns FOUND.
   *
   * @param heartbeatRequest a {@link HeartbeatRequest}
   * @return A {@link HeartbeatResponse} indicating whether the service needs to be re-registered or
   *         has been found in the registry.
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

    crudRegistry.insert(registerServiceResponse);

    return new HeartbeatResponse(HeartbeatEvent.FOUND, HeartbeatEvent.FOUND.getDescription());
  }
}
