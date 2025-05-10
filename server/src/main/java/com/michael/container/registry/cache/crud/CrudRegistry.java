package com.michael.container.registry.cache.crud;

import com.michael.container.exceptions.ResourceNotFoundException;
import com.michael.container.registry.cache.entity.ApplicationEntity;
import com.michael.container.registry.cache.entity.InstanceEntity;
import com.michael.container.registry.cache.repositories.ApplicationRepository;
import com.michael.container.registry.cache.repositories.InstanceRepository;
import com.michael.container.registry.model.DeregisterEvent;
import com.michael.container.registry.model.StatusChangeEvent;
import com.michael.container.utils.ContainerConstants;
import com.michael.contract.resources.validations.enums.Status;
import com.michael.contract.resources.validations.responses.RegisterServiceResponse;
import jakarta.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class CrudRegistry {
  private static final Logger log = LoggerFactory.getLogger(CrudRegistry.class);
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
   * {@link RegisterServiceResponse}.
   * <p>
   * If the application does not already exist, a new application entity is created. After the instance is saved,
   * it is added to the application entity, and the application is either saved or updated accordingly.
   * Additionally, an event of type {@link StatusChangeEvent} is published with details from the
   * {@link RegisterServiceResponse}, including the application name, URL, version, port,
   * and the status change (before and after).
   * </p>
   *
   * @param registerServiceResponse the {@link RegisterServiceResponse} containing the service instance information
   *                               to be inserted or updated in the repository
   */
  public void insert(@Nonnull RegisterServiceResponse registerServiceResponse) {
    var applicationEntity =
        applicationRepository.findById(registerServiceResponse.applicationName()).orElse(null);

    if (applicationEntity == null) {
      applicationEntity = new ApplicationEntity();
    }

    var instanceEntity = conversionService.convert(registerServiceResponse, InstanceEntity.class);
    Status previousStatus = Objects.requireNonNull(instanceEntity).getStatus();
    if (previousStatus == null) {
      // The initial status is ALWAYS starting.
      // It will transition to the next status upon first successful heartbeat.
      instanceEntity.setStatus(Status.STARTING);
    }

    instanceRepository.save(instanceEntity);

    applicationEntity.setApplicationName(registerServiceResponse.applicationName());
    applicationEntity.addAllInstanceEntities(instanceEntity);

    applicationRepository.save(applicationEntity);

    eventPublisher.publishEvent(
        new StatusChangeEvent(
            registerServiceResponse.applicationName(),
            registerServiceResponse.url(),
            registerServiceResponse.applicationVersion(),
            registerServiceResponse.port(),
            previousStatus,
            instanceEntity.getStatus()));
  }

  /**
   * Fetches all registered services and their corresponding instances.
   * <p>
   * This method retrieves all applications from the repository and, for each application, collects
   * its associated service instances. The resulting map is organized by the application name as the key,
   * with a set of {@link RegisterServiceResponse} as the value, representing the instances of each service.
   * </p>
   *
   * @return a map where the key is the application name, and the value is a set of {@link RegisterServiceResponse}
   *         representing the registered service instances for each application
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
   * <p>
   * This method searches for an application by its name and retrieves all associated service instances.
   * If no application is found for the given name, an empty set is returned.
   * </p>
   *
   * @param applicationName the name of the application whose service instances are to be retrieved
   * @return a set of {@link RegisterServiceResponse} containing the registered service instances for the given application,
   *         or an empty set if no application is found
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
   * <p>
   * This method searches for a specific service instance using a composite key formed from the provided
   * application name, URL, port, and version. If the service instance is found, it is converted into a
   * {@link RegisterServiceResponse} and returned as an {@link Optional}.
   * </p>
   *
   * @param applicationName the name of the application whose service instance is being searched for
   * @param url the URL of the service instance to find
   * @param port the port of the service instance to find
   * @param version the version of the service instance to find
   * @return an {@link Optional} containing the {@link RegisterServiceResponse} if found, or an empty {@link Optional} if not
   */
  public Optional<RegisterServiceResponse> findOne(
      @Nonnull String applicationName, @Nonnull String url, int port, int version) {
    return instanceRepository
        .findById(InstanceEntity.formCompositeKey(applicationName, version, url, port))
        .map(entity -> conversionService.convert(entity, RegisterServiceResponse.class));
  }

  /**
   * Removes a registered service instance and its associated application if necessary.
   * <p>
   * This method deletes a service instance from the repository using a composite key formed from the provided
   * application name, URL, application version, and port. After the instance is removed, it checks if there are
   * any remaining instances for the application. If no instances are left, the application entity is also deleted
   * from the repository. Finally, a {@link DeregisterEvent} is published to notify other components of the
   * service deregistration.
   * </p>
   *
   * @param applicationName the name of the application to which the service instance belongs
   * @param url the URL of the service instance to be removed
   * @param applicationVersion the version of the application instance to be removed
   * @param port the port of the service instance to be removed
   */
  public void remove(
      @Nonnull String applicationName, @Nonnull String url, int applicationVersion, int port) {
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

  /**
   * Updates the Time-to-Live (TTL) for a service instance in the registry.
   * Allows the user to specify a ttl in milliseconds.
   * @param applicationName the name of the application for the service instance
   * @param url the URL of the service instance
   * @param applicationVersion the version of the application instance
   * @param port the port of the service instance
   */
  public void updateTTL(
      @Nonnull String applicationName,
      @Nonnull String url,
      int applicationVersion,
      int port,
      long ttl) {
    updateTTLHelper(applicationName, url, applicationVersion, port, ttl);
  }

  /**
   * Updates the Time-to-Live (TTL) for a service instance in the registry.
   * This uses the default TTL value as defined by {@link ContainerConstants#INSTANCE_ENTITY_DEFAULT_TIME_TO_LIVE}
   * @param applicationName the name of the application for the service instance
   * @param url the URL of the service instance
   * @param applicationVersion the version of the application instance
   * @param port the port of the service instance
   */
  public void updateTTL(
      @Nonnull String applicationName, @Nonnull String url, int applicationVersion, int port) {
    updateTTLHelper(
        applicationName,
        url,
        applicationVersion,
        port,
        ContainerConstants.INSTANCE_ENTITY_DEFAULT_TIME_TO_LIVE);
  }

  /**
   * Updates the status of a service instance and optionally publishes a status change event.
   * <p>
   * This method finds the {@link InstanceEntity} based on the provided application details
   * (name, URL, version, and port) and updates its status to the new status provided.
   * If {@code shouldFollowStateMachine} is {@code true}, a {@link StatusChangeEvent} is published
   * to notify listeners of the status change.
   * </p>
   *
   * @param applicationName the name of the application whose instance status is being updated
   * @param url the URL of the service instance whose status is being updated
   * @param applicationVersion the version of the application instance
   * @param port the port of the service instance whose status is being updated
   * @param status the new {@link Status} to update the service instance with
   * @param shouldFollowStateMachine flag indicating whether to publish a status change event
   *        to follow the state machine logic
   */
  public void updateStatusOnService(
      @Nonnull String applicationName,
      @Nonnull String url,
      int applicationVersion,
      int port,
      @Nonnull Status status,
      boolean shouldFollowStateMachine) {
    InstanceEntity instanceEntity =
        findInstanceEntityOrElseThrow(applicationName, url, applicationVersion, port);

    Status previousStatus = instanceEntity.getStatus();

    instanceEntity.setStatus(status);

    log.debug(
        "Updating status of instance with applicationName: {}, version: {}, url: {}, port: {} to status: {}",
        applicationName,
        applicationVersion,
        url,
        port,
        status);

    instanceRepository.save(instanceEntity);

    if (shouldFollowStateMachine) {
      eventPublisher.publishEvent(
          new StatusChangeEvent(
              applicationName, url, applicationVersion, port, previousStatus, status));
    }
  }

  private InstanceEntity findInstanceEntityOrElseThrow(
      String applicationName, String url, int applicationVersion, int port) {
    String instanceEntityCompositeKey =
        InstanceEntity.formCompositeKey(applicationName, applicationVersion, url, port);
    return instanceRepository
        .findById(instanceEntityCompositeKey)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "Instance not found with composite key %s"
                        .formatted(instanceEntityCompositeKey)));
  }

  private void updateTTLHelper(
      String applicationName, String url, int applicationVersion, int port, long ttl) {
    InstanceEntity instanceEntity =
        findInstanceEntityOrElseThrow(applicationName, url, applicationVersion, port);

    instanceEntity.setTimeToLive(ttl);

    if (Status.HEARTBEAT_STATUS_TO_HEALTHY_TRANSITIONS.contains(instanceEntity.getStatus())) {
      Status previousStatus = instanceEntity.getStatus();
      instanceEntity.setStatus(Status.HEALTHY);
      eventPublisher.publishEvent(
          new StatusChangeEvent(
              applicationName, url, applicationVersion, port, previousStatus, Status.HEALTHY));
      log.debug(
          "Instance for application '{}', version '{}', url '{}', port '{}' transitioning from {} to HEALTHY.",
          applicationName,
          applicationVersion,
          url,
          port,
          previousStatus);
    }
    instanceRepository.save(instanceEntity);
  }
}
