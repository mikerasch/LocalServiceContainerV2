package com.michael.container.heartbeat.controller;

import com.michael.container.heartbeat.service.HeartbeatService;
import com.michael.contract.resources.validations.requests.HeartbeatRequest;
import com.michael.contract.resources.validations.responses.HeartbeatResponse;
import com.michael.spring.utils.logger.annotations.ExecutionTime;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/heartbeat")
public class HeartbeatController {
  private final HeartbeatService heartbeatService;

  public HeartbeatController(HeartbeatService heartbeatService) {
    this.heartbeatService = heartbeatService;
  }

  @PostMapping
  @Operation(
      summary =
          "Used for clients to perform heartbeats. This will re-register the TTL of the record."
              + " If TTL has expired, or no record found, "
              + "it will return RE_REGISTER, in which the client is expected to re-register.")
  @ExecutionTime
  public HeartbeatResponse heartbeat(@RequestBody @Valid HeartbeatRequest heartbeatRequest) {
    return heartbeatService.heartbeat(heartbeatRequest);
  }
}
