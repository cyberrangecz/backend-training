package cz.cyberrange.platform.training.opensearch.events.commands.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrainingCommand {

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
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    this.timestamp = LocalDateTime.parse(time.substring(0, 19), formatter);
  }
}
