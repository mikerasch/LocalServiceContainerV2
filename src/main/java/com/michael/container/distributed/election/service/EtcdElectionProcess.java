package com.michael.container.distributed.election.service;

import com.michael.container.distributed.election.config.EtcdConfiguration;
import com.michael.container.distributed.election.enums.Role;
import com.michael.container.distributed.election.model.LeaderKeyDeletionEvent;
import com.michael.container.distributed.election.observer.LeaseRenewalStreamObserver;
import com.michael.container.distributed.election.state.ElectionState;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.kv.TxnResponse;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.op.Cmp;
import io.etcd.jetcd.op.CmpTarget;
import io.etcd.jetcd.op.Op;
import io.etcd.jetcd.options.PutOption;
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

  public EtcdElectionProcess(
      Client etcdClient,
      EtcdConfiguration etcdConfiguration,
      ApplicationEventPublisher eventPublisher,
      ElectionState electionState) {
    this.etcdClient = etcdClient;
    this.etcdConfiguration = etcdConfiguration;
    this.eventPublisher = eventPublisher;
    this.electionState = electionState;
  }

  @Override
  public void releaseLeadership() {
    try {
      etcdClient.getLeaseClient().revoke(electionState.getLeaseId()).get();
      electionState.reset();
    } catch (ExecutionException | InterruptedException | IOException e) {
      logger.warn("Release leadership exception.", e);
    }
  }

  @Override
  public void startLeaderElection() {
    determineRole();
  }

  private void determineRole() {
    boolean acquiredLeadership = false;
    LeaseGrantResponse leaseResponse = etcdClient.getLeaseClient().grant(5L).join();
    electionState.setLeaseId(leaseResponse.getID());
    ByteSequence key =
        ByteSequence.from(etcdConfiguration.getEtcdLeaderKey(), StandardCharsets.UTF_8);
    ByteSequence value =
        ByteSequence.from(
            etcdConfiguration.getServiceUniqueIdentifier().toString(), StandardCharsets.UTF_8);

    Cmp keyExists = new Cmp(key, Cmp.Op.EQUAL, CmpTarget.version(0));

    PutOption putOption = PutOption.builder().withLeaseId(electionState.getLeaseId()).build();

    try {
      TxnResponse txnResponse =
          etcdClient
              .getKVClient()
              .txn()
              .If(keyExists)
              .Then(Op.put(key, value, putOption))
              .commit()
              .get();

      if (txnResponse.isSucceeded()) {
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
