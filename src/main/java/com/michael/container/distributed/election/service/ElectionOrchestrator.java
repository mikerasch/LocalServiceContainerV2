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

  @PostConstruct
  void postConstruct() {
    startLeaderElection();
  }

  @PreDestroy
  void onDestroy() {
    electionProcess.releaseLeadership();
  }

  @EventListener(LeaderKeyDeletionEvent.class)
  void startLeaderElection() {
    electionProcess.startLeaderElection();
  }
}
