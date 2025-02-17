package com.michael.container.notifications.model;

import com.michael.container.notifications.enums.NotificationType;

public record ServiceNotificationRequest(
    NotificationType notificationType,
    String applicationName,
    String url,
    int applicationVersion,
    int port) {}
