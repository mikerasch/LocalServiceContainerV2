package com.michael.container.heartbeat.model;

import com.michael.container.heartbeat.enums.HeartbeatEvent;
import jakarta.annotation.Nonnull;

public record HeartbeatResponse(@Nonnull HeartbeatEvent event, @Nonnull String description) {}
