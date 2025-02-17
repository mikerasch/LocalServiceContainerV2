package com.michael.container.heartbeat.model;

import com.michael.container.heartbeat.enums.HeartbeatEvent;

public record HeartbeatResponse(HeartbeatEvent event, String description) {}
