package cz.cyberrange.platform.training.opensearch.events.training.logging.exceptions;

/** Exception thrown when an error occurs during querying OpenSearch */
public class OpenSearchQueryException extends RuntimeException {

  public OpenSearchQueryException(String message, Throwable cause) {
    super(message, cause);
  }
}
