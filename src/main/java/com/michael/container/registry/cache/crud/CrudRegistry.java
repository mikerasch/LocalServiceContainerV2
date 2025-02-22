package com.michael.container.registry.cache.crud;

import com.michael.container.registry.cache.entity.ApplicationEntity;
import com.michael.container.registry.cache.entity.InstanceEntity;
import com.michael.container.registry.cache.repositories.ApplicationRepository;
import com.michael.container.registry.cache.repositories.InstanceRepository;
import com.michael.container.registry.model.DeregisterEvent;
import com.michael.container.registry.model.RegisterEvent;
import com.michael.container.registry.model.RegisterServiceResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class CrudRegistry {
  private final ApplicationEventPublisher eventPublisher;
  private final ApplicationRepository applicationRepository;
  private final InstanceRepository instanceRepository;

  public CrudRegistry(
      ApplicationEventPublisher eventPublisher,
      ApplicationRepository applicationRepository,
      InstanceRepository instanceRepository) {
    this.eventPublisher = eventPublisher;
    this.applicationRepository = applicationRepository;
    this.instanceRepository = instanceRepository;
  }

  public void insert(RegisterServiceResponse registerServiceResponse) {
    var applicationEntity =
        applicationRepository.findById(registerServiceResponse.applicationName()).orElse(null);

    if (applicationEntity == null) {
      applicationEntity = new ApplicationEntity();
    }

    var instanceEntity =
        new InstanceEntity(
            registerServiceResponse.applicationName(),
            registerServiceResponse.applicationVersion(),
            registerServiceResponse.url(),
            registerServiceResponse.port());

    instanceEntity.setDependsOn(registerServiceResponse.dependsOn());
    instanceEntity.setMetaData(registerServiceResponse.metaData());
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
                      new RegisterServiceResponse(
                          instanceEntity.getApplicationName(),
                          instanceEntity.getApplicationVersion(),
                          instanceEntity.getUrl(),
                          instanceEntity.getPort(),
                          instanceEntity.getDependsOn(),
                          instanceEntity.getMetaData()))
              .collect(Collectors.toSet());

      result.put(applicationEntity.getApplicationName(), registerServiceResponses);
    }

    return result;
  }

  public Optional<RegisterServiceResponse> findOne(
      String applicationName, String url, int port, int version) {
    return instanceRepository
        .findById(InstanceEntity.formCompositeKey(applicationName, version, url, port))
        .map(
            entity ->
                new RegisterServiceResponse(
                    entity.getApplicationName(),
                    entity.getApplicationVersion(),
                    entity.getUrl(),
                    entity.getPort(),
                    entity.getDependsOn(),
                    entity.getMetaData()));
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
