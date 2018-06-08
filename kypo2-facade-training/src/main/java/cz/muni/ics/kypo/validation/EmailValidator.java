package cz.muni.ics.kypo.validation;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import cz.muni.ics.kypo.validation.annotations.ValidEmail;

/**
 * @author Pavel Šeda
 *
 */
public class EmailValidator implements ConstraintValidator<ValidEmail, String> {
  private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-+]+(.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(.[A-Za-z0-9]+)*(.[A-Za-z]{2,})$";

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
