package com.michael.container.validation.annotation;

import com.michael.container.validation.handler.HttpUrlValidationHandler;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.hibernate.validator.constraints.URL;

@Target({ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = HttpUrlValidationHandler.class)
@NotNull
public @interface HttpUrl {
  String message() default
      "A valid url must have a HTTP(s) protocol. With no port. An example: http://google.com or https://10.10.10.10";

  @URL
  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
