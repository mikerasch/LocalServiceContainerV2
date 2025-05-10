package com.michael.container.distributed.election.service;

import com.michael.container.distributed.election.config.EtcdConfiguration;
import com.michael.container.distributed.election.enums.Role;
import com.michael.container.distributed.election.model.LeaderKeyDeletionEvent;
import com.michael.container.distributed.election.model.LockResult;
import com.michael.container.distributed.election.observer.LeaseRenewalStreamObserver;
import com.michael.container.distributed.election.state.ElectionState;
import com.michael.spring.utils.logger.annotations.ExecutionTime;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import io.etcd.jetcd.watch.WatchResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class EtcdElectionProcess implements ElectionProcess {
  private static final Logger logger = LoggerFactory.getLogger(EtcdElectionProcess.class);
  private final Client etcdClient;
  private final EtcdConfiguration etcdConfiguration;
  private final ElectionState electionState;
  private final ApplicationEventPublisher eventPublisher;
  private final LockProcess lockProcess;

  public EtcdElectionProcess(
      Client etcdClient,
      EtcdConfiguration etcdConfiguration,
      ApplicationEventPublisher eventPublisher,
      ElectionState electionState,
      LockProcess lockProcess) {
    this.etcdClient = etcdClient;
    this.etcdConfiguration = etcdConfiguration;
    this.eventPublisher = eventPublisher;
    this.electionState = electionState;
    this.lockProcess = lockProcess;
  }

  /**
   * This will revoke the lease from this server, resulting in a removal of the election key. This
   * will also trigger various cleanup actions of the election state.
   */
  @Override
  @ExecutionTime
  public void releaseLeadership() {
    try {
      if (electionState.getRole() == Role.LEADER) {
        etcdClient.getLeaseClient().revoke(electionState.getLeaseId()).get();
      }
      electionState.reset();
    } catch (ExecutionException | InterruptedException | IOException e) {
      logger.warn("Release leadership exception.", e);
    }
  }

  /**
   * Starts the leader election process:
   * 1) Create a new lease for 5 seconds.
   * 2) Fetch the ETCD Key and Value (Key is shared; Value is unique to the instance).
   * 3) Create a "put" option with the leaseId. This will only succeed if there is no existing leader key.
   * 4) If the "put" operation succeeds, initiate a new lease renewal process to keep the key alive.
   * 5) Listen for the leader key's deletion, and if deleted, start a new election process.
   */
  @Override
  @ExecutionTime
  public void startLeaderElection() {
    boolean acquiredLeadership = false;
    try {
      // Try and be efficient, we wrap all the computations into one request
      LockResult lockResult =
          lockProcess.lock(
              etcdConfiguration.getEtcdLeaderKey(), etcdConfiguration.getBaseUrl(), 5L);
      electionState.setLeaseId(lockResult.leaseId());
      if (lockResult.txnResponse().isSucceeded()) {
        acquiredLeadership = true;
        logger.info("Server elected as leader...");
        startLeaseRenewal();
      } else {
        logger.info("Server elected as follower...");
      }
    } catch (ExecutionException | InterruptedException e) {
      logger.warn("Could not acquire leadership", e);
    }
    electionState.setRole(acquiredLeadership ? Role.LEADER : Role.FOLLOWER);
    listenForKeyDeletion();
  }

  private void listenForKeyDeletion() {
    ByteSequence key =
        ByteSequence.from(etcdConfiguration.getEtcdLeaderKey(), StandardCharsets.UTF_8);
    electionState.setWatchCloseable(
        etcdClient
            .getWatchClient()
            .watch(key, WatchOption.builder().withNoPut(true).build(), this::onDeletionOfKey));
  }

  private void onDeletionOfKey(WatchResponse watchResponse) {
    if (watchResponse.getEvents().stream()
        .anyMatch(event -> event.getEventType() == WatchEvent.EventType.DELETE)) {
      logger.info("Leader deletion occurred.");
      try {
        electionState.reset();
      } catch (IOException e) {
        logger.error("Error resetting the election state after leader deletion.", e);
      } finally {
        CompletableFuture.runAsync(() -> eventPublisher.publishEvent(new LeaderKeyDeletionEvent()));
      }
    }
  }

  private void startLeaseRenewal() {
    electionState.setLeaseCloseableClient(
        etcdClient
            .getLeaseClient()
            .keepAlive(electionState.getLeaseId(), new LeaseRenewalStreamObserver()));
  }
}
