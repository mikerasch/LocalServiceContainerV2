package com.michael.container.health.routines;

import com.google.common.collect.Lists;
import com.michael.container.distributed.election.enums.Role;
import com.michael.container.distributed.election.state.ElectionState;
import com.michael.container.health.repositories.HealthQueueRepository;
import com.michael.container.registry.cache.entity.ApplicationEntity;
import com.michael.container.registry.cache.repositories.ApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class HealthCheckRoutine {
    private static final Logger log = LoggerFactory.getLogger(HealthCheckRoutine.class);
    private final HealthQueueRepository healthQueueRepository;
    private final ApplicationRepository applicationRepository;
    private final ElectionState electionState;
    public HealthCheckRoutine(HealthQueueRepository healthQueueRepository, ApplicationRepository applicationRepository, ElectionState electionState) {
        this.healthQueueRepository = healthQueueRepository;
        this.applicationRepository = applicationRepository;
        this.electionState = electionState;
    }

    @Scheduled(fixedRate = 3000L)
    public void populateHealthCheckQueue() {
        List<ApplicationEntity> applicationEntities = Lists.newArrayList(applicationRepository.findAll());
        if (applicationEntities.isEmpty() || electionState.getRole() == Role.FOLLOWER) {
            return;
        }
        log.info("Queuing {} application entities for health check.", applicationEntities.size());
        healthQueueRepository.enqueue(applicationEntities);
    }
}
