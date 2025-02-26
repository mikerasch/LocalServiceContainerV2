package com.michael.container.health.routines;

import static com.michael.container.registry.enums.Status.STATUSES_TO_SKIP_HEARTBEAT;

import com.google.common.collect.Lists;
import com.michael.container.distributed.election.enums.Role;
import com.michael.container.distributed.election.state.ElectionState;
import com.michael.container.health.repositories.HealthQueueRepository;
import com.michael.container.registry.cache.entity.ApplicationEntity;
import com.michael.container.registry.cache.entity.BaseInstance;
import com.michael.container.registry.cache.entity.HealthQueueEntity;
import com.michael.container.registry.cache.repositories.ApplicationRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HealthCheckRoutine {
  private static final Logger log = LoggerFactory.getLogger(HealthCheckRoutine.class);
  private final HealthQueueRepository healthQueueRepository;
  private final ApplicationRepository applicationRepository;
  private final ElectionState electionState;

  public HealthCheckRoutine(
      HealthQueueRepository healthQueueRepository,
      ApplicationRepository applicationRepository,
      ElectionState electionState) {
    this.healthQueueRepository = healthQueueRepository;
    this.applicationRepository = applicationRepository;
    this.electionState = electionState;
  }

  /**
   * Periodically populates the health check queue with the application entities' health data.
   * This method runs at a fixed rate (every 30 seconds) and collects all application entities from
   * the repository. All entities will be translated to a health entity and added to the health entity queue
   * for later processing.
   * This method does not perform any action if the application entities list is empty or if the
   * current role is {@link Role#FOLLOWER}.
   */
  @Scheduled(fixedRate = 30000L)
  public void populateHealthCheckQueue() {
    List<ApplicationEntity> applicationEntities =
        Lists.newArrayList(applicationRepository.findAll());
    if (applicationEntities.isEmpty() || electionState.getRole() == Role.FOLLOWER) {
      return;
    }
    // TODO NEED TO ENSURE WE ARE NOT ALLOWING A HEALTH QUEUE ENTITY WITH AN EMPTY BASE INSTANCE IN.
    Set<HealthQueueEntity> healthQueueEntities =
        applicationEntities.stream()
            .map(
                applicationEntity -> {
                  HealthQueueEntity healthQueueEntity = new HealthQueueEntity();
                  healthQueueEntity.setBaseInstanceList(
                      applicationEntity.getInstanceEntities().stream()
                          .filter(
                              entity -> !STATUSES_TO_SKIP_HEARTBEAT.contains(entity.getStatus()))
                          .map(BaseInstance.class::cast)
                          .collect(Collectors.toSet()));
                  return healthQueueEntity;
                })
            .collect(Collectors.toSet());
    log.info("Queuing {} application entities for health check.", healthQueueEntities.size());

    healthQueueRepository.enqueue(healthQueueEntities);
  }
}
