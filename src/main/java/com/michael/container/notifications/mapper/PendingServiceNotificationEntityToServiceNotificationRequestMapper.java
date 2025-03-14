package com.michael.container.notifications.mapper;

import com.michael.container.notifications.enums.NotificationType;
import com.michael.container.notifications.model.ServiceNotificationRequest;
import com.michael.container.registry.cache.entity.PendingServiceNotificationEntity;
import jakarta.annotation.Nonnull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class PendingServiceNotificationEntityToServiceNotificationRequestMapper
    implements Converter<PendingServiceNotificationEntity, ServiceNotificationRequest> {
  @Override
  @Nonnull
  public ServiceNotificationRequest convert(@Nonnull PendingServiceNotificationEntity source) {
    return new ServiceNotificationRequest(
        NotificationType.SERVICE_REGISTERED,
        source.getApplicationName(),
        source.getUrl(),
        source.getApplicationVersion(),
        source.getPort());
  }
}
