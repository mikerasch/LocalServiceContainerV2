package com.michael.container.integration.suite;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.michael.container.IntegrationTestExtension;
import com.michael.container.distributed.election.enums.Role;
import com.michael.container.distributed.election.state.ElectionState;
import com.michael.container.registry.service.ServiceRegistryService;
import com.michael.contract.resources.validations.enums.Status;
import com.michael.contract.resources.validations.requests.RegisterServiceRequest;
import com.michael.contract.resources.validations.requests.RemoveServiceRequest;
import com.michael.contract.resources.validations.requests.UpdateStatusRequest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

@EnableWireMock({
  @ConfigureWireMock(name = "first-service"),
  @ConfigureWireMock(name = "second-service")
})
class MultipleServiceTestSuite extends IntegrationTestExtension {
  @Autowired ServiceRegistryService serviceRegistryService;

  @InjectWireMock("first-service")
  WireMockServer firstService;

  @InjectWireMock("second-service")
  WireMockServer secondService;

  @Autowired ElectionState electionState;

  String firstWireMockUrl;
  int firstWireMockPort;

  String secondWireMockUrl;
  int secondWireMockPort;

  @BeforeEach
  void setup() {
    electionState.setRole(Role.LEADER);
    firstWireMockPort = firstService.port();
    firstWireMockUrl = firstService.baseUrl().replace(":" + firstWireMockPort, "");
    secondWireMockPort = secondService.port();
    secondWireMockUrl = secondService.baseUrl().replace(":" + secondWireMockPort, "");
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
    serviceRegistryService.updateStatusOnService(
        new UpdateStatusRequest(
            "second-service", 1, secondWireMockUrl, secondWireMockPort, Status.HEALTHY),
        true);
    serviceRegistryService.registerService(firstRegisterRequest);
    serviceRegistryService.updateStatusOnService(
        new UpdateStatusRequest(
            "first-service", 1, firstWireMockUrl, firstWireMockPort, Status.HEALTHY),
        true);

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
    serviceRegistryService.updateStatusOnService(
        new UpdateStatusRequest(
            "second-service", 1, secondWireMockUrl, secondWireMockPort, Status.HEALTHY),
        true);

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
    serviceRegistryService.updateStatusOnService(
        new UpdateStatusRequest(
            "first-service", 1, firstWireMockUrl, firstWireMockPort, Status.HEALTHY),
        true);
    serviceRegistryService.registerService(secondRegisterRequest);
    serviceRegistryService.updateStatusOnService(
        new UpdateStatusRequest(
            "second-service", 1, secondWireMockUrl, secondWireMockPort, Status.HEALTHY),
        true);

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
