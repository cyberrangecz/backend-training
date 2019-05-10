package cz.muni.ics.kypo.training.annotations.transactions;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

/**
 * @author Pavel Seda (441048)
 */
@Transactional(rollbackFor = Exception.class)
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface TransactionalWO {
    Propagation propagation() default Propagation.REQUIRED;
}