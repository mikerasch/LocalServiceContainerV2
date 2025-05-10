package com.michael.container.registry.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.michael.container.registry.service.ServiceRegistryService;
import com.michael.contract.resources.validations.requests.RegisterServiceRequest;
import java.util.HashMap;
import java.util.HashSet;
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
class ServiceRegistryControllerTest {
  @InjectMocks ServiceRegistryController controller;

  @Mock ServiceRegistryService registryService;

  MockMvc mockMvc;
  ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setup() {
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  @Test
  void registerService_NoContent() throws Exception {
    mockMvc
        .perform(
            post("/service-registry")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new RegisterServiceRequest(
                            "applicationName",
                            1,
                            "http://google.com",
                            8080,
                            new HashSet<>(),
                            new HashMap<>()))))
        .andExpect(status().isCreated());

    Mockito.verify(registryService).registerService(Mockito.any());
  }

  @Test
  void retrieveAllServices_Success() throws Exception {
    mockMvc.perform(get("/service-registry")).andExpect(status().isOk());

    Mockito.verify(registryService).fetchAll();
  }
}
