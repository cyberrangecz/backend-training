package cz.cyberrange.platform.training.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Checks that a UTC {@link LocalDateTime} is no earlier than the current UTC time minus a short
 * allowance for the delay between choosing the time and submitting it. A {@code null} value passes
 * the check.
 */
public class StartNotTooFarInPastValidator
    implements ConstraintValidator<StartNotTooFarInPast, LocalDateTime> {

  private static final Duration SUBMISSION_ALLOWANCE = Duration.ofSeconds(30);

  @Override
  public boolean isValid(LocalDateTime startTime, ConstraintValidatorContext context) {
    return startTime == null
        || !startTime.isBefore(LocalDateTime.now(Clock.systemUTC()).minus(SUBMISSION_ALLOWANCE));
  }
}
