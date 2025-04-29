package com.michael.container;

import io.etcd.jetcd.launcher.EtcdContainer;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;

public class EtcdTestConfiguration {
  private static final EtcdContainer etcdContainer =
      new EtcdContainer("quay.io/coreos/etcd:v3.5.16", "node1", new ArrayList<>())
          .withExposedPorts(2379);

  @BeforeEach
  public void setup() {
    etcdContainer.start();
    String etcdHost = etcdContainer.getHost();
    Integer etcdPort = etcdContainer.getMappedPort(2379);
    String etcdUrl = "http://" + etcdHost + ":" + etcdPort;
    System.setProperty("etcd.urls", etcdUrl);
  }
}
