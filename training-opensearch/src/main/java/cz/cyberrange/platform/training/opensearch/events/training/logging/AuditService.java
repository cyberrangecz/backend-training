package cz.cyberrange.platform.training.opensearch.events.training.logging;

import static org.springframework.util.Assert.notNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cyberrange.platform.training.opensearch.events.training.logging.exceptions.OpenSearchSerializeException;
import cz.cyberrange.platform.training.opensearch.events.training.model.AbstractAuditPOJO;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

/**
 * Emits training run audit events. An event is stamped with a time and its own type name, then
 * written as a single JSON line to the application log; getting it from there into the audit store
 * is somebody else's job, so a successful call proves only that the line was written.
 */
@Service
public class AuditService {

  private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

  private static final Long DEFAULT_TIMESTAMP_DELAY_MS = 10L;

  private final ObjectMapper objectMapper;

  @Autowired
  public AuditService(@Qualifier("openSearchObjectMapper") ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * Writes the given event as one JSON line, stamping it with the current time nudged forward by
   * the given priority and with the type name registered for its class. The nudge exists because
   * two events emitted in the same millisecond would otherwise be indistinguishable in time: a
   * higher priority pushes an event later, so events emitted back to back keep the order they were
   * emitted in.
   *
   * @param <T> the kind of event being written
   * @param pojoClass the event to write
   * @param priority how many places after the immediate moment to stamp the event; 0 leaves the
   *     time as it is
   * @throws IllegalArgumentException when the given priority is negative
   * @throws OpenSearchSerializeException when the event cannot be turned into JSON
   */
  @SneakyThrows
  public <T extends AbstractAuditPOJO> void saveTrainingRunEvent(
      @NonNull T pojoClass, int priority) {
    if (priority < 0) {
      throw new IllegalArgumentException("Order must be non-negative");
    }
    notNull(pojoClass, "Null class could not be saved via audit method.");
    try {
      pojoClass.setTimestamp(
          System.currentTimeMillis() + (long) priority * DEFAULT_TIMESTAMP_DELAY_MS);
      pojoClass.setType(AbstractAuditPOJO.resolveEventType(pojoClass.getClass()));

      logger.info(objectMapper.writeValueAsString(pojoClass));
    } catch (JsonProcessingException e) {
      logger.error("Failed to serialize audit log entry to JSON", e);
      throw new OpenSearchSerializeException("Failed to serialize audit log entry to JSON", e);
    }
  }

  /**
   * Writes the given event stamped with the current moment, leaving it ahead of anything written
   * alongside it under a priority.
   *
   * @param <T> the kind of event being written
   * @param pojoClass the event to write
   * @throws OpenSearchSerializeException when the event cannot be turned into JSON
   */
  @SneakyThrows
  public <T extends AbstractAuditPOJO> void saveTrainingRunEvent(@NonNull T pojoClass) {
    saveTrainingRunEvent(pojoClass, 0);
  }
}
