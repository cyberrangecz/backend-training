package cz.muni.ics.kypo.training.rest.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * @author Pavel Seda (441048)
 */
@Aspect
@Component
public class LoggingAspect {

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Before("cz.muni.ics.kypo.training.rest.aop.CommonJoinPointConfig.dataLayerExecutionLoggingDebug()")
    public void dataLayerExecutionLoggingDebug(JoinPoint joinPoint) {
        LOG.debug("Persistence layer execution for {}", joinPoint);
    }

    @Before("cz.muni.ics.kypo.training.rest.aop.CommonJoinPointConfig.serviceLayerExecutionLoggingDebug()")
    public void serviceLayerExecutionLoggingDebug(JoinPoint joinPoint) {
        LOG.debug("Service layer execution for {}", joinPoint);
    }

    @Before("cz.muni.ics.kypo.training.rest.aop.CommonJoinPointConfig.facadeLayerExecutionLoggingDebug()")
    public void facadeLayerExecutionLoggingDebug(JoinPoint joinPoint) {
        LOG.debug("Facade layer execution for {}", joinPoint);
    }

    @Before("cz.muni.ics.kypo.training.rest.aop.CommonJoinPointConfig.restLayerExecutionLoggingDebug()")
    public void restLayerExecutionLoggingDebug(JoinPoint joinPoint) {
        LOG.debug("Rest layer execution for {}", joinPoint);
    }

    @AfterThrowing(pointcut = "cz.muni.ics.kypo.training.rest.aop.CommonJoinPointConfig.restLayerExecutionLoggingError()", throwing = "ex")
    public void afterThrowingExceptionInRestLayer(JoinPoint jp, Exception ex) {
        LOG.error("Error: " + jp.getSignature().getName() + ". Class: " + jp.getTarget().getClass().getSimpleName() + " Exception: {}", ex);
    }

    @Around("@annotation(cz.muni.ics.kypo.training.rest.aop.annotations.TrackTime)")
    public void trackTimeAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Instant startTime = Instant.now();
        joinPoint.proceed();
        long timeTaken = Duration.between(startTime, Instant.now()).toMillis();
        LOG.info("Time Taken by method: {} is {}", joinPoint, timeTaken);
    }

}
