package com.michael.container.registry.model;

import jakarta.annotation.Nonnull;

public record RegisterEvent(@Nonnull String applicationName, @Nonnull String url, int version, int port) {}
