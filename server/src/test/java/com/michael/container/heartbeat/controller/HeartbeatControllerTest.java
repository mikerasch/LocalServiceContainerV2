package com.michael.container.heartbeat.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.michael.container.heartbeat.service.HeartbeatService;
import com.michael.contract.resources.validations.requests.HeartbeatRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class HeartbeatControllerTest {
  @Mock HeartbeatService heartbeatService;

  @InjectMocks HeartbeatController heartbeatController;

  MockMvc mockMvc;
  ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setup() {
    mockMvc = MockMvcBuilders.standaloneSetup(heartbeatController).build();
  }

  @Test
  void heartbeat_Success() throws Exception {
    HeartbeatRequest request =
        new HeartbeatRequest("application-name", "http://somehostName.com", 8080, 1);

    mockMvc
        .perform(
            post("/heartbeat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    Mockito.verify(heartbeatService).heartbeat(request);
  }
}
