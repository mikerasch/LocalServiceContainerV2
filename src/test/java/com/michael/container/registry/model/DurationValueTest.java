package com.michael.container.registry.model;

import com.michael.container.registry.enums.Status;
import java.time.Instant;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DurationValueTest {

  public static Stream<Arguments> instants() {
    return Stream.of(
        Arguments.of(Instant.MIN, Status.EXPIRED), Arguments.of(Instant.MAX, Status.UP));
  }

  @ParameterizedTest
  @MethodSource("instants")
  void getStatus(Instant instant, Status expected) {
    DurationValue value = new DurationValue(instant);

    Assertions.assertEquals(expected, value.getStatus());
  }
}
