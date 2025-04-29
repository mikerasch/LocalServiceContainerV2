package com.michael.container.auto;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ComponentScan("com.michael.container")
@Configuration
@ConditionalOnProperty(value = "service.registry.enabled", havingValue = "true")
public class ScanPackages {}
