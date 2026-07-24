package cz.cyberrange.platform.training.service.unit.mapstruct;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cz.cyberrange.platform.training.api.dto.event.*;
import cz.cyberrange.platform.training.opensearch.events.commands.model.TrainingCommand;
import cz.cyberrange.platform.training.opensearch.events.training.model.*;
import cz.cyberrange.platform.training.opensearch.events.training.model.enums.EventLevelType;
import cz.cyberrange.platform.training.service.mapping.mapstruct.EventMapper;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

@DisplayName("EventMapper")
class EventMapperTest {

  private static final long TIMESTAMP = 1609459200000L;
  private static final String EVENT_ID = "opensearch-doc-id-abc123";
  private static final String SANDBOX_ID = "sandbox-123";
  private static final Long POOL_ID = 1L;
  private static final long TRAINING_DEFINITION_ID = 2L;
  private static final long TRAINING_INSTANCE_ID = 3L;
  private static final long TRAINING_RUN_ID = 4L;
  private static final long TRAINING_TIME = 5000L;
  private static final int ACTUAL_SCORE_IN_LEVEL = 10;
  private static final long LEVEL = 5L;
  private static final long LEVEL_ORDER = 1L;
  private static final long USER_REF_ID = 100L;
  private static final int TOTAL_TRAINING_SCORE = 50;
  private static final int TOTAL_ASSESSMENT_SCORE = 20;

  private EventMapper sut;

  @BeforeEach
  void setUp() {
    sut = Mappers.getMapper(EventMapper.class);
  }

  private <B extends AbstractAuditPOJO.AbstractAuditPOJOBuilder<?, ?>> B commonBuilder(B builder) {
    return (B)
        builder
            .eventId(EVENT_ID)
            .timestamp(TIMESTAMP)
            .sandboxId(SANDBOX_ID)
            .poolId(POOL_ID)
            .trainingDefinitionId(TRAINING_DEFINITION_ID)
            .trainingInstanceId(TRAINING_INSTANCE_ID)
            .trainingRunId(TRAINING_RUN_ID)
            .trainingTime(TRAINING_TIME)
            .actualScoreInLevel(ACTUAL_SCORE_IN_LEVEL)
            .level(LEVEL)
            .levelOrder(LEVEL_ORDER)
            .userRefId(USER_REF_ID)
            .totalTrainingScore(TOTAL_TRAINING_SCORE)
            .totalAssessmentScore(TOTAL_ASSESSMENT_SCORE);
  }

  private void assertCommonFields(AbstractAuditPOJO event, TrainingEventDTO dto) {
    assertEquals(event.getEventId(), dto.getEventId());
    assertEquals(event.getType(), dto.getType());
    assertEquals(
        LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTimestamp()), ZoneId.systemDefault()),
        dto.getTimestamp());
    assertEquals(Duration.ofMillis(event.getTrainingTime()), dto.getTrainingTime());
    assertEquals(event.getSandboxId(), dto.getSandboxId());
    assertEquals(event.getPoolId(), dto.getPoolId());
    assertEquals(event.getTrainingDefinitionId(), dto.getTrainingDefinitionId());
    assertEquals(event.getTrainingInstanceId(), dto.getTrainingInstanceId());
    assertEquals(event.getTrainingRunId(), dto.getTrainingRunId());
    assertEquals(event.getActualScoreInLevel(), dto.getActualScoreInLevel());
    assertEquals(event.getLevel(), dto.getLevel());
    assertEquals(event.getLevelOrder(), dto.getLevelOrder());
    assertEquals(event.getUserRefId(), dto.getUserRefId());
    assertEquals(event.getTotalTrainingScore(), dto.getTotalTrainingScore());
    assertEquals(event.getTotalAssessmentScore(), dto.getTotalAssessmentScore());
  }

  @Nested
  @DisplayName("mapToDTO(TrainingCommand)")
  class MapTrainingCommandToDto {

    @Test
    @DisplayName("should map all fields to CommandEventDTO")
    void shouldMapAllFieldsToCommandEventDto() {
      TrainingCommand command = new TrainingCommand();
      command.setEventId("cmd-doc-id-xyz789");
      command.setSandboxId("cmd-sandbox");
      command.setTimestamp(LocalDateTime.of(2021, 1, 1, 12, 0));
      command.setTrainingTime(Duration.ofMinutes(5));
      command.setCmdType("bash");
      command.setCommand("ls");
      command.setCommandArguments("-la");
      command.setHostname("kali");
      command.setUsername("root");
      command.setWd("/home");
      command.setIp("10.0.0.1");

      CommandEventDTO result = sut.mapToDTO(command);

      assertNotNull(result);
      assertEquals(command.getEventId(), result.getEventId());
      assertEquals(command.getSandboxId(), result.getSandboxId());
      assertEquals(command.getTimestamp(), result.getTimestamp());
      assertEquals(command.getTrainingTime(), result.getTrainingTime());
      assertEquals(command.getCmdType(), result.getCmdType());
      assertEquals(command.getCommand(), result.getCommand());
      assertEquals(command.getCommandArguments(), result.getCommandArguments());
      assertEquals(command.getHostname(), result.getHostname());
      assertEquals(command.getUsername(), result.getUsername());
      assertEquals(command.getWd(), result.getWd());
      assertEquals(command.getIp(), result.getIp());
    }

    @Test
    @DisplayName("should map TrainingCommand with null fields")
    void shouldMapTrainingCommandWithNullFields() {
      TrainingCommand command = new TrainingCommand();

      CommandEventDTO result = sut.mapToDTO(command);

      assertNotNull(result);
      assertNull(result.getEventId());
      assertNull(result.getSandboxId());
      assertNull(result.getTimestamp());
      assertNull(result.getTrainingTime());
      assertNull(result.getCmdType());
      assertNull(result.getCommand());
      assertNull(result.getCommandArguments());
      assertNull(result.getHostname());
      assertNull(result.getUsername());
      assertNull(result.getWd());
      assertNull(result.getIp());
    }
  }

  @Nested
  @DisplayName("mapToListDTO(List<TrainingCommand>)")
  class MapTrainingCommandListToDto {

    @Test
    @DisplayName("should map list of commands to list of DTOs")
    void shouldMapListOfCommandsToListOfDtos() {
      TrainingCommand c1 = new TrainingCommand();
      c1.setCommand("ls");
      TrainingCommand c2 = new TrainingCommand();
      c2.setCommand("pwd");

      List<CommandEventDTO> result = sut.mapToListDTO(List.of(c1, c2));

      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals("ls", result.get(0).getCommand());
      assertEquals("pwd", result.get(1).getCommand());
    }

    @Test
    @DisplayName("should return empty list for empty input")
    void shouldReturnEmptyListForEmptyInput() {
      List<CommandEventDTO> result = sut.mapToListDTO(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      assertNull(sut.mapToListDTO(null));
    }
  }

  @Nested
  @DisplayName("mapToDTO(AbstractAuditPOJO) polymorphic dispatch")
  class MapAuditPojoToDto {

    @Test
    @DisplayName("should map TrainingRunStarted to TrainingRunStartedDTO")
    void shouldMapTrainingRunStarted() {
      TrainingRunStarted event =
          (TrainingRunStarted)
              commonBuilder(TrainingRunStarted.builder().type(TrainingRunStarted.TYPE)).build();

      TrainingEventDTO result = sut.mapToDTO(event);

      assertNotNull(result);
      assertInstanceOf(TrainingRunStartedDTO.class, result);
      assertCommonFields(event, result);
    }

    @Test
    @DisplayName("should map TrainingRunResumed to TrainingRunResumedDTO")
    void shouldMapTrainingRunResumed() {
      TrainingRunResumed event =
          (TrainingRunResumed)
              commonBuilder(TrainingRunResumed.builder().type(TrainingRunResumed.TYPE)).build();

      TrainingEventDTO result = sut.mapToDTO(event);

      assertNotNull(result);
      assertInstanceOf(TrainingRunResumedDTO.class, result);
      assertCommonFields(event, result);
    }

    @Test
    @DisplayName("should map TrainingRunFinished to TrainingRunFinishedDTO")
    void shouldMapTrainingRunFinished() {
      TrainingRunFinished event =
          (TrainingRunFinished)
              commonBuilder(TrainingRunFinished.builder().type(TrainingRunFinished.TYPE))
                  .startTime(1000L)
                  .endTime(2000L)
                  .build();

      TrainingEventDTO result = sut.mapToDTO(event);

      assertNotNull(result);
      assertInstanceOf(TrainingRunFinishedDTO.class, result);
      assertCommonFields(event, result);
      TrainingRunFinishedDTO dto = (TrainingRunFinishedDTO) result;
      assertEquals(event.getStartTime(), dto.getStartTime());
      assertEquals(event.getEndTime(), dto.getEndTime());
    }

    @Test
    @DisplayName("should map LevelStarted to LevelStartedDTO")
    void shouldMapLevelStarted() {
      LevelStarted event =
          (LevelStarted)
              commonBuilder(LevelStarted.builder().type(LevelStarted.TYPE))
                  .levelType(EventLevelType.TRAINING)
                  .levelTitle("Level 1")
                  .maxScore(100)
                  .build();

      TrainingEventDTO result = sut.mapToDTO(event);

      assertNotNull(result);
      assertInstanceOf(LevelStartedDTO.class, result);
      assertCommonFields(event, result);
      LevelStartedDTO dto = (LevelStartedDTO) result;
      assertEquals(event.getLevelType().name(), dto.getLevelType());
      assertEquals(event.getLevelTitle(), dto.getLevelTitle());
      assertEquals(event.getMaxScore(), dto.getMaxScore());
    }

    @Test
    @DisplayName("should map LevelCompleted to LevelCompletedDTO")
    void shouldMapLevelCompleted() {
      LevelCompleted event =
          (LevelCompleted)
              commonBuilder(LevelCompleted.builder().type(LevelCompleted.TYPE))
                  .levelType(EventLevelType.ASSESSMENT)
                  .build();

      TrainingEventDTO result = sut.mapToDTO(event);

      assertNotNull(result);
      assertInstanceOf(LevelCompletedDTO.class, result);
      assertCommonFields(event, result);
      LevelCompletedDTO dto = (LevelCompletedDTO) result;
      assertEquals(event.getLevelType().name(), dto.getLevelType());
    }

    @Test
    @DisplayName("should map CorrectAnswerSubmitted to CorrectAnswerSubmittedDTO")
    void shouldMapCorrectAnswerSubmitted() {
      CorrectAnswerSubmitted event =
          (CorrectAnswerSubmitted)
              commonBuilder(CorrectAnswerSubmitted.builder().type(CorrectAnswerSubmitted.TYPE))
                  .answerContent("correct-flag")
                  .build();

      TrainingEventDTO result = sut.mapToDTO(event);

      assertNotNull(result);
      assertInstanceOf(CorrectAnswerSubmittedDTO.class, result);
      assertCommonFields(event, result);
      CorrectAnswerSubmittedDTO dto = (CorrectAnswerSubmittedDTO) result;
      assertEquals(event.getAnswerContent(), dto.getAnswerContent());
    }

    @Test
    @DisplayName("should map WrongAnswerSubmitted to WrongAnswerSubmittedDTO")
    void shouldMapWrongAnswerSubmitted() {
      WrongAnswerSubmitted event =
          (WrongAnswerSubmitted)
              commonBuilder(WrongAnswerSubmitted.builder().type(WrongAnswerSubmitted.TYPE))
                  .answerContent("wrong-flag")
                  .count(3)
                  .build();

      TrainingEventDTO result = sut.mapToDTO(event);

      assertNotNull(result);
      assertInstanceOf(WrongAnswerSubmittedDTO.class, result);
      assertCommonFields(event, result);
      WrongAnswerSubmittedDTO dto = (WrongAnswerSubmittedDTO) result;
      assertEquals(event.getAnswerContent(), dto.getAnswerContent());
      assertEquals(Long.valueOf(event.getCount()), dto.getCount());
    }

    @Test
    @DisplayName("should map HintTaken to HintTakenDTO")
    void shouldMapHintTaken() {
      HintTaken event =
          (HintTaken)
              commonBuilder(HintTaken.builder().type(HintTaken.TYPE))
                  .hintId(10L)
                  .hintTitle("Hint 1")
                  .hintPenaltyPoints(5)
                  .build();

      TrainingEventDTO result = sut.mapToDTO(event);

      assertNotNull(result);
      assertInstanceOf(HintTakenDTO.class, result);
      assertCommonFields(event, result);
      HintTakenDTO dto = (HintTakenDTO) result;
      assertEquals(event.getHintId(), dto.getHintId());
      assertEquals(event.getHintTitle(), dto.getHintTitle());
      assertEquals(event.getHintPenaltyPoints(), dto.getHintPenaltyPoints());
    }

    @Test
    @DisplayName("should map SolutionDisplayed to SolutionDisplayedDTO")
    void shouldMapSolutionDisplayed() {
      SolutionDisplayed event =
          (SolutionDisplayed)
              commonBuilder(SolutionDisplayed.builder().type(SolutionDisplayed.TYPE))
                  .penaltyPoints(15)
                  .build();

      TrainingEventDTO result = sut.mapToDTO(event);

      assertNotNull(result);
      assertInstanceOf(SolutionDisplayedDTO.class, result);
      assertCommonFields(event, result);
      SolutionDisplayedDTO dto = (SolutionDisplayedDTO) result;
      assertEquals(event.getPenaltyPoints(), dto.getPenaltyPoints());
    }

    @Test
    @DisplayName(
        "should map AssessmentAnswered typed answers to typed DTO carriers per question type")
    void shouldMapAssessmentAnswered() {
      FreeFormEventAnswer freeForm =
          FreeFormEventAnswer.builder()
              .questionId(1L)
              .answer(answerSelection("an answer", true))
              .correct(true)
              .pointsGained(10)
              .build();
      MultipleChoiceEventAnswer multipleChoice =
          MultipleChoiceEventAnswer.builder()
              .questionId(2L)
              .selectedOptions(List.of(answerSelection(0, true), answerSelection(2, false)))
              .correct(false)
              .pointsGained(-2)
              .build();
      ExtendedMatchingEventAnswer extendedMatching =
          ExtendedMatchingEventAnswer.builder()
              .questionId(3L)
              .pairs(Map.of(0, answerSelection(1, false)))
              .build();
      AssessmentAnswered event =
          (AssessmentAnswered)
              commonBuilder(AssessmentAnswered.builder().type(AssessmentAnswered.TYPE))
                  .answers(List.of(freeForm, multipleChoice, extendedMatching))
                  .build();

      TrainingEventDTO result = sut.mapToDTO(event);

      assertNotNull(result);
      assertInstanceOf(AssessmentAnsweredDTO.class, result);
      assertCommonFields(event, result);
      AssessmentAnsweredDTO dto = (AssessmentAnsweredDTO) result;
      assertEquals(3, dto.getAnswers().size());

      assertInstanceOf(FreeFormEventAnswerDTO.class, dto.getAnswers().get(0));
      FreeFormEventAnswerDTO freeFormDto = (FreeFormEventAnswerDTO) dto.getAnswers().get(0);
      assertEquals(1L, freeFormDto.getQuestionId());
      assertEquals("an answer", freeFormDto.getAnswer().getValue());
      assertEquals(Boolean.TRUE, freeFormDto.getAnswer().getCorrect());
      assertEquals(Boolean.TRUE, freeFormDto.getCorrect());
      assertEquals(10, freeFormDto.getPointsGained());

      assertInstanceOf(MultipleChoiceEventAnswerDTO.class, dto.getAnswers().get(1));
      MultipleChoiceEventAnswerDTO multipleChoiceDto =
          (MultipleChoiceEventAnswerDTO) dto.getAnswers().get(1);
      assertEquals(2L, multipleChoiceDto.getQuestionId());
      assertEquals(2, multipleChoiceDto.getSelectedOptions().size());
      assertEquals(0, multipleChoiceDto.getSelectedOptions().get(0).getValue());
      assertEquals(Boolean.TRUE, multipleChoiceDto.getSelectedOptions().get(0).getCorrect());
      assertEquals(2, multipleChoiceDto.getSelectedOptions().get(1).getValue());
      assertEquals(Boolean.FALSE, multipleChoiceDto.getSelectedOptions().get(1).getCorrect());
      assertEquals(Boolean.FALSE, multipleChoiceDto.getCorrect());
      assertEquals(-2, multipleChoiceDto.getPointsGained());

      assertInstanceOf(ExtendedMatchingEventAnswerDTO.class, dto.getAnswers().get(2));
      ExtendedMatchingEventAnswerDTO extendedMatchingDto =
          (ExtendedMatchingEventAnswerDTO) dto.getAnswers().get(2);
      assertEquals(3L, extendedMatchingDto.getQuestionId());
      assertEquals(1, extendedMatchingDto.getPairs().get(0).getValue());
      assertEquals(Boolean.FALSE, extendedMatchingDto.getPairs().get(0).getCorrect());
      assertNull(extendedMatchingDto.getCorrect());
      assertNull(extendedMatchingDto.getPointsGained());
    }

    private <T> AnswerSelection<T> answerSelection(T value, Boolean correct) {
      return AnswerSelection.<T>builder().value(value).correct(correct).build();
    }

    @Test
    @DisplayName("should return null for unknown event type")
    void shouldReturnNullForUnknownEventType() {
      AbstractAuditPOJO unknown = new AbstractAuditPOJO() {};
      assertNull(sut.mapToDTO(unknown));
    }
  }

  @Nested
  @DisplayName("mapToEventListDTO(List<AbstractAuditPOJO>)")
  class MapAuditPojoListToDto {

    @Test
    @DisplayName("should map list of mixed events to list of DTOs")
    void shouldMapListOfMixedEventsToListOfDtos() {
      TrainingRunStarted e1 =
          (TrainingRunStarted)
              commonBuilder(TrainingRunStarted.builder().type(TrainingRunStarted.TYPE)).build();
      TrainingRunFinished e2 =
          (TrainingRunFinished)
              commonBuilder(TrainingRunFinished.builder().type(TrainingRunFinished.TYPE))
                  .startTime(1000L)
                  .endTime(2000L)
                  .build();

      List<TrainingEventDTO> result = sut.mapToEventListDTO(List.of(e1, e2));

      assertNotNull(result);
      assertEquals(2, result.size());
      assertInstanceOf(TrainingRunStartedDTO.class, result.get(0));
      assertInstanceOf(TrainingRunFinishedDTO.class, result.get(1));
    }

    @Test
    @DisplayName("should return empty list for empty input")
    void shouldReturnEmptyListForEmptyInput() {
      List<TrainingEventDTO> result = sut.mapToEventListDTO(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return empty list for null input")
    void shouldReturnEmptyListForNullInput() {
      List<TrainingEventDTO> result = sut.mapToEventListDTO(null);
      assertNotNull(result);
      assertTrue(result.isEmpty());
    }
  }

  @Nested
  @DisplayName("mapTimestamp")
  class MapTimestamp {

    @Test
    @DisplayName("should convert epoch millis to LocalDateTime")
    void shouldConvertEpochMillisToLocalDateTime() {
      long epoch = 1609459200000L;
      LocalDateTime expected =
          LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneId.systemDefault());

      LocalDateTime result = sut.mapTimestamp(epoch);

      assertEquals(expected, result);
    }
  }

  @Nested
  @DisplayName("mapTrainingTime")
  class MapTrainingTime {

    @Test
    @DisplayName("should convert millis to Duration")
    void shouldConvertMillisToDuration() {
      assertEquals(Duration.ofMillis(5000), sut.mapTrainingTime(5000L));
    }
  }
}
