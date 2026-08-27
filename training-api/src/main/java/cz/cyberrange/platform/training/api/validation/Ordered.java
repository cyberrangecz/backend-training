package cz.cyberrange.platform.training.api.validation;

/**
 * Contract for an element whose position within its containing list matters: the value returned by
 * {@link #getOrder()} is used elsewhere as a zero-based index back into that list
 */
public interface Ordered {

  int getOrder();
}
