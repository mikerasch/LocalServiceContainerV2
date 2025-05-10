package com.michael.contract.resources.validations.responses;

import com.michael.contract.resources.validations.enums.HeartbeatEvent;
import org.springframework.lang.NonNull;

public record HeartbeatResponse(@NonNull HeartbeatEvent event, @NonNull String description) {}
