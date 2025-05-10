package com.michael.contract.resources.validations.requests;

import com.michael.contract.resources.validations.annotations.HttpUrl;
import com.michael.contract.resources.validations.enums.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.Set;
import org.springframework.lang.Nullable;

public record RegisterServiceRequest(
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
    @Nullable
        @Schema(
            description =
                "List of application names that the service depends on. For example, if takeout-service-v1 requires food-pricing-service-v1, it will send the necessary information for making calls.",
            example = "food-pricing-service-v1")
        Set<String> dependsOn,
    @Nullable
        @Schema(
            description =
                "Metadata tags for registering service. Ideally, each service should populate all applicable Tags.")
        Map<Tag, String> metaData) {}
