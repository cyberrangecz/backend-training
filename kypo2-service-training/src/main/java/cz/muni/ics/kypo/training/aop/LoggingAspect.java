package cz.muni.ics.kypo.training.aop;

import java.time.Duration;
import java.time.Instant;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Aspect
@Component
public class LoggingAspect {

	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	@Before("cz.muni.ics.kypo.training.aop.CommonJoinPointConfig.dataLayerExecutionLoggingDebug()")
	public void before(JoinPoint joinPoint) {
		LOG.debug("Persistence layer execution for {}", joinPoint);
	}

	@AfterThrowing(pointcut = "cz.muni.ics.kypo.training.aop.CommonJoinPointConfig.restLayerExecutionLoggingError()", throwing = "ex")
	public void afterThrowingExceptionInRestLayer(JoinPoint jp, Exception ex) {
		LOG.error("Error: " + jp.getSignature().getName() + ". Class: " + jp.getTarget().getClass().getSimpleName() + " Exception: {}", ex);
	}

	@Around("@annotation(cz.muni.ics.kypo.training.aop.TrackTime)")
	public void trackTimeAround(ProceedingJoinPoint joinPoint) throws Throwable {
		Instant startTime = Instant.now();
		joinPoint.proceed();
		long timeTaken = Duration.between(startTime, Instant.now()).toMillis();
		LOG.info("Time Taken by method: {} is {}", joinPoint, timeTaken);
	}

}
