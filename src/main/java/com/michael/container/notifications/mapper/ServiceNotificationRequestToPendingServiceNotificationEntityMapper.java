package com.michael.container.notifications.mapper;

import com.michael.container.notifications.model.ServiceNotificationRequest;
import com.michael.container.registry.cache.entity.PendingServiceNotificationEntity;
import jakarta.annotation.Nonnull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ServiceNotificationRequestToPendingServiceNotificationEntityMapper
    implements Converter<ServiceNotificationRequest, PendingServiceNotificationEntity> {
  @Override
  @Nonnull
  public PendingServiceNotificationEntity convert(@Nonnull ServiceNotificationRequest source) {
    var entity = new PendingServiceNotificationEntity();
    entity.setPort(source.port());
    entity.setUrl(source.url());
    entity.setApplicationName(source.applicationName());
    entity.setApplicationVersion(source.applicationVersion());
    return entity;
  }
}
