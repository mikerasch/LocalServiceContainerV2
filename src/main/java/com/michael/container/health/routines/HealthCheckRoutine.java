package com.michael.container.health.routines;

import static com.michael.container.registry.enums.Status.STATUSES_TO_SKIP_HEARTBEAT;

import com.google.common.collect.Lists;
import com.michael.container.annotations.SkipIfAutomationEnvironment;
import com.michael.container.annotations.SkipIfFollower;
import com.michael.container.distributed.election.enums.Role;
import com.michael.container.health.repositories.HealthQueueRepository;
import com.michael.container.registry.cache.entity.ApplicationEntity;
import com.michael.container.registry.cache.entity.BaseInstance;
import com.michael.container.registry.cache.entity.HealthQueueEntity;
import com.michael.container.registry.cache.repositories.ApplicationRepository;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HealthCheckRoutine {
  private static final Logger log = LoggerFactory.getLogger(HealthCheckRoutine.class);
  private final HealthQueueRepository healthQueueRepository;
  private final ApplicationRepository applicationRepository;

  public HealthCheckRoutine(
      HealthQueueRepository healthQueueRepository, ApplicationRepository applicationRepository) {
    this.healthQueueRepository = healthQueueRepository;
    this.applicationRepository = applicationRepository;
  }

  /**
   * Periodically populates the health check queue with the health data of application entities.
   * <p>
   * This method runs at a fixed rate (every 10 seconds) and collects all application entities from
   * the repository. Each entity is translated into a health entity and added to the health entity queue
   * for later processing. The health data is only added if the application entity is not {@code null}.
   * </p>
   * <p>
   * The method does not perform any action if:
   * <ul>
   *   <li>The application entities list is empty.</li>
   *   <li>The current role is {@link Role#FOLLOWER} (as determined by {@link SkipIfFollower}).</li>
   * </ul>
   * </p>
   */
  @Scheduled(fixedRate = 10000L)
  @SkipIfFollower
  @SkipIfAutomationEnvironment
  public void populateHealthCheckQueue() {
    List<ApplicationEntity> applicationEntities =
        Lists.newArrayList(applicationRepository.findAll());
    if (applicationEntities.isEmpty()) {
      return;
    }

    Set<HealthQueueEntity> healthQueueEntities =
        applicationEntities.stream()
            .map(HealthCheckRoutine::getHealthQueueEntity)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    if (CollectionUtils.isEmpty(healthQueueEntities)) {
      return;
    }

    log.info("Queuing {} application entities for health check.", healthQueueEntities.size());

    healthQueueRepository.enqueue(healthQueueEntities);
  }

  @Nullable
  private static HealthQueueEntity getHealthQueueEntity(ApplicationEntity applicationEntity) {
    HealthQueueEntity healthQueueEntity = new HealthQueueEntity();
    healthQueueEntity.setBaseInstanceList(
        applicationEntity.getInstanceEntities().stream()
            .filter(entity -> !STATUSES_TO_SKIP_HEARTBEAT.contains(entity.getStatus()))
            .map(BaseInstance.class::cast)
            .collect(Collectors.toSet()));
    if (CollectionUtils.isEmpty(healthQueueEntity.getBaseInstanceList())) {
      return null;
    }
    return healthQueueEntity;
  }
}
