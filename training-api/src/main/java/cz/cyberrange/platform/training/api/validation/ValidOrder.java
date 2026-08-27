package cz.cyberrange.platform.training.api.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Marks a {@link java.util.List} field whose elements, once sorted ascending by {@link
 * Ordered#getOrder()}, must carry sequential order values starting at 0 (0, 1, 2, ...). Each
 * element of the list must implement {@link Ordered}. A {@code null} list passes the check.
 * Checking the field sorts the annotated list itself, so the order of its elements after validation
 * is not the order they arrived in.
 */
@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = OrderValidator.class)
@Documented
public @interface ValidOrder {
  String message() default "Invalid order of questions.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
