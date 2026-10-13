package cz.cyberrange.platform.training.rest.utils.sort;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the properties a paged endpoint accepts in its {@code sort} query parameter. A request
 * sorting by any other property is rejected with a {@code 400 Bad Request} once the caller is
 * authorized, and the generated OpenAPI document lists the declared properties for the parameter.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SortableProperties {

  /**
   * The list of properties the endpoint may be sorted by.
   *
   * @return the accepted sort properties
   */
  SortableList value();

  /**
   * The property the endpoint sorts by, ascending, when the request names none. It only feeds the
   * generated documentation; the sort itself is applied by the query behind the endpoint.
   *
   * @return the property of the documented default order
   */
  String defaultProperty() default "id";
}
