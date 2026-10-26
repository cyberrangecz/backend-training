package cz.cyberrange.platform.training.api.validation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a value as required to match the pattern implemented by {@link EmailValidator}. A {@code
 * null} value is not treated as valid: the validator throws a {@link NullPointerException} on it
 * instead of returning a validation failure.
 */
@Target({TYPE, FIELD, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = EmailValidator.class)
@Documented
public @interface ValidEmail {
  /** Message reported when the annotated value fails validation */
  String message() default "Invalid email";

  /** Validation groups this constraint belongs to, per the Bean Validation specification */
  Class<?>[] groups() default {};

  /** Payload types carried through to a client of the Bean Validation API */
  Class<? extends Payload>[] payload() default {};
}
