package com.michael.container.registry.mapper;

import com.michael.container.registry.enums.Status;
import com.michael.container.registry.model.RegisterServiceRequest;
import com.michael.container.registry.model.RegisterServiceResponse;
import jakarta.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class RegisterServiceRequestToRegisterServiceResponseMapper
    implements Converter<RegisterServiceRequest, RegisterServiceResponse> {
  @Override
  public RegisterServiceResponse convert(@Nonnull RegisterServiceRequest source) {
    return new RegisterServiceResponse(
        source.applicationName(),
        source.applicationVersion(),
        source.url(),
        source.port(),
        Status.STARTING, // Should be safe since this is only used for creating
        source.dependsOn() == null ? new HashSet<>() : source.dependsOn(),
        source.metaData() == null ? new HashMap<>() : source.metaData());
  }
}
