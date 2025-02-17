package com.michael.container.registry.model;

import com.michael.container.registry.enums.Tag;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.Set;

public record RegisterServiceResponse(
    @NotNull String applicationName,
    @NotNull int applicationVersion,
    @NotNull String url,
    @NotNull int port,
    @NotNull Set<String> dependsOn,
    @NotNull Map<Tag, String> metaData) {}
