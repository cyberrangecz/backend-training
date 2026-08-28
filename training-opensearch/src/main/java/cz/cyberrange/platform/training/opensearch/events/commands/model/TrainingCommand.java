package cz.cyberrange.platform.training.opensearch.events.commands.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * One console command captured on a trainee's sandbox and read back from its OpenSearch audit
 * document, deserialized through the snake-case, textual-date object mapper configured for
 * OpenSearch
 */
@Data
@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrainingCommand {

  /** Timestamp assigned to a command whose logged time is missing or unparsable */
  private static final LocalDateTime EPOCH_START = LocalDateTime.of(1970, 1, 1, 0, 0);

  /** OpenSearch document identifier, assigned when a command is read back from the index */
  @JsonIgnore private String eventId;

  /** Identifier of the sandbox instance the command was executed in */
  @JsonProperty("sandbox_id")
  private String sandboxId;

  /** Moment the command was executed, in UTC, or {@link #EPOCH_START} when unparsable */
  private LocalDateTime timestamp;

  /** Time spent in the training run so far when the command was executed */
  private Duration trainingTime;

  /** Classifies the kind of console command logged */
  @JsonProperty("cmd_type")
  private String cmdType;

  /** The command name: the leading whitespace-delimited token of the logged command line */
  private String command;

  /** Everything after the command name in the logged command line, or empty if there was none */
  private String commandArguments;

  private String hostname;
  private String username;

  /** Working directory the command was executed from */
  private String wd;

  private String ip;

  @JsonProperty("cmd")
  private void deserializeCmd(String command) {
    if (command == null || command.isBlank()) {
      log.warn("Command event has no 'cmd' value; recording an empty command.");
      this.command = "";
      this.commandArguments = "";
      return;
    }
    String trimmedCommand = command.trim();
    String[] parts = trimmedCommand.split("\\s+", 2);
    this.command = parts[0];
    this.commandArguments = parts.length > 1 ? parts[1] : "";
  }

  @JsonProperty("timestamp_str")
  private void deserializeTime(String time) {
    if (time == null || time.isBlank()) {
      log.warn("Command event has no 'timestamp_str' value; falling back to {}.", EPOCH_START);
      this.timestamp = EPOCH_START;
      return;
    }
    try {
      this.timestamp = toUtcDateTime(time.trim());
    } catch (DateTimeParseException e) {
      log.warn(
          "Command event has unparsable 'timestamp_str' value '{}'; falling back to {}.",
          time,
          EPOCH_START,
          e);
      this.timestamp = EPOCH_START;
    }
  }

  // Reads an ISO-8601 date-time as UTC, taking a value that carries no zone to already be UTC
  private static LocalDateTime toUtcDateTime(String time) {
    TemporalAccessor parsed =
        DateTimeFormatter.ISO_DATE_TIME.parseBest(time, OffsetDateTime::from, LocalDateTime::from);
    return parsed instanceof OffsetDateTime zoneQualified
        ? zoneQualified.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime()
        : (LocalDateTime) parsed;
  }
}
