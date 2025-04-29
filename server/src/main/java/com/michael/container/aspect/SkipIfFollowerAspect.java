package com.michael.container.aspect;

import com.michael.container.distributed.election.enums.Role;
import com.michael.container.distributed.election.state.ElectionState;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SkipIfFollowerAspect {
  private final ElectionState electionState;

  public SkipIfFollowerAspect(ElectionState electionState) {
    this.electionState = electionState;
  }

  /**
   * Aspect handler for methods annotated with {@link com.michael.container.annotations.SkipIfFollower}.
   * Skips the method execution if the service's role is {@link Role#FOLLOWER}.
   *
   * @param joinPoint the join point representing the method invocation
   * @return the result of the method execution, or {@code null} if skipped
   * @throws Throwable if the method execution throws an exception
   */
  @Around("@annotation(com.michael.container.annotations.SkipIfFollower)")
  public Object skipIfFollower(ProceedingJoinPoint joinPoint) throws Throwable {
    if (electionState.getRole() == Role.FOLLOWER) {
      return null;
    }
    return joinPoint.proceed();
  }
}
