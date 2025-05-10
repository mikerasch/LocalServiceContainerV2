package com.michael.container.registry.cache.crud;

import com.michael.container.RedisTestConfiguration;
import com.michael.container.distributed.election.enums.Role;
import com.michael.container.distributed.election.state.ElectionState;
import com.michael.container.registry.cache.repositories.ApplicationRepository;
import com.michael.container.registry.cache.repositories.InstanceRepository;
import com.michael.container.registry.mapper.InstanceEntityToRegisterServiceResponseMapper;
import com.michael.container.registry.mapper.RegisterServiceResponseToInstanceEntityMapper;
import com.michael.contract.resources.validations.enums.Status;
import com.michael.contract.resources.validations.responses.RegisterServiceResponse;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.support.DefaultConversionService;

@ExtendWith({MockitoExtension.class})
class CrudRegistryTest extends RedisTestConfiguration {
  CrudRegistry crudRegistry;
  @Mock ApplicationEventPublisher eventPublisher;
  @Autowired ApplicationRepository applicationRepository;
  @Autowired InstanceRepository instanceRepository;

  @TestConfiguration
  static class TestConfig {
    @Bean
    public ElectionState electionState() {
      var electionState = new ElectionState();
      electionState.setRole(Role.LEADER);
      return electionState;
    }
  }

  @BeforeEach
  void beforeEach() {
    DefaultConversionService conversionService = new DefaultConversionService();
    conversionService.addConverter(new RegisterServiceResponseToInstanceEntityMapper());
    conversionService.addConverter(new InstanceEntityToRegisterServiceResponseMapper());
    crudRegistry =
        new CrudRegistry(
            eventPublisher, applicationRepository, instanceRepository, conversionService);
  }

  @Test
  void insert_InsertsIntoCache() {
    var registerServiceResponse =
        new RegisterServiceResponse(
            "applicationName",
            1,
            "localhost",
            8080,
            Status.STARTING,
            new HashSet<>(),
            new HashMap<>());

    crudRegistry.insert(registerServiceResponse);

    Assertions.assertTrue(
        crudRegistry.fetchAll().values().stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toSet())
            .contains(registerServiceResponse));
  }

  @Test
  void fetchAll_RetrievesNewMap() {
    var registerServiceResponse =
        new RegisterServiceResponse(
            "applicationName",
            1,
            "localhost",
            8080,
            Status.STARTING,
            new HashSet<>(),
            new HashMap<>());
    crudRegistry.insert(registerServiceResponse);

    Set<RegisterServiceResponse> response = crudRegistry.fetchAll().get("applicationName");

    Assertions.assertAll(
        () -> Assertions.assertEquals(1, response.size()),
        () -> Assertions.assertTrue(response.contains(registerServiceResponse)));

    response.add(
        new RegisterServiceResponse(
            "applicationName", 1, "test", 9090, Status.STARTING, new HashSet<>(), new HashMap<>()));

    Assertions.assertEquals(1, crudRegistry.fetchAll().size());
  }

  @Test
  void findOne_ResponseNotFound() {
    RegisterServiceResponse response =
        crudRegistry.findOne("applicationName", "test", 9090, 1).orElse(null);

    Assertions.assertNull(response);
  }

  @Test
  void findOne_ResponseFound() {
    crudRegistry.insert(
        new RegisterServiceResponse(
            "applicationName", 1, "test", 9090, Status.STARTING, new HashSet<>(), new HashMap<>()));

    RegisterServiceResponse response =
        crudRegistry.findOne("applicationName", "test", 9090, 1).orElse(null);

    Assertions.assertAll(
        () -> Assertions.assertNotNull(response),
        () -> Assertions.assertEquals("test", response.url()),
        () -> Assertions.assertEquals(9090, response.port()));
  }

  @Test
  void remove_WithHostNameAndPort() {
    crudRegistry.insert(
        new RegisterServiceResponse(
            "applicationName", 1, "test", 9090, Status.STARTING, new HashSet<>(), new HashMap<>()));

    crudRegistry.remove("applicationName", "test", 1, 9090);

    Assertions.assertTrue(
        crudRegistry.fetchAll().values().stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toSet())
            .isEmpty());
  }

  @Test
  void remove_WithResponse() {
    var registerServiceResponse =
        new RegisterServiceResponse(
            "applicationName", 1, "url", 9090, Status.STARTING, new HashSet<>(), new HashMap<>());

    crudRegistry.remove(
        registerServiceResponse.applicationName(),
        registerServiceResponse.url(),
        registerServiceResponse.applicationVersion(),
        registerServiceResponse.port());

    Assertions.assertTrue(crudRegistry.fetchAll().isEmpty());
  }
}
