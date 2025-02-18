package com.michael.container.distributed.election.observer;

import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeaseRenewalStreamObserver implements StreamObserver<LeaseKeepAliveResponse> {
  private static final Logger logger = LoggerFactory.getLogger(LeaseRenewalStreamObserver.class);

  @Override
  public void onNext(LeaseKeepAliveResponse leaseKeepAliveResponse) {
    logger.debug("Received LeaseKeepAliveResponse: {}", leaseKeepAliveResponse);
    if (leaseKeepAliveResponse == null) {
      logger.warn("Received null LeaseKeepAliveResponse");
    }
  }

  @Override
  public void onError(Throwable throwable) {
    logger.error("Error occurred during lease renewal: ", throwable);
  }

  @Override
  public void onCompleted() {
    logger.info("Lease renewal stream completed.");
  }
}
