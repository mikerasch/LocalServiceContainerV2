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

  @Around("@annotation(com.michael.container.annotations.SkipIfFollower)")
  public Object skipIfFollower(ProceedingJoinPoint joinPoint) throws Throwable {
    if (electionState.getRole() == Role.FOLLOWER) {
      return null;
    }
    return joinPoint.proceed();
  }
}
