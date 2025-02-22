package com.michael.container.registry.mapper;

import com.michael.container.registry.cache.entity.InstanceEntity;
import com.michael.container.registry.model.RegisterServiceResponse;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class InstanceEntityToRegisterServiceResponseMapper
    implements Converter<InstanceEntity, RegisterServiceResponse> {

  @Override
  public RegisterServiceResponse convert(InstanceEntity source) {
    return new RegisterServiceResponse(
        source.getApplicationName(),
        source.getApplicationVersion(),
        source.getUrl(),
        source.getPort(),
        source.getDependsOn(),
        source.getMetaData());
  }
}
