package com.michael.container.distributed.election.service;

import com.michael.container.distributed.election.model.LockResult;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.kv.TxnResponse;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.op.Cmp;
import io.etcd.jetcd.op.CmpTarget;
import io.etcd.jetcd.op.Op;
import io.etcd.jetcd.options.PutOption;
import jakarta.annotation.Nonnull;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import org.springframework.stereotype.Service;

@Service
public class LockProcess {
  private final Client etcdClient;

  public LockProcess(Client etcdClient) {
    this.etcdClient = etcdClient;
  }

  /**
   * Acquires a lock by performing a conditional transaction on the key-value store.
   * The lock is acquired if the specified key does not exist (version 0).
   * The method uses a lease to ensure that the lock will expire after the given time-to-live (TTL).
   *
   * @param key the key to lock
   * @param value the value associated with the lock
   * @param ttlDuration the time-to-live duration for the lock in seconds
   * @return a {@link LockResult} containing the lease ID and the transaction response
   */
  public LockResult lock(@Nonnull String key, @Nonnull String value, long ttlDuration)
      throws ExecutionException, InterruptedException {
    ByteSequence byteSequenceKey = ByteSequence.from(key, StandardCharsets.UTF_8);
    ByteSequence byteSequenceValue = ByteSequence.from(value, StandardCharsets.UTF_8);

    Cmp keyExists = new Cmp(byteSequenceKey, Cmp.Op.EQUAL, CmpTarget.version(0));

    LeaseGrantResponse leaseResponse = etcdClient.getLeaseClient().grant(ttlDuration).join();

    PutOption putOption = PutOption.builder().withLeaseId(leaseResponse.getID()).build();

    TxnResponse txnResponse =
        etcdClient
            .getKVClient()
            .txn()
            .If(keyExists)
            .Then(Op.put(byteSequenceKey, byteSequenceValue, putOption))
            .commit()
            .get();

    return new LockResult(leaseResponse.getID(), txnResponse);
  }
}
