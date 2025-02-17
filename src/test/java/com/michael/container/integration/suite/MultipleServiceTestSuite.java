package com.michael.container.integration.suite;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.michael.container.registry.cache.RegistryCache;
import com.michael.container.registry.model.RegisterServiceRequest;
import com.michael.container.registry.model.RemoveServiceRequest;
import com.michael.container.registry.service.ServiceRegistryService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock({
  @ConfigureWireMock(name = "first-service"),
  @ConfigureWireMock(name = "second-service")
})
class MultipleServiceTestSuite {
  @Autowired ServiceRegistryService serviceRegistryService;

  @Autowired RegistryCache registryCache;

  @InjectWireMock("first-service")
  WireMockServer firstService;

  @InjectWireMock("second-service")
  WireMockServer secondService;

  String firstWireMockUrl;
  int firstWireMockPort;

  String secondWireMockUrl;
  int secondWireMockPort;

  @BeforeEach
  void setup() {
    firstWireMockPort = firstService.port();
    firstWireMockUrl = firstService.baseUrl().replace(":" + firstWireMockPort, "");
    secondWireMockPort = secondService.port();
    secondWireMockUrl = secondService.baseUrl().replace(":" + secondWireMockPort, "");
  }

  @AfterEach
  void afterEach() {
    registryCache.getApplicationToRegisterServiceMap().clear();
  }

  @Test
  void firstDependsOnSecond_SecondRegisteredAlready() {
    RegisterServiceRequest firstRegisterRequest =
        new RegisterServiceRequest(
            "first-service",
            1,
            firstWireMockUrl,
            firstWireMockPort,
            Set.of("second-service"),
            new HashMap<>());
    RegisterServiceRequest secondRegisterRequest =
        new RegisterServiceRequest(
            "second-service",
            1,
            secondWireMockUrl,
            secondWireMockPort,
            new HashSet<>(),
            new HashMap<>());
    firstService.stubFor(
        post(urlEqualTo("/service-registration/notify")).willReturn(aResponse().withStatus(200)));

    serviceRegistryService.registerService(secondRegisterRequest);
    serviceRegistryService.registerService(firstRegisterRequest);

    verify(
        postRequestedFor(urlEqualTo("/service-registration/notify"))
            .withHeader("Host", matching(firstService.baseUrl().replace("http://", ""))));
  }

  @Test
  void firstDependsOnSecond_SecondNotRegisteredAlready_RegistersLater() {
    RegisterServiceRequest firstRegisterRequest =
        new RegisterServiceRequest(
            "first-service",
            1,
            firstWireMockUrl,
            firstWireMockPort,
            Set.of("second-service"),
            new HashMap<>());
    RegisterServiceRequest secondRegisterRequest =
        new RegisterServiceRequest(
            "second-service",
            1,
            secondWireMockUrl,
            secondWireMockPort,
            new HashSet<>(),
            new HashMap<>());
    firstService.stubFor(
        post(urlEqualTo("/service-registration/notify")).willReturn(aResponse().withStatus(200)));

    serviceRegistryService.registerService(firstRegisterRequest);
    serviceRegistryService.registerService(secondRegisterRequest);

    verify(
        postRequestedFor(urlEqualTo("/service-registration/notify"))
            .withHeader("Host", matching(firstService.baseUrl().replace("http://", ""))));
  }

  @Test
  void acyclicDependency_SecondNotRegisteredAlready_RegistersLater() {
    RegisterServiceRequest firstRegisterRequest =
        new RegisterServiceRequest(
            "first-service",
            1,
            firstWireMockUrl,
            firstWireMockPort,
            Set.of("second-service"),
            new HashMap<>());
    RegisterServiceRequest secondRegisterRequest =
        new RegisterServiceRequest(
            "second-service",
            1,
            secondWireMockUrl,
            secondWireMockPort,
            Set.of("first-service"),
            new HashMap<>());
    firstService.stubFor(
        post(urlEqualTo("/service-registration/notify")).willReturn(aResponse().withStatus(200)));
    secondService.stubFor(
        post(urlEqualTo("/service-registration/notify")).willReturn(aResponse().withStatus(200)));

    serviceRegistryService.registerService(firstRegisterRequest);
    serviceRegistryService.registerService(secondRegisterRequest);

    int firstServiceRequestCount =
        firstService
            .countRequestsMatching(
                postRequestedFor(urlEqualTo("/service-registration/notify"))
                    .withHeader("Host", matching(firstService.baseUrl().replace("http://", "")))
                    .build())
            .getCount();

    int secondServiceRequestCount =
        secondService
            .countRequestsMatching(
                postRequestedFor(urlEqualTo("/service-registration/notify"))
                    .withHeader("Host", matching(secondService.baseUrl().replace("http://", "")))
                    .build())
            .getCount();

    Assertions.assertEquals(1, firstServiceRequestCount);
    Assertions.assertEquals(1, secondServiceRequestCount);
  }

  @Test
  void
      firstDependsOnSecond_SecondRegisteredAlready_RemoveServiceCalled_FirstServiceReceivesDeregisterEvent() {
    RegisterServiceRequest firstRegisterRequest =
        new RegisterServiceRequest(
            "first-service",
            1,
            firstWireMockUrl,
            firstWireMockPort,
            Set.of("second-service"),
            new HashMap<>());
    RegisterServiceRequest secondRegisterRequest =
        new RegisterServiceRequest(
            "second-service",
            1,
            secondWireMockUrl,
            secondWireMockPort,
            new HashSet<>(),
            new HashMap<>());
    RemoveServiceRequest removeServiceRequest =
        new RemoveServiceRequest("second-service", secondWireMockUrl, 1, secondWireMockPort);

    firstService.stubFor(
        post(urlEqualTo("/service-registration/notify")).willReturn(aResponse().withStatus(200)));

    serviceRegistryService.registerService(secondRegisterRequest);
    serviceRegistryService.registerService(firstRegisterRequest);
    serviceRegistryService.removeService(removeServiceRequest);

    int firstServiceRequestCount =
        firstService
            .countRequestsMatching(
                postRequestedFor(urlEqualTo("/service-registration/notify"))
                    .withHeader("Host", matching(firstService.baseUrl().replace("http://", "")))
                    .withRequestBody(containing("SERVICE_DEREGISTERED"))
                    .build())
            .getCount();

    Assertions.assertEquals(1, firstServiceRequestCount);
  }
}
