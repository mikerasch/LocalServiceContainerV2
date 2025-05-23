package com.michael.container.heartbeat.service;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

import com.michael.container.registry.cache.crud.CrudRegistry;
import com.michael.contract.resources.validations.enums.HeartbeatEvent;
import com.michael.contract.resources.validations.enums.Status;
import com.michael.contract.resources.validations.requests.HeartbeatRequest;
import com.michael.contract.resources.validations.responses.HeartbeatResponse;
import com.michael.contract.resources.validations.responses.RegisterServiceResponse;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HeartbeatServiceTest {
  @InjectMocks HeartbeatService heartbeatService;

  @Mock CrudRegistry crudRegistry;

  @Test
  void heartbeat_serviceNotRegistered_ReturnReRegister() {
    Mockito.when(crudRegistry.findOne(anyString(), anyString(), anyInt(), anyInt()))
        .thenReturn(Optional.empty());

    HeartbeatResponse response =
        heartbeatService.heartbeat(new HeartbeatRequest("application-name", "test", 8080, 1));

    Assertions.assertEquals(HeartbeatEvent.RE_REGISTER, response.event());
  }

  @Test
  void heartbeat_serviceRegistered_ReturnFound() {
    Mockito.when(crudRegistry.findOne(anyString(), anyString(), anyInt(), anyInt()))
        .thenReturn(
            Optional.of(
                new RegisterServiceResponse(
                    "applicationName",
                    1,
                    "test",
                    8080,
                    Status.STARTING,
                    new HashSet<>(),
                    new HashMap<>())));

    HeartbeatResponse response =
        heartbeatService.heartbeat(new HeartbeatRequest("application-name", "test", 8080, 1));

    Assertions.assertEquals(HeartbeatEvent.FOUND, response.event());
  }
}
