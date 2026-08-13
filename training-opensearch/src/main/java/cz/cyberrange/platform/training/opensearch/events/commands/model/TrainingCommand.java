package cz.cyberrange.platform.training.opensearch.events.commands.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrainingCommand {

  // Timestamp assigned to a command whose logged time is missing or unparsable
  private static final LocalDateTime EPOCH_START = LocalDateTime.of(1970, 1, 1, 0, 0);

  // OpenSearch document id of the event
  @JsonIgnore private String eventId;

  @JsonProperty("sandbox_id")
  private String sandboxId;

  private LocalDateTime timestamp;
  private Duration trainingTime;

  @JsonProperty("cmd_type")
  private String cmdType;

  private String command;
  private String commandArguments;
  private String hostname;
  private String username;
  private String wd; // working directory
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
      this.timestamp = LocalDateTime.parse(time.trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    } catch (DateTimeParseException e) {
      log.warn(
          "Command event has unparsable 'timestamp_str' value '{}'; falling back to {}.",
          time,
          EPOCH_START,
          e);
      this.timestamp = EPOCH_START;
    }
  }
}
