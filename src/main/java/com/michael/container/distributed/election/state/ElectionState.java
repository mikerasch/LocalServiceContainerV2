package com.michael.container.distributed.election.state;

import com.michael.container.distributed.election.enums.Role;
import io.etcd.jetcd.support.CloseableClient;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.IOException;

@Component
public class ElectionState {
  private Long leaseId;
  private CloseableClient leaseCloseableClient;
  private Closeable watchCloseable;
  private Role role;

  public Long getLeaseId() {
    return leaseId;
  }

  public void setLeaseId(long leaseId) {
    this.leaseId = leaseId;
  }

  public CloseableClient getLeaseCloseableClient() {
    return leaseCloseableClient;
  }

  public void setLeaseCloseableClient(CloseableClient leaseCloseableClient) {
    this.leaseCloseableClient = leaseCloseableClient;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public Closeable getWatchCloseable() {
    return watchCloseable;
  }

  public void setWatchCloseable(Closeable watchCloseable) {
    this.watchCloseable = watchCloseable;
  }

  public void reset() throws IOException {
    leaseId = null;
    role = null;
    if (leaseCloseableClient != null) {
      leaseCloseableClient.close();
    }
    if (watchCloseable != null) {
      watchCloseable.close();
    }
  }
}
