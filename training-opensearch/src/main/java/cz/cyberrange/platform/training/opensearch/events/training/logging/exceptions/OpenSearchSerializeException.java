package cz.cyberrange.platform.training.opensearch.events.training.logging.exceptions;

import tools.jackson.core.JacksonException;

/**
 * Exception thrown when an error occurs during serialization of log event or query from OpenSearch
 */
public class OpenSearchSerializeException extends JacksonException {

  public OpenSearchSerializeException(String message) {
    super(message);
  }

  public OpenSearchSerializeException(String message, Throwable ex) {
    super(message, ex);
  }

  public OpenSearchSerializeException(Throwable ex) {
    super(ex);
  }
}
