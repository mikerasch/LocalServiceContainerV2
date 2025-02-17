package com.michael.container.registry.model;

public record DeregisterEvent(String applicationName, String url, int version, int port) {}
