package com.michael.spring.utils.logger.handlers;

import com.michael.spring.utils.logger.annotations.ExecutionTime;
import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class ExecutionTimeAspectHandler {
  private static final long ONE_MILLION = 1_000_000L;
  private static final Logger logger = LoggerFactory.getLogger(ExecutionTimeAspectHandler.class);
  private static final String LOG_MESSAGE = "{}.{} time {} ms";

  @Pointcut("@annotation(com.michael.spring.utils.logger.annotations.ExecutionTime)")
  public void pointCutOnExecutionTimeAnnotation() {}

  @Around("pointCutOnExecutionTimeAnnotation()")
  public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    ExecutionTime executionTime = signature.getMethod().getAnnotation(ExecutionTime.class);
    Method method = signature.getMethod();
    String methodName = method.getName();
    String className = joinPoint.getTarget().getClass().getSimpleName();

    double clampedChance = Math.clamp(executionTime.chance(), 0.0, 1.0);

    if (clampedChance < Math.random()) {
      return joinPoint.proceed();
    }

    long start = System.nanoTime();

    try {
      return joinPoint.proceed();
    } finally {
      long end = System.nanoTime();
      long inMilliseconds = (end - start) / ONE_MILLION;
      logger.info(LOG_MESSAGE, className, methodName, inMilliseconds);
    }
  }
}
