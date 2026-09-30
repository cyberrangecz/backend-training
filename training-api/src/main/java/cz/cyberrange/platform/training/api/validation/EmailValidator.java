package cz.cyberrange.platform.training.api.validation;

import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Checks a string against a hand-written pattern: one or more of {@code _A-Za-z0-9-+}, then zero or
 * more groups of any single character followed by one or more of {@code _A-Za-z0-9-}, then
 * {@code @}, then one or more of {@code A-Za-z0-9-}, zero or more groups of any single character
 * followed by one or more of {@code A-Za-z0-9}, and finally any single character followed by at
 * least two letters. The separators in the pattern are unescaped dots, so each one matches any
 * single character rather than only a literal {@code .}. Passing {@code null} throws a {@link
 * NullPointerException} instead of failing the check.
 */
public class EmailValidator implements ConstraintValidator<ValidEmail, String> {
  private static final String EMAIL_PATTERN =
      "^[_A-Za-z0-9-+]+(.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(.[A-Za-z0-9]+)*(.[A-Za-z]{2,})$";

  @Override
  public void initialize(ValidEmail constraintAnnotation) {}

  @Override
  public boolean isValid(String email, ConstraintValidatorContext context) {
    return (validateEmail(email));
  }

  private boolean validateEmail(String email) {
    return Pattern.compile(EMAIL_PATTERN).matcher(email).matches();
  }
}
