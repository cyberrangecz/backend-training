package cz.cyberrange.platform.training.api.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a {@link java.time.LocalDateTime} field holding a UTC start time that must not lie in the
 * past, allowing for the short delay between choosing the time and submitting it, as checked by
 * {@link StartNotTooFarInPastValidator}. A {@code null} value passes the check.
 */
@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = StartNotTooFarInPastValidator.class)
@Documented
public @interface StartNotTooFarInPast {

  /** Message reported when the annotated time lies in the past */
  String message() default "{trainingInstance.startTime.StartNotTooFarInPast.message}";

  /** Validation groups this constraint belongs to, per the Bean Validation specification */
  Class<?>[] groups() default {};

  /** Payload types carried through to a client of the Bean Validation API */
  Class<? extends Payload>[] payload() default {};
}
