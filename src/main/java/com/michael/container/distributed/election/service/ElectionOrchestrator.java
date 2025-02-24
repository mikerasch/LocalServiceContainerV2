package com.michael.container.distributed.election.service;

import com.michael.container.distributed.election.model.LeaderKeyDeletionEvent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class ElectionOrchestrator {
  private final ElectionProcess electionProcess;

  public ElectionOrchestrator(ElectionProcess electionProcess) {
    this.electionProcess = electionProcess;
  }

  /** Upon starting up of service, each service needs to start the leader election. */
  @PostConstruct
  public void postConstruct() {
    startLeaderElection();
  }

  /** Upon removal of bean from context, we will release leadership if applicable. */
  @PreDestroy
  public void onDestroy() {
    electionProcess.releaseLeadership();
  }

  /**
   * Start leader election. A leader election does not necessarily mean it will pick a new leader,
   * there is a short circuit to ensure if there is a current leader, nothing is needed.
   */
  @EventListener(LeaderKeyDeletionEvent.class)
  public void startLeaderElection() {
    electionProcess.startLeaderElection();
  }
}
