package com.michael.container.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceRegistryMarkerConfiguration {

  @Bean
  public Marker serviceRegistryMarkerBean() {
    return new Marker();
  }

  class Marker {}
}
