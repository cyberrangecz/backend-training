package cz.cyberrange.platform.training.api.validation;

import java.util.Comparator;
import java.util.List;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Checks that, once sorted ascending by {@link Ordered#getOrder()}, a list's elements carry
 * sequential order values starting at 0 (0, 1, 2, ...). Sorts the list in place while checking it.
 * A {@code null} list passes the check. A list containing a {@code null} element throws a {@link
 * NullPointerException}.
 */
public class OrderValidator implements ConstraintValidator<ValidOrder, List<? extends Ordered>> {

  @Override
  public void initialize(ValidOrder constraintAnnotation) {}

  @Override
  public boolean isValid(List<? extends Ordered> entities, ConstraintValidatorContext context) {
    if (entities == null) {
      return true;
    }
    int actualOrder = 0;
    entities.sort(Comparator.comparingInt(Ordered::getOrder));
    for (Ordered entity : entities) {
      if (entity.getOrder() != actualOrder) {
        context.disableDefaultConstraintViolation();
        context
            .buildConstraintViolationWithTemplate(
                "The order of the entity '"
                    + entity.getClass()
                    + "' has unexpected order value."
                    + " Expected order is "
                    + actualOrder
                    + ", but actual is "
                    + entity.getOrder()
                    + ".")
            .addConstraintViolation();
        return false;
      }
      actualOrder++;
    }

    return true;
  }
}
