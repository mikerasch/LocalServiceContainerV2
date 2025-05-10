package com.michael.container.registry.model;

import com.michael.contract.resources.validations.enums.Status;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public record StatusChangeEvent(
    @Nonnull String applicationName,
    @Nonnull String url,
    int applicationVersion,
    int port,
    @Nullable Status previousStatus,
    @Nonnull Status newStatus) {}
