package cz.muni.ics.kypo.training.annotations.transactions;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

/**
 * @author Pavel Seda
 */
@Transactional(rollbackFor = Exception.class, readOnly = true, propagation = Propagation.REQUIRES_NEW)
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface TransactionalROPropagationRequiredNew {
}
