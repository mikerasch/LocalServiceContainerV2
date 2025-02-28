package com.michael.container.registry.model;

import com.michael.container.registry.enums.Status;
import com.michael.container.validation.annotation.HttpUrl;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
    @NotNull
        @Schema(
            description =
                "Application name. This should follow the {service-name}-{service}-{version}",
            example = "takeout-service-v1")
        String applicationName,
    @NotNull
        @Schema(description = "The version of the application being registered.", example = "1")
        int applicationVersion,
    @HttpUrl
        @NotNull
        @Schema(
            description =
                "The full url where the service is accessible. Includes the context-path.",
            example = "http://takeout-service.com:80/takeout-service/v1")
        String url,
    @NotNull
        @Schema(
            description =
                "Port should be similar to the one provided in the url. Will be removed in future as we can detect it based on url.",
            example = "8080")
        int port,
    @NotNull Status status) {}
