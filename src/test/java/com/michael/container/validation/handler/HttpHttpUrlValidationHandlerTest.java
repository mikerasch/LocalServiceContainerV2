package com.michael.container.validation.handler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HttpHttpUrlValidationHandlerTest {
  @InjectMocks HttpUrlValidationHandler handler;

  @ParameterizedTest
  @CsvSource({
    "'http://example.com', true",
    "'https://secure.com', true",
    "'ftp://nonhttp.com', false",
    "'example.com', false",
    "'http://10.10.10.10:8080', true",
    "'http://10.10.10.10:999999', false",
    "'http://10.10.10.10:helloWorld', false"
  })
  void UrlValidation(String url, boolean isValid) {
    boolean result = handler.isValid(url, null);
    Assertions.assertEquals(isValid, result);
  }
}
