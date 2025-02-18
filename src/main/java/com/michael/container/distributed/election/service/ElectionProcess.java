package com.michael.container.distributed.election.service;

public interface ElectionProcess {
  void releaseLeadership();

  void startLeaderElection();
}
