package cz.cyberrange.platform.training.opensearch.events.training.logging.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Exception thrown when an error occurs during serialization of log event or query from OpenSearch.
 */
public class OpenSearchSerializeException extends JsonProcessingException {

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
