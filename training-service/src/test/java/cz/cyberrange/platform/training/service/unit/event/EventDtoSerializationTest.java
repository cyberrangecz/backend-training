package cz.cyberrange.platform.training.service.unit.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cyberrange.platform.training.api.dto.event.CommandEventDTO;
import cz.cyberrange.platform.training.api.dto.event.TrainingRunStartedDTO;
import cz.cyberrange.platform.training.opensearch.events.commands.model.TrainingCommand;
import cz.cyberrange.platform.training.opensearch.events.training.model.AnswerSelection;
import cz.cyberrange.platform.training.opensearch.events.training.model.AssessmentAnswered;
import cz.cyberrange.platform.training.opensearch.events.training.model.EventAnswer;
import cz.cyberrange.platform.training.opensearch.events.training.model.ExtendedMatchingEventAnswer;
import cz.cyberrange.platform.training.opensearch.events.training.model.FreeFormEventAnswer;
import cz.cyberrange.platform.training.opensearch.events.training.model.MultipleChoiceEventAnswer;
import cz.cyberrange.platform.training.opensearch.events.training.model.TrainingRunStarted;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Event id serialization")
class EventDtoSerializationTest {

  private static final String DOCUMENT_ID = "opensearch-doc-id-abc123";

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
  }

  @Nested
  @DisplayName("DTO response serialization")
  class DtoResponseSerialization {

    @Test
    @DisplayName("should expose CommandEventDTO event id under the event_id property")
    void shouldExposeCommandEventDtoIdAsEventId() throws Exception {
      CommandEventDTO dto = new CommandEventDTO();
      dto.setEventId(DOCUMENT_ID);

      JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(dto));

      assertTrue(json.has("event_id"));
      assertEquals(DOCUMENT_ID, json.get("event_id").asText());
      assertFalse(json.has("eventId"));
    }

    @Test
    @DisplayName("should expose TrainingEventDTO event id under the event_id property")
    void shouldExposeTrainingEventDtoIdAsEventId() throws Exception {
      TrainingRunStartedDTO dto = new TrainingRunStartedDTO();
      dto.setEventId(DOCUMENT_ID);

      JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(dto));

      assertTrue(json.has("event_id"));
      assertEquals(DOCUMENT_ID, json.get("event_id").asText());
      assertFalse(json.has("eventId"));
    }
  }

  @Nested
  @DisplayName("Persisted POJO serialization")
  class PersistedPojoSerialization {

    @Test
    @DisplayName("should not serialize event id on a persisted TrainingCommand")
    void shouldNotSerializeEventIdOnTrainingCommand() throws Exception {
      TrainingCommand command = new TrainingCommand();
      command.setEventId(DOCUMENT_ID);
      command.setCommand("ls");

      JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(command));

      assertFalse(json.has("event_id"));
      assertFalse(json.has("eventId"));
    }

    @Test
    @DisplayName("should not serialize event id on a persisted audit event")
    void shouldNotSerializeEventIdOnAuditEvent() throws Exception {
      TrainingRunStarted event =
          (TrainingRunStarted)
              TrainingRunStarted.builder()
                  .type(TrainingRunStarted.TYPE)
                  .eventId(DOCUMENT_ID)
                  .sandboxId("sandbox-123")
                  .build();

      JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(event));

      assertFalse(json.has("event_id"));
      assertFalse(json.has("eventId"));
    }
  }

  @Nested
  @DisplayName("Assessment answer carrier serialization")
  class AssessmentAnswerCarrierSerialization {

    @Test
    @DisplayName("should tag each carrier with its question-type discriminator")
    void shouldTagCarrierWithQuestionTypeDiscriminator() throws Exception {
      assertEquals(
          "FFQ",
          discriminatorOf(
              FreeFormEventAnswer.builder().questionId(1L).answer(selection("x", true)).build()));
      assertEquals(
          "MCQ",
          discriminatorOf(
              MultipleChoiceEventAnswer.builder()
                  .questionId(2L)
                  .selectedOptions(List.of(selection(1, true)))
                  .build()));
      assertEquals(
          "EMI",
          discriminatorOf(
              ExtendedMatchingEventAnswer.builder()
                  .questionId(3L)
                  .pairs(Map.of(0, selection(1, false)))
                  .build()));
    }

    @Test
    @DisplayName("should round-trip a typed answer list back to its concrete subtypes")
    void shouldRoundTripTypedAnswers() throws Exception {
      List<EventAnswer> answers =
          List.of(
              FreeFormEventAnswer.builder()
                  .questionId(1L)
                  .answer(selection("an answer", true))
                  .correct(true)
                  .pointsGained(10)
                  .build(),
              MultipleChoiceEventAnswer.builder()
                  .questionId(2L)
                  .selectedOptions(List.of(selection(0, true), selection(2, false)))
                  .correct(false)
                  .pointsGained(-2)
                  .build(),
              ExtendedMatchingEventAnswer.builder()
                  .questionId(3L)
                  .pairs(Map.of(0, selection(1, null)))
                  .build());

      String json =
          objectMapper
              .writerFor(new TypeReference<List<EventAnswer>>() {})
              .writeValueAsString(answers);
      List<EventAnswer> restored =
          objectMapper.readValue(json, new TypeReference<List<EventAnswer>>() {});

      assertEquals(3, restored.size());
      assertInstanceOf(FreeFormEventAnswer.class, restored.get(0));
      assertEquals(1L, restored.get(0).getQuestionId());
      FreeFormEventAnswer freeForm = (FreeFormEventAnswer) restored.get(0);
      assertEquals("an answer", freeForm.getAnswer().getValue());
      assertEquals(Boolean.TRUE, freeForm.getAnswer().getCorrect());
      assertEquals(Boolean.TRUE, restored.get(0).getCorrect());
      assertEquals(10, restored.get(0).getPointsGained());

      assertInstanceOf(MultipleChoiceEventAnswer.class, restored.get(1));
      MultipleChoiceEventAnswer multipleChoice = (MultipleChoiceEventAnswer) restored.get(1);
      assertEquals(2, multipleChoice.getSelectedOptions().size());
      assertEquals(0, multipleChoice.getSelectedOptions().get(0).getValue());
      assertEquals(Boolean.TRUE, multipleChoice.getSelectedOptions().get(0).getCorrect());
      assertEquals(2, multipleChoice.getSelectedOptions().get(1).getValue());
      assertEquals(Boolean.FALSE, multipleChoice.getSelectedOptions().get(1).getCorrect());
      assertEquals(Boolean.FALSE, restored.get(1).getCorrect());
      assertEquals(-2, restored.get(1).getPointsGained());

      assertInstanceOf(ExtendedMatchingEventAnswer.class, restored.get(2));
      ExtendedMatchingEventAnswer extendedMatching = (ExtendedMatchingEventAnswer) restored.get(2);
      assertEquals(1, extendedMatching.getPairs().get(0).getValue());
      assertNull(extendedMatching.getPairs().get(0).getCorrect());
      assertNull(restored.get(2).getCorrect());
      assertNull(restored.get(2).getPointsGained());
    }

    @Test
    @DisplayName("should serialize each selected option as a value/correct object in list order")
    void shouldSerializeSelectedOptionsAsObjects() throws Exception {
      MultipleChoiceEventAnswer answer =
          MultipleChoiceEventAnswer.builder()
              .questionId(1L)
              .selectedOptions(List.of(selection(0, true), selection(2, false), selection(5, null)))
              .build();

      JsonNode options =
          objectMapper.readTree(objectMapper.writeValueAsString(answer)).get("selected_options");

      assertEquals(0, options.get(0).get("value").asInt());
      assertTrue(options.get(0).get("correct").asBoolean());
      assertEquals(2, options.get(1).get("value").asInt());
      assertFalse(options.get(1).get("correct").asBoolean());
      assertEquals(5, options.get(2).get("value").asInt());
      assertTrue(options.get(2).get("correct").isNull());
    }

    @Test
    @DisplayName(
        "should emit the carrier discriminator when serialized through the event answers field")
    void shouldEmitDiscriminatorThroughEventField() throws Exception {
      AssessmentAnswered event =
          (AssessmentAnswered)
              AssessmentAnswered.builder()
                  .type(AssessmentAnswered.TYPE)
                  .answers(
                      List.of(
                          FreeFormEventAnswer.builder()
                              .questionId(1L)
                              .answer(selection("x", true))
                              .build(),
                          MultipleChoiceEventAnswer.builder()
                              .questionId(2L)
                              .selectedOptions(List.of(selection(1, true)))
                              .build()))
                  .build();

      JsonNode answers =
          objectMapper.readTree(objectMapper.writeValueAsString(event)).get("answers");

      assertEquals("FFQ", answers.get(0).get("type").asText());
      assertEquals("MCQ", answers.get(1).get("type").asText());
    }

    private String discriminatorOf(EventAnswer answer) throws Exception {
      return objectMapper.readTree(objectMapper.writeValueAsString(answer)).get("type").asText();
    }

    private <T> AnswerSelection<T> selection(T value, Boolean correct) {
      return AnswerSelection.<T>builder().value(value).correct(correct).build();
    }
  }
}
