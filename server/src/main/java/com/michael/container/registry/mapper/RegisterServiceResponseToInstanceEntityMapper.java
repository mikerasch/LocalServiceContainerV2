package com.michael.container.registry.mapper;

import com.michael.container.registry.cache.entity.InstanceEntity;
import com.michael.contract.resources.validations.responses.RegisterServiceResponse;
import jakarta.annotation.Nonnull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class RegisterServiceResponseToInstanceEntityMapper
    implements Converter<RegisterServiceResponse, InstanceEntity> {
  @Override
  public InstanceEntity convert(@Nonnull RegisterServiceResponse source) {
    var entity =
        new InstanceEntity(
            source.applicationName(), source.applicationVersion(), source.url(), source.port());
    entity.setDependsOn(source.dependsOn());
    entity.setMetaData(source.metaData());
    return entity;
  }
}
