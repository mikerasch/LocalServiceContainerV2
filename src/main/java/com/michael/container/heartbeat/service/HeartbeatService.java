package com.michael.container.heartbeat.service;

import com.michael.container.heartbeat.enums.HeartbeatEvent;
import com.michael.container.heartbeat.model.HeartbeatRequest;
import com.michael.container.heartbeat.model.HeartbeatResponse;
import com.michael.container.registry.cache.crud.CrudRegistry;
import com.michael.container.registry.model.RegisterServiceResponse;
import org.springframework.stereotype.Service;

@Service
public class HeartbeatService {
  private final CrudRegistry crudRegistry;

  public HeartbeatService(CrudRegistry crudRegistry) {
    this.crudRegistry = crudRegistry;
  }

  public HeartbeatResponse heartbeat(HeartbeatRequest heartbeatRequest) {
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
