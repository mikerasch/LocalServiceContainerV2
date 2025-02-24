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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.annotation.Nonnull;
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

  /**
   * Inserts a new instance into the application repository or updates an existing one based on the provided
   * {@link RegisterServiceResponse}. If the application does not already exist, a new application entity is created.
   * After the instance is saved, it is added to the application entity, and the application is saved or updated.
   * Additionally, an event of type {@link RegisterEvent} is published with the details from the
   * {@link RegisterServiceResponse}, including the application name, URL, version, and port.
   */
  public void insert(@Nonnull RegisterServiceResponse registerServiceResponse) {
    var applicationEntity =
        applicationRepository.findById(registerServiceResponse.applicationName()).orElse(null);

    if (applicationEntity == null) {
      applicationEntity = new ApplicationEntity();
    }

    var instanceEntity = conversionService.convert(registerServiceResponse, InstanceEntity.class);

    instanceRepository.save(Objects.requireNonNull(instanceEntity));

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

  /**
   * Fetches all registered services and their corresponding instances.
   * The resulting map is organized by the application name
   * as the key, with a set of {@link RegisterServiceResponse} as the value, representing the instances of
   * each service.
   */
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

  /**
   * Finds and retrieves the registered service instances for a specific application.
   * If no application is found for the given name, an empty set is returned.
   */
  public Set<RegisterServiceResponse> findByApplicationName(@Nonnull String applicationName) {
    ApplicationEntity applicationEntity =
        applicationRepository.findById(applicationName).orElse(null);

    if (applicationEntity == null) {
      return new HashSet<>();
    }

    return applicationEntity.getInstanceEntities().stream()
        .map(entity -> conversionService.convert(entity, RegisterServiceResponse.class))
        .collect(Collectors.toSet());
  }

  /**
   * Finds a single registered service instance based on application name, URL, port, and version.
   * This method searches for a specific service instance using a composite key formed from the provided
   * application name, URL, port.
   */
  public Optional<RegisterServiceResponse> findOne(
      @Nonnull String applicationName, @Nonnull String url, int port, int version) {
    return instanceRepository
        .findById(InstanceEntity.formCompositeKey(applicationName, version, url, port))
        .map(entity -> conversionService.convert(entity, RegisterServiceResponse.class));
  }

  /**
   * Removes a registered service instance and its associated application if necessary.
   * This method deletes a service instance from the repository using a composite key formed from the provided
   * application name, URL, application version, and port. After the instance is removed, it checks if there are
   * any remaining instances for the application. If no instances are left, the application entity is also deleted
   * from the repository. Finally, a {@link DeregisterEvent} is published to notify other components of the
   * service deregistration.
   */
  public void remove(@Nonnull String applicationName, @Nonnull String url, int applicationVersion, int port) {
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
