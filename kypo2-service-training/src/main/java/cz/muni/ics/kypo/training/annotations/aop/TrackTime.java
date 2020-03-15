package cz.muni.ics.kypo.training.annotations.aop;

import java.lang.annotation.*;

/**
 * The interface Track time.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface TrackTime {

}
