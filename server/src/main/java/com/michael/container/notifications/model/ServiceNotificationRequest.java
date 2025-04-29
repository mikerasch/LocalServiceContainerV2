package com.michael.container.notifications.model;

import com.michael.container.notifications.enums.NotificationType;
import jakarta.annotation.Nonnull;

public record ServiceNotificationRequest(
    @Nonnull NotificationType notificationType,
    @Nonnull String applicationName,
    @Nonnull String url,
    int applicationVersion,
    int port) {}
