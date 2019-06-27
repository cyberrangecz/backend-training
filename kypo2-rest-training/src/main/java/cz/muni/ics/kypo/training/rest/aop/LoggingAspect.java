package cz.muni.ics.kypo.training.rest.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * AOP aspect for logging that cross-cutting multiple layers of project.
 *
 * @author Pavel Seda (441048)
 * @author Dominik Pilar (445537)
 */
@Aspect
@Component
public class LoggingAspect {


    /**
     * Advice executed before particular join point in data layer.
     *
     * @param joinPoint method executed in data layer.
     */
    @Before("cz.muni.ics.kypo.training.rest.aop.CommonJoinPointConfig.dataLayerExecutionLoggingDebug()")
    public void dataLayerExecutionLoggingDebug(JoinPoint joinPoint) {
        logJoinPoint(joinPoint, null);
    }

    /**
     * Advice executed before particular join point in service layer.
     *
     * @param joinPoint method executed in service layer.
     */
    @Before("cz.muni.ics.kypo.training.rest.aop.CommonJoinPointConfig.serviceLayerExecutionLoggingDebug()")
    public void serviceLayerExecutionLoggingDebug(JoinPoint joinPoint) {
        logJoinPoint(joinPoint, null);
    }

    /**
     * Advice executed before particular join point in facade layer.
     *
     * @param joinPoint method executed in facade layer.
     */
    @Before("cz.muni.ics.kypo.training.rest.aop.CommonJoinPointConfig.facadeLayerExecutionLoggingDebug()")
    public void facadeLayerExecutionLoggingDebug(JoinPoint joinPoint) {
        logJoinPoint(joinPoint, null);
    }

    /**
     * Advice executed before particular join point in rest layer.
     *
     * @param joinPoint method executed in rest layer.
     */
    @Before("cz.muni.ics.kypo.training.rest.aop.CommonJoinPointConfig.restLayerExecutionLoggingDebug()")
    public void restLayerExecutionLoggingDebug(JoinPoint joinPoint) {
        logJoinPoint(joinPoint, null);
    }

    /**
     * Advice executed after throwing exception in rest layer.
     *
     * @param joinPoint method executed in rest layer.
     */
    @Before("cz.muni.ics.kypo.training.rest.aop.CommonJoinPointConfig.restLayerExecutionLoggingError()")
    public void afterThrowingExceptionInRestLayer(JoinPoint joinPoint) {
        Exception exception = (Exception) joinPoint.getArgs()[0];
        LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringTypeName()).error( "", exception);
    }

    /**
     * It tracks how much time takes method which calls PythonAPI or UserAndGroup.
     *
     * @param joinPoint executed method with annotation @TrackTime.
     * @throws Throwable the throwable
     */
    @Around("@annotation(cz.muni.ics.kypo.training.annotations.aop.TrackTime)")
    public Object trackTimeAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Instant startTime = Instant.now();
        logJoinPoint(joinPoint, startTime);
        Object objectToReturn = joinPoint.proceed();
        logJoinPointEnd(joinPoint, startTime);

        return objectToReturn;
    }

    private void logJoinPoint(JoinPoint joinPoint, Instant time) {
        StringBuilder builder = printMethodWithParameters(joinPoint);
        if (time != null) {
            builder.append("\t Method starts in: ")
                    .append(time.toEpochMilli());
        }
        LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringTypeName()).debug(builder.toString());
    }
    private void logJoinPointEnd(JoinPoint joinPoint, Instant startTime) {
        long timeTaken = Duration.between(startTime, Instant.now()).toMillis();

        StringBuilder builder = printMethodWithParameters(joinPoint);
        builder.append("\t Method ends in: ")
                .append(Instant.now().toEpochMilli())
                .append(". ")
                .append("Time taken by method: ")
                .append(timeTaken);
        LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringTypeName()).debug(builder.toString());
    }

    private StringBuilder printMethodWithParameters(JoinPoint joinPoint) {
        StringBuilder builder = new StringBuilder();
        builder.append(joinPoint.getSignature().getName())
                .append("(");
        for(Object o : joinPoint.getArgs()) {
            if(o == null) {
                builder.append(o)
                        .append(",");
            } else {
                builder.append(o)
                        .append(",");
            }
        }
        builder.delete(builder.length()-1, builder.length());
        builder.append(")");
        return builder;
    }

}
