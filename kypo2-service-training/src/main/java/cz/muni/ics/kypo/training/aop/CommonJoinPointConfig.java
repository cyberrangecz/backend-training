package cz.muni.ics.kypo.training.aop;

import org.aspectj.lang.annotation.Pointcut;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
public class CommonJoinPointConfig {

	@Pointcut("execution(* cz.muni.ics.kypo.training.persistence.repository.*.*(..))")
	public void dataLayerExecutionLoggingDebug() {}

	@Pointcut("execution(* cz.muni.ics.kypo.training.service.*.*(..))")
	public void serviceLayerExecutionLoggingDebug() {}

	@Pointcut("execution(* cz.muni.ics.kypo.training.facade.*.*(..))")
	public void facadeLayerExecutionLoggingDebug() {}

	@Pointcut("execution(* cz.muni.ics.kypo.training.rest.*.*(..))")
	public void restLayerExecutionLoggingDebug() {}

	@Pointcut("execution(* cz.muni.ics.kypo.training.rest.controllers*.*(..))")
	public void restLayerExecutionLoggingError() {}

}
