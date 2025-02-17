package com.michael.container.registry.model;

public record RegisterEvent(String applicationName, String url, int version, int port) {}
