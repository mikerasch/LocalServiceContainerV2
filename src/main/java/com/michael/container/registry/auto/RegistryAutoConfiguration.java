package com.michael.container.registry.auto;

import com.michael.container.distributed.election.state.ElectionState;
import com.michael.container.registry.cache.crud.CrudRegistry;
import com.michael.container.registry.cache.listener.key.ExpiredKeyListener;
import com.michael.container.registry.cache.listener.key.KeyListener;
import com.michael.container.registry.cache.listener.key.KeyOrchestrator;
import com.michael.container.registry.cache.repositories.ApplicationRepository;
import com.michael.container.registry.cache.repositories.InstanceRepository;
import com.michael.container.registry.controller.ServiceRegistryController;
import com.michael.container.registry.mapper.InstanceEntityToRegisterServiceResponseMapper;
import com.michael.container.registry.mapper.RegisterServiceRequestToRegisterServiceResponseMapper;
import com.michael.container.registry.mapper.RegisterServiceResponseToInstanceEntityMapper;
import com.michael.container.registry.service.ServiceRegistryService;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

public class RegistryAutoConfiguration {
  @Configuration
  public static class ControllerConfig {
    @ConditionalOnMissingBean
    @Bean
    public ServiceRegistryController serviceRegistryController(
        ServiceRegistryService serviceRegistryService) {
      return new ServiceRegistryController(serviceRegistryService);
    }
  }

  @Configuration
  @EnableRedisRepositories("com.michael.container.registry.entity.repositories")
  @EntityScan("com.michael.container.registry.cache.entity")
  public static class CacheConfig {
    @ConditionalOnMissingBean
    @Bean
    public CrudRegistry crudRegistry(
        ApplicationEventPublisher eventPublisher,
        ApplicationRepository applicationRepository,
        InstanceRepository instanceRepository,
        ConversionService conversionService) {
      return new CrudRegistry(
          eventPublisher, applicationRepository, instanceRepository, conversionService);
    }

    @ConditionalOnMissingBean
    @Bean
    public ExpiredKeyListener expiredKeyListener(ServiceRegistryService service) {
      return new ExpiredKeyListener(service);
    }

    @ConditionalOnMissingBean
    @Bean
    public KeyOrchestrator keyOrchestrator(
        Set<KeyListener> keyListenerSet, ElectionState electionState) {
      return new KeyOrchestrator(keyListenerSet, electionState);
    }
  }

  @Configuration
  public static class ServiceConfig {
    @ConditionalOnMissingBean
    @Bean
    public ServiceRegistryService serviceRegistryService(
        ConversionService conversionService, CrudRegistry crudRegistry) {
      return new ServiceRegistryService(conversionService, crudRegistry);
    }
  }

  @Configuration
  public static class MapperConfig {

    @ConditionalOnMissingBean
    @Bean
    public InstanceEntityToRegisterServiceResponseMapper
        instanceEntityToRegisterServiceResponseMapper() {
      return new InstanceEntityToRegisterServiceResponseMapper();
    }

    @ConditionalOnMissingBean
    @Bean
    public RegisterServiceRequestToRegisterServiceResponseMapper
        registerServiceRequestToRegisterServiceResponseMapper() {
      return new RegisterServiceRequestToRegisterServiceResponseMapper();
    }

    @ConditionalOnMissingBean
    @Bean
    public RegisterServiceResponseToInstanceEntityMapper
        registerServiceResponseToInstanceEntityMapper() {
      return new RegisterServiceResponseToInstanceEntityMapper();
    }
  }
}
