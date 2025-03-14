package com.michael.container.health.routines;

import com.michael.container.IntegrationTestExtension;
import com.michael.container.distributed.election.enums.Role;
import com.michael.container.distributed.election.state.ElectionState;
import com.michael.container.health.repositories.HealthQueueRepository;
import com.michael.container.registry.cache.entity.ApplicationEntity;
import com.michael.container.registry.cache.entity.InstanceEntity;
import com.michael.container.registry.cache.repositories.ApplicationRepository;
import com.michael.container.registry.enums.Status;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ExtendWith(MockitoExtension.class)
class HealthCheckRoutineTest extends IntegrationTestExtension {
  @Autowired HealthCheckRoutine healthCheckRoutine;

  @MockitoBean HealthQueueRepository healthQueueRepository;

  @MockitoBean ApplicationRepository applicationRepository;

  @MockitoBean ElectionState electionState;

  @Test
  void populateHealthCheckQueue_Follower_EarlyReturn() {
    Mockito.when(electionState.getRole()).thenReturn(Role.FOLLOWER);

    healthCheckRoutine.populateHealthCheckQueue();

    Mockito.verifyNoInteractions(applicationRepository);
    Mockito.verify(electionState).getRole();
    Mockito.verifyNoInteractions(healthQueueRepository);
  }

  @Test
  void populateHealthCheckQueue_Leader_NoEntities_EarlyReturn() {
    Mockito.when(applicationRepository.findAll()).thenReturn(Collections.emptyList());
    Mockito.when(electionState.getRole()).thenReturn(Role.LEADER);

    healthCheckRoutine.populateHealthCheckQueue();

    Mockito.verify(applicationRepository).findAll();
    Mockito.verifyNoInteractions(healthQueueRepository);
  }

  @Test
  void populateHealthCheckQueue_Leader_AllEntitiesDown_EarlyReturn() {
    ApplicationEntity applicationEntity = new ApplicationEntity();
    applicationEntity.setApplicationName("APPLICATION_NAME");
    InstanceEntity instanceEntity =
        new InstanceEntity("APPLICATION_NAME", 1, "http://APPNAME", 8080);
    instanceEntity.setStatus(Status.DOWN);
    applicationEntity.setInstanceEntities(Set.of(instanceEntity));

    Mockito.when(applicationRepository.findAll())
        .thenReturn(Collections.singletonList(applicationEntity));
    Mockito.when(electionState.getRole()).thenReturn(Role.LEADER);

    healthCheckRoutine.populateHealthCheckQueue();
    Mockito.verifyNoInteractions(healthQueueRepository);
  }

  @Test
  void populateHealthCheckQueue_ValidEntities_PopulateRepo() {
    ApplicationEntity applicationEntity = new ApplicationEntity();
    applicationEntity.setApplicationName("APPLICATION_NAME");
    InstanceEntity instanceEntity =
        new InstanceEntity("APPLICATION_NAME", 1, "http://APPNAME", 8080);
    instanceEntity.setStatus(Status.HEALTHY);
    applicationEntity.setInstanceEntities(Set.of(instanceEntity));

    Mockito.when(applicationRepository.findAll())
        .thenReturn(Collections.singletonList(applicationEntity));
    Mockito.when(electionState.getRole()).thenReturn(Role.LEADER);

    healthCheckRoutine.populateHealthCheckQueue();
    Mockito.verify(healthQueueRepository).enqueue(Mockito.anySet());
  }
}
