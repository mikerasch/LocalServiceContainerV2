package com.michael.contract.resources.validations.responses;

import com.michael.contract.resources.validations.enums.Status;
import com.michael.contract.resources.validations.enums.Tag;
import java.util.Map;
import java.util.Set;
import org.springframework.lang.NonNull;

public record RegisterServiceResponse(
    @NonNull String applicationName,
    @NonNull int applicationVersion,
    @NonNull String url,
    @NonNull int port,
    @NonNull Status status,
    @NonNull Set<String> dependsOn,
    @NonNull Map<Tag, String> metaData) {}
