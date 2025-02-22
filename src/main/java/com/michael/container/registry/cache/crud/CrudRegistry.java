package com.michael.container.registry.cache.crud;

import com.michael.container.registry.cache.entity.ApplicationEntity;
import com.michael.container.registry.cache.entity.InstanceEntity;
import com.michael.container.registry.cache.repositories.ApplicationRepository;
import com.michael.container.registry.cache.repositories.InstanceRepository;
import com.michael.container.registry.model.DeregisterEvent;
import com.michael.container.registry.model.RegisterEvent;
import com.michael.container.registry.model.RegisterServiceResponse;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class CrudRegistry {
  private final ApplicationEventPublisher eventPublisher;
  private final ApplicationRepository applicationRepository;
  private final InstanceRepository instanceRepository;
  private final ConversionService conversionService;

  public CrudRegistry(
      ApplicationEventPublisher eventPublisher,
      ApplicationRepository applicationRepository,
      InstanceRepository instanceRepository,
      ConversionService conversionService) {
    this.eventPublisher = eventPublisher;
    this.applicationRepository = applicationRepository;
    this.instanceRepository = instanceRepository;
    this.conversionService = conversionService;
  }

  public void insert(RegisterServiceResponse registerServiceResponse) {
    var applicationEntity =
        applicationRepository.findById(registerServiceResponse.applicationName()).orElse(null);

    if (applicationEntity == null) {
      applicationEntity = new ApplicationEntity();
    }

    var instanceEntity = conversionService.convert(registerServiceResponse, InstanceEntity.class);

    instanceRepository.save(instanceEntity);

    applicationEntity.setApplicationName(registerServiceResponse.applicationName());
    applicationEntity.addAllInstanceEntities(instanceEntity);

    applicationRepository.save(applicationEntity);

    eventPublisher.publishEvent(
        new RegisterEvent(
            registerServiceResponse.applicationName(),
            registerServiceResponse.url(),
            registerServiceResponse.applicationVersion(),
            registerServiceResponse.port()));
  }

  public Map<String, Set<RegisterServiceResponse>> fetchAll() {
    Map<String, Set<RegisterServiceResponse>> result = new HashMap<>();

    Iterable<ApplicationEntity> applicationEntities = applicationRepository.findAll();

    for (var applicationEntity : applicationEntities) {
      var registerServiceResponses =
          applicationEntity.getInstanceEntities().stream()
              .map(
                  instanceEntity ->
                      conversionService.convert(instanceEntity, RegisterServiceResponse.class))
              .collect(Collectors.toSet());

      result.put(applicationEntity.getApplicationName(), registerServiceResponses);
    }

    return result;
  }

  public Set<RegisterServiceResponse> findByApplicationName(String applicationName) {
    ApplicationEntity applicationEntity =
        applicationRepository.findById(applicationName).orElse(null);

    if (applicationEntity == null) {
      return new HashSet<>();
    }

    return applicationEntity.getInstanceEntities().stream()
        .map(entity -> conversionService.convert(entity, RegisterServiceResponse.class))
        .collect(Collectors.toSet());
  }

  public Optional<RegisterServiceResponse> findOne(
      String applicationName, String url, int port, int version) {
    return instanceRepository
        .findById(InstanceEntity.formCompositeKey(applicationName, version, url, port))
        .map(entity -> conversionService.convert(entity, RegisterServiceResponse.class));
  }

  public void remove(String applicationName, String url, int applicationVersion, int port) {
    instanceRepository.deleteById(
        InstanceEntity.formCompositeKey(applicationName, applicationVersion, url, port));

    applicationRepository
        .findById(applicationName)
        .map(ApplicationEntity::getInstanceEntities)
        .filter(CollectionUtils::isEmpty)
        .ifPresent(ignored -> applicationRepository.deleteById(applicationName));

    eventPublisher.publishEvent(
        new DeregisterEvent(applicationName, url, applicationVersion, port));
  }
}
