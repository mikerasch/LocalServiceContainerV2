package com.michael.container.distributed.election.model;

import io.etcd.jetcd.kv.TxnResponse;

public record LockResult(long leaseId, TxnResponse txnResponse) {}
