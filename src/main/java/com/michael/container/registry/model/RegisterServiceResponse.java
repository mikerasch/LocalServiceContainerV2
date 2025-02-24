package com.michael.container.registry.model;

import com.michael.container.registry.enums.Tag;
import java.util.Map;
import java.util.Set;
import org.springframework.lang.NonNull;

public record RegisterServiceResponse(
    @NonNull String applicationName,
    @NonNull int applicationVersion,
    @NonNull String url,
    @NonNull int port,
    @NonNull Set<String> dependsOn,
    @NonNull Map<Tag, String> metaData) {}
