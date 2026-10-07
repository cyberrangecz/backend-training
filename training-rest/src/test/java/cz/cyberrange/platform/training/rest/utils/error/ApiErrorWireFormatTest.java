package cz.cyberrange.platform.training.rest.utils.error;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.training.api.exceptions.errors.JavaApiError;
import cz.cyberrange.platform.training.service.config.ObjectMappersConfiguration;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.json.JsonMapper;

@DisplayName("Error body timestamp wire format")
class ApiErrorWireFormatTest {

  private static final LocalDateTime MICROSECONDS =
      LocalDateTime.of(2026, 10, 6, 11, 34, 4, 71_345_000);

  private JsonMapper jsonMapper;

  @BeforeEach
  void setUp() {
    JsonMapper.Builder builder = JsonMapper.builder();
    new ObjectMappersConfiguration().snakeCaseJsonMapperCustomizer().customize(builder);
    jsonMapper = builder.build();
  }

  @Test
  @DisplayName("should write an error timestamp as a UTC instant with 3 fraction digits")
  void shouldWriteErrorTimestampAsUtcInstant() {
    ApiError error = ApiError.of(HttpStatus.NOT_FOUND, "Missing", "Missing", "/path");
    error.setTimestamp(MICROSECONDS);

    assertEquals("2026-10-06T11:34:04.071Z", timestampOf(error));
  }

  @Test
  @DisplayName("should pad a whole second to 3 fraction digits")
  void shouldPadWholeSecond() {
    ApiError error = ApiError.of(HttpStatus.NOT_FOUND, "Missing", "Missing", "/path");
    error.setTimestamp(LocalDateTime.of(2026, 10, 6, 11, 34, 4));

    assertEquals("2026-10-06T11:34:04.000Z", timestampOf(error));
  }

  @Test
  @DisplayName("should write an entity error timestamp in the same form")
  void shouldWriteEntityErrorTimestamp() {
    ApiEntityError error =
        ApiEntityError.of(
            HttpStatus.NOT_FOUND, "Missing", "Missing", "/path", new EntityErrorDetail());
    error.setTimestamp(MICROSECONDS);

    assertEquals("2026-10-06T11:34:04.071Z", timestampOf(error));
  }

  @Test
  @DisplayName("should stamp a new error with the current UTC time")
  void shouldStampCurrentUtcTime() {
    LocalDateTime before = LocalDateTime.now(Clock.systemUTC());

    LocalDateTime stamped = ApiError.of(HttpStatus.NOT_FOUND, "Missing", "Missing").getTimestamp();

    assertTrue(Duration.between(before, stamped).abs().compareTo(Duration.ofSeconds(5)) < 0);
  }

  @Test
  @DisplayName("should write a microservice error timestamp in the same form")
  void shouldWriteMicroserviceErrorTimestamp() {
    ApiError error =
        ApiMicroserviceError.of(
            HttpStatus.FORBIDDEN, "Denied", "Denied", JavaApiError.of("Detail"));
    error.setTimestamp(MICROSECONDS);

    assertEquals("2026-10-06T11:34:04.071Z", timestampOf(error));
  }

  private String timestampOf(ApiError error) {
    return jsonMapper.valueToTree(error).get("timestamp").asString();
  }
}
