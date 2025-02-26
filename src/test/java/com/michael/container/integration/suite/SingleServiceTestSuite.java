package com.michael.container.integration.suite;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.michael.container.IntegrationTestExtension;
import com.michael.container.health.routines.HealthCheckRoutine;
import com.michael.container.health.service.HealthCheckService;
import com.michael.container.registry.enums.Status;
import com.michael.container.registry.model.RegisterServiceRequest;
import com.michael.container.registry.model.RegisterServiceResponse;
import com.michael.container.registry.model.RemoveServiceRequest;
import com.michael.container.registry.model.UpdateStatusRequest;
import com.michael.container.registry.service.ServiceRegistryService;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

@EnableWireMock({@ConfigureWireMock(name = "first-service")})
class SingleServiceTestSuite extends IntegrationTestExtension {
  @Autowired ObjectMapper objectMapper;

  @Autowired TestRestTemplate testRestTemplate;

  @Autowired ServiceRegistryService serviceRegistryService;

  @Autowired HealthCheckService healthCheckService;

  @Autowired
  @Qualifier("healthCheckExecutorService")
  ExecutorService executorService;

  @Autowired HealthCheckRoutine healthCheckRoutine;

  @InjectWireMock("first-service")
  WireMockServer firstService;

  String wireMockUrl;
  int wireMockPort;

  HttpHeaders headers;

  @BeforeEach
  public void setUp() {
    headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    wireMockPort = firstService.port();
    wireMockUrl = firstService.baseUrl().replace(":" + wireMockPort, "");
  }

  @Test
  void registerService_SuccessfulRegistration_Endpoints() throws JsonProcessingException {
    RegisterServiceRequest registerServiceRequest =
        new RegisterServiceRequest(
            "first-service", 1, wireMockUrl, wireMockPort, new HashSet<>(), new HashMap<>());

    HttpEntity<String> entity =
        new HttpEntity<>(objectMapper.writeValueAsString(registerServiceRequest), headers);

    ResponseEntity<Void> registerResponse =
        testRestTemplate.exchange("/service-registry", HttpMethod.POST, entity, Void.class);

    Map<String, Set<RegisterServiceResponse>> fetchResponse =
        testRestTemplate
            .exchange(
                "/service-registry",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<Map<String, Set<RegisterServiceResponse>>>() {})
            .getBody();

    Assertions.assertAll(
        () ->
            Assertions.assertTrue(
                registerResponse.getStatusCode().isSameCodeAs(HttpStatus.CREATED)),
        () -> Assertions.assertEquals(1, fetchResponse.size()),
        () ->
            Assertions.assertTrue(
                fetchResponse
                    .get("first-service")
                    .contains(
                        new RegisterServiceResponse(
                            "first-service",
                            1,
                            wireMockUrl,
                            wireMockPort,
                            Status.STARTING,
                            new HashSet<>(),
                            new HashMap<>()))));
  }

  @Test
  void registerService_SuccessfulRegistration_SuccessfulDeRegistration_Endpoints()
      throws JsonProcessingException {
    RegisterServiceRequest registerServiceRequest =
        new RegisterServiceRequest(
            "first-service", 1, wireMockUrl, wireMockPort, new HashSet<>(), new HashMap<>());
    RemoveServiceRequest removeServiceRequest =
        new RemoveServiceRequest("first-service", wireMockUrl, 1, wireMockPort);

    ResponseEntity<Void> registerResponse =
        testRestTemplate.exchange(
            "/service-registry",
            HttpMethod.POST,
            new HttpEntity<>(objectMapper.writeValueAsString(registerServiceRequest), headers),
            Void.class);

    testRestTemplate.exchange(
        "/service-registry",
        HttpMethod.DELETE,
        new HttpEntity<>(objectMapper.writeValueAsString(removeServiceRequest), headers),
        Void.class);

    Map<String, Set<RegisterServiceResponse>> fetchResponse =
        testRestTemplate
            .exchange(
                "/service-registry",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<Map<String, Set<RegisterServiceResponse>>>() {})
            .getBody();

    Assertions.assertAll(
        () ->
            Assertions.assertTrue(
                registerResponse.getStatusCode().isSameCodeAs(HttpStatus.CREATED)),
        () -> Assertions.assertEquals(0, fetchResponse.size()));
  }

  @Test
  void registerService_SuccessfulRegistration_HealthCheckFailsDueToConnectTimeout_DownStatus()
      throws InterruptedException {
    RegisterServiceRequest registerServiceRequest =
        new RegisterServiceRequest(
            "first-service", 1, wireMockUrl, wireMockPort, new HashSet<>(), new HashMap<>());

    stubFor(
        get(urlEqualTo("/health")).willReturn(aResponse().withFixedDelay(10000).withStatus(200)));

    serviceRegistryService.registerService(registerServiceRequest);

    healthCheckRoutine.populateHealthCheckQueue();
    healthCheckService.performCheck();

    executorService.awaitTermination(5, TimeUnit.SECONDS);

    Assertions.assertEquals(1, serviceRegistryService.fetchAll().size());
    Assertions.assertTrue(
        serviceRegistryService.fetchAll().get("first-service").stream()
            .anyMatch(x -> x.status() == Status.DOWN));
  }

  @Test
  void registerService_SuccessfulRegistration_HealthCheckFailsDueToNot200_SetToDown()
      throws InterruptedException {
    RegisterServiceRequest registerServiceRequest =
        new RegisterServiceRequest(
            "first-service", 1, wireMockUrl, wireMockPort, new HashSet<>(), new HashMap<>());

    stubFor(get(urlEqualTo("/health")).willReturn(aResponse().withStatus(404)));

    serviceRegistryService.registerService(registerServiceRequest);
    serviceRegistryService.updateStatusOnService(
        new UpdateStatusRequest("first-service", 1, wireMockUrl, wireMockPort, Status.HEALTHY),
        true);
    healthCheckRoutine.populateHealthCheckQueue();

    healthCheckService.performCheck();

    executorService.awaitTermination(5, TimeUnit.SECONDS);

    Assertions.assertEquals(1, serviceRegistryService.fetchAll().size());
    Assertions.assertTrue(
        serviceRegistryService.fetchAll().get("first-service").stream()
            .anyMatch(x -> x.status() == Status.DOWN));
  }

  @Test
  void registerService_SuccessfulRegistration_HealthCheckPasses_KeepRegistered() {
    RegisterServiceRequest registerServiceRequest =
        new RegisterServiceRequest(
            "first-service", 1, wireMockUrl, wireMockPort, new HashSet<>(), new HashMap<>());

    stubFor(get(urlEqualTo("/health")).willReturn(aResponse().withStatus(200)));

    serviceRegistryService.registerService(registerServiceRequest);

    healthCheckService.performCheck();

    Assertions.assertEquals(1, serviceRegistryService.fetchAll().size());
  }
}
