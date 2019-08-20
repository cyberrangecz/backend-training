package cz.muni.ics.kypo.training.annotations.aop;

import java.lang.annotation.*;

/**
 * @author Pavel Seda
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface TrackTime {

}
