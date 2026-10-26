package cz.cyberrange.platform.training.service.services.api;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.util.UriBuilder;

/**
 * Appends query parameters to a {@link UriBuilder} so that every value reaches the called service
 * unchanged. Each value is passed to the builder as a URI template variable, which the builder
 * encodes strictly, instead of as template text, where braces would be read as variables and
 * characters such as {@code +}, {@code &} and {@code =} would stay unescaped.
 */
public final class UriQueryParameters {

  private static final String VARIABLE_NAME_PREFIX = "queryValue";

  private final UriBuilder uriBuilder;
  private final Map<String, Object> templateVariables = new HashMap<>();

  /**
   * Wraps the builder the parameters are appended to.
   *
   * @param uriBuilder the builder already holding the request path
   */
  public UriQueryParameters(UriBuilder uriBuilder) {
    this.uriBuilder = uriBuilder;
  }

  /**
   * Appends one query parameter.
   *
   * @param name the parameter name, taken as literal template text
   * @param value the parameter value, encoded so it arrives unchanged
   * @return this instance
   */
  public UriQueryParameters add(String name, Object value) {
    String variableName = VARIABLE_NAME_PREFIX + templateVariables.size();
    templateVariables.put(variableName, value);
    uriBuilder.queryParam(name, "{" + variableName + "}");
    return this;
  }

  /**
   * Appends one query parameter per value, all under the same name.
   *
   * @param name the parameter name, taken as literal template text
   * @param values the parameter values, each encoded so it arrives unchanged
   * @return this instance
   */
  public UriQueryParameters addAll(String name, Collection<?> values) {
    values.forEach(value -> add(name, value));
    return this;
  }

  /**
   * Builds the URI with every appended value expanded and encoded.
   *
   * @return the finished request URI
   */
  public URI build() {
    return uriBuilder.build(templateVariables);
  }
}
