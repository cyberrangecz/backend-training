package cz.muni.ics.kypo.training.rest.aop;

import org.aspectj.lang.annotation.Pointcut;

public class CommonJoinPointConfig {

    @Pointcut("execution(* cz.muni.ics.kypo.training.persistence.repository.*.*(..))")
    public void dataLayerExecutionLoggingDebug() {
    }

    @Pointcut("execution(* cz.muni.ics.kypo.training.service.*.*(..)) && !@annotation(cz.muni.ics.kypo.training.annotations.aop.TrackTime)")
    public void serviceLayerExecutionLoggingDebug() {
    }

    @Pointcut("execution(* cz.muni.ics.kypo.training.facade.*.*(..))")
    public void facadeLayerExecutionLoggingDebug() {
    }

    @Pointcut("within(cz.muni.ics.kypo.training.rest.controllers.*)")
    public void restLayerExecutionLoggingDebug() {
    }

    @Pointcut("execution(* cz.muni.ics.kypo.training.rest.CustomRestExceptionHandlerTraining.*(..))")
    public void restLayerExecutionLoggingError() {
    }

}
