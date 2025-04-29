package com.michael.container.aspect;

import com.michael.container.annotations.SkipIfAutomationEnvironment;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SkipIfAutomationEnvironmentAspect {
  private final Environment environment;

  public SkipIfAutomationEnvironmentAspect(final Environment environment) {
    this.environment = environment;
  }

  /**
   * Aspect handler for methods annotated with {@link SkipIfAutomationEnvironment}.
   * Skips the method execution if the environment is being run in automation mode.
   *
   * @param joinPoint the join point representing the method invocation
   * @return the result of the method execution, or {@code null} if skipped
   * @throws Throwable if the method execution throws an exception
   */
  @Around("@annotation(com.michael.container.annotations.SkipIfAutomationEnvironment)")
  public Object skipIfAutomationEnvironment(ProceedingJoinPoint joinPoint) throws Throwable {
    if (containsEnvironmentToSkip()) {
      return null;
    }
    return joinPoint.proceed();
  }

  private boolean containsEnvironmentToSkip() {
    return environment.matchesProfiles("automation");
  }
}
