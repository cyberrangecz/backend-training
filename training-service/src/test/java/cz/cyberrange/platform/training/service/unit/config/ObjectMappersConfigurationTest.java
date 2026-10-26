package cz.cyberrange.platform.training.service.unit.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cz.cyberrange.platform.training.api.dto.cheatingdetection.DetectedForbiddenCommandDTO;
import cz.cyberrange.platform.training.api.dto.event.CommandEventDTO;
import cz.cyberrange.platform.training.api.dto.event.TrainingRunFinishedDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceCreateDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceDTO;
import cz.cyberrange.platform.training.api.exceptions.errors.JavaApiError;
import cz.cyberrange.platform.training.service.config.ObjectMappersConfiguration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

@DisplayName("Date-time wire format of the application mappers")
class ObjectMappersConfigurationTest {

  private static final LocalDateTime WHOLE_SECOND = LocalDateTime.of(2019, 11, 20, 10, 28, 2);
  private static final LocalDateTime MILLISECONDS =
      LocalDateTime.of(2019, 11, 20, 10, 28, 2, 727_000_000);
  private static final LocalDateTime MICROSECONDS =
      LocalDateTime.of(2019, 11, 20, 10, 28, 2, 727_345_000);
  private static final LocalDateTime NANOSECONDS =
      LocalDateTime.of(2019, 11, 20, 10, 28, 2, 727_999_999);

  private JsonMapper jsonMapper;
  private YAMLMapper yamlMapper;

  @BeforeEach
  void setUp() {
    ObjectMappersConfiguration configuration = new ObjectMappersConfiguration();
    JsonMapper.Builder builder = JsonMapper.builder();
    configuration.snakeCaseJsonMapperCustomizer().customize(builder);
    jsonMapper = builder.build();
    yamlMapper = configuration.yamlMapper();
  }

  @Nested
  @DisplayName("JSON output")
  class JsonOutput {

    @Test
    @DisplayName("should pad a whole second to three fractional digits")
    void shouldPadWholeSecond() {
      assertEquals("2019-11-20T10:28:02.000Z", lastEditedOf(WHOLE_SECOND));
    }

    @Test
    @DisplayName("should keep millisecond precision unchanged")
    void shouldKeepMilliseconds() {
      assertEquals("2019-11-20T10:28:02.727Z", lastEditedOf(MILLISECONDS));
    }

    @Test
    @DisplayName("should truncate sub-millisecond digits")
    void shouldTruncateSubMillisecondDigits() {
      assertEquals("2019-11-20T10:28:02.727Z", lastEditedOf(MICROSECONDS));
      assertEquals("2019-11-20T10:28:02.727Z", lastEditedOf(NANOSECONDS));
    }

    @Test
    @DisplayName("should write an event timestamp as a UTC instant")
    void shouldWriteEventTimestampAsUtcInstant() {
      CommandEventDTO event = new CommandEventDTO();
      event.setTimestamp(WHOLE_SECOND);

      assertEquals(
          "2019-11-20T10:28:02.000Z", jsonMapper.valueToTree(event).get("timestamp").asString());
    }

    @Test
    @DisplayName("should write a detected forbidden command time as a UTC instant")
    void shouldWriteForbiddenCommandTimeAsUtcInstant() {
      DetectedForbiddenCommandDTO command = new DetectedForbiddenCommandDTO();
      command.setOccurredAt(MICROSECONDS);

      assertEquals(
          "2019-11-20T10:28:02.727Z",
          jsonMapper.valueToTree(command).get("occurred_at").asString());
    }

    @Test
    @DisplayName("should write a finished run's start and end as UTC instant strings")
    void shouldWriteFinishedRunTimesAsUtcInstantStrings() {
      TrainingRunFinishedDTO finished = new TrainingRunFinishedDTO();
      finished.setStartTime(WHOLE_SECOND);
      finished.setEndTime(MILLISECONDS);

      JsonNode json = jsonMapper.valueToTree(finished);

      assertEquals("2019-11-20T10:28:02.000Z", json.get("start_time").asString());
      assertEquals("2019-11-20T10:28:02.727Z", json.get("end_time").asString());
    }

    private String lastEditedOf(LocalDateTime value) {
      TrainingInstanceDTO instance = new TrainingInstanceDTO();
      instance.setLastEdited(value);
      return jsonMapper.valueToTree(instance).get("last_edited").asString();
    }
  }

  @Nested
  @DisplayName("YAML output")
  class YamlOutput {

    @Test
    @DisplayName("should write a date-time in the same UTC instant form as JSON")
    void shouldWriteSameUtcInstantForm() {
      TrainingInstanceDTO instance = new TrainingInstanceDTO();
      instance.setLastEdited(WHOLE_SECOND);

      assertEquals(
          "2019-11-20T10:28:02.000Z",
          yamlMapper.valueToTree(instance).get("last_edited").asString());
    }

    @Test
    @DisplayName("should ignore a property the target type does not declare when reading")
    void readValue_unknownProperty_ignoresIt() {
      TrainingInstanceCreateDTO read =
          yamlMapper.readValue(
              "title: Instance\nestimated_duration: 60\n", TrainingInstanceCreateDTO.class);

      assertEquals("Instance", read.getTitle());
    }
  }

  @Nested
  @DisplayName("JSON input")
  class JsonInput {

    @Test
    @DisplayName("should read a start time in the fixed millisecond form")
    void shouldReadFixedMillisecondForm() {
      assertEquals(MILLISECONDS, startTimeRead("2019-11-20T10:28:02.727Z"));
    }

    @Test
    @DisplayName("should read a start time without fractional digits")
    void shouldReadWithoutFractionalDigits() {
      assertEquals(WHOLE_SECOND, startTimeRead("2019-11-20T10:28:02Z"));
    }

    @Test
    @DisplayName("should read a microservice error body timestamp in the fixed millisecond form")
    void shouldReadMicroserviceErrorTimestamp() {
      JavaApiError error =
          jsonMapper.readValue(
              "{\"timestamp\": \"2026-10-06T11:34:04.071Z\", \"status\": \"NOT_FOUND\","
                  + " \"message\": \"Missing\", \"errors\": [\"Missing\"], \"path\": \"/groups/1\"}",
              JavaApiError.class);

      assertEquals(LocalDateTime.of(2026, 10, 6, 11, 34, 4, 71_000_000), error.getTimestamp());
    }

    @Test
    @DisplayName("should ignore a property the target type does not declare")
    void readValue_unknownProperty_ignoresIt() {
      TrainingInstanceCreateDTO read =
          jsonMapper.readValue(
              "{\"title\": \"Instance\", \"estimated_duration\": 60}",
              TrainingInstanceCreateDTO.class);

      assertEquals("Instance", read.getTitle());
    }

    private LocalDateTime startTimeRead(String startTime) {
      return jsonMapper
          .readValue("{\"start_time\": \"" + startTime + "\"}", TrainingInstanceCreateDTO.class)
          .getStartTime();
    }
  }
}
