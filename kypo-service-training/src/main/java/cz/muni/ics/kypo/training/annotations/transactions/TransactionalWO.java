package cz.muni.ics.kypo.training.annotations.transactions;

import org.springframework.core.annotation.AliasFor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

/**
 * Extending of the class {@link Transactional} which has <i>read-only</i> set to false.
 *
 */
@Transactional(rollbackFor = Exception.class)
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface TransactionalWO {

    /**
     * Value string.
     *
     * @return the string
     */
    @AliasFor("transactionManager")
    String value() default "";

    /**
     * Transaction manager string.
     *
     * @return the string
     */
    @AliasFor("value")
    String transactionManager() default "";

    /**
     * Propagation propagation.
     *
     * @return the propagation
     */
    Propagation propagation() default Propagation.REQUIRED;

    /**
     * Isolation isolation.
     *
     * @return the isolation
     */
    Isolation isolation() default Isolation.DEFAULT;

    /**
     * Timeout int.
     *
     * @return the int
     */
    int timeout() default -1;
}