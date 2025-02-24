package com.michael.container.validation.handler;

import com.michael.container.validation.annotation.HttpUrl;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;


public class HttpUrlValidationHandler implements ConstraintValidator<HttpUrl, String> {
  private static final int MINIMUM_PORT = 0;
  private static final int MAXIMUM_PORT = 65535;
  private static final Set<String> VALID_SCHEMES = Set.of("HTTP", "HTTPS");

  @Override
  public boolean isValid(String url, ConstraintValidatorContext constraintValidatorContext) {
    try {
      if (StringUtils.isEmpty(url)) {
        return false;
      }
      var uri = new URI(url);
      return Stream.of(() -> checkScheme(uri), (Supplier<Boolean>) () -> checkPort(uri))
          .allMatch(Supplier::get);
    } catch (URISyntaxException e) {
      return false;
    }
  }

  private Boolean checkPort(URI uri) {
    String[] portSplit = uri.toString().split(":");
    if (portSplit.length == 2) {
      return true;
    }
    try {
      int port = Integer.parseInt(portSplit[2]);
      return port >= MINIMUM_PORT && port <= MAXIMUM_PORT;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  private boolean checkScheme(URI uri) {
    return VALID_SCHEMES.stream()
        .anyMatch(validScheme -> StringUtils.equalsIgnoreCase(validScheme, uri.getScheme()));
  }
}
