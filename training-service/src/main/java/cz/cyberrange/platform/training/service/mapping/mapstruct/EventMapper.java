package cz.cyberrange.platform.training.service.mapping.mapstruct;

import cz.cyberrange.platform.training.api.dto.event.*;
import cz.cyberrange.platform.training.opensearch.events.commands.model.TrainingCommand;
import cz.cyberrange.platform.training.opensearch.events.training.model.*;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * Translates the documents held in the audit index into their event data transfer objects, covering
 * both console commands and each kind of training event, and dispatching on the concrete event type
 * where the caller holds only the common supertype. Its implementation is generated at compile
 * time.
 */
@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventMapper {

  /**
   * Maps every field of {@link CommandEventDTO} that shares a name with a {@link TrainingCommand}
   * field, and sets {@code type} to the fixed discriminator registered for command events on {@code
   * AbstractEventDTO}.
   *
   * @param command command to map
   * @return the mapped DTO
   */
  @Mapping(target = "type", constant = "COMMAND")
  CommandEventDTO mapToDTO(TrainingCommand command);

  /**
   * Maps each command the same way {@link #mapToDTO(TrainingCommand)} does, preserving input order.
   *
   * @param commands commands to map
   * @return one DTO per input command, in the same order
   */
  List<CommandEventDTO> mapToListDTO(List<TrainingCommand> commands);

  /**
   * Dispatches on the concrete subtype of the given training event and maps it to its own DTO.
   *
   * @param event the training event to map
   * @return the mapped DTO, or {@code null} when the event's subtype is none of the handled ones
   */
  default TrainingEventDTO mapToDTO(AbstractAuditPOJO event) {
    if (event instanceof AssessmentAnswered) {
      return mapToDTO((AssessmentAnswered) event);
    } else if (event instanceof CorrectAnswerSubmitted) {
      return mapToDTO((CorrectAnswerSubmitted) event);
    } else if (event instanceof HintTaken) {
      return mapToDTO((HintTaken) event);
    } else if (event instanceof LevelCompleted) {
      return mapToDTO((LevelCompleted) event);
    } else if (event instanceof LevelStarted) {
      return mapToDTO((LevelStarted) event);
    } else if (event instanceof SolutionDisplayed) {
      return mapToDTO((SolutionDisplayed) event);
    } else if (event instanceof TrainingRunFinished) {
      return mapToDTO((TrainingRunFinished) event);
    } else if (event instanceof TrainingRunResumed) {
      return mapToDTO((TrainingRunResumed) event);
    } else if (event instanceof TrainingRunStarted) {
      return mapToDTO((TrainingRunStarted) event);
    } else if (event instanceof WrongAnswerSubmitted) {
      return mapToDTO((WrongAnswerSubmitted) event);
    }
    return null;
  }

  /**
   * Maps each event the same way {@link #mapToDTO(AbstractAuditPOJO)} does, preserving input order.
   *
   * @param events the training events to map, possibly null
   * @return one DTO per input event, in the same order, or an empty list when {@code events} is
   *     null
   */
  default List<TrainingEventDTO> mapToEventListDTO(List<AbstractAuditPOJO> events) {
    if (events == null) {
      return new ArrayList<>();
    }
    return events.stream().map(this::mapToDTO).collect(Collectors.toList());
  }

  AssessmentAnsweredDTO mapToDTO(AssessmentAnswered event);

  /**
   * Dispatches on the concrete subtype of the given event answer and maps it to its own DTO.
   *
   * @param answer the event answer to map
   * @return the mapped DTO, or {@code null} when the answer's subtype is none of the handled ones
   */
  default EventAnswerDTO mapAnswer(EventAnswer answer) {
    if (answer instanceof FreeFormEventAnswer) {
      return mapAnswer((FreeFormEventAnswer) answer);
    } else if (answer instanceof MultipleChoiceEventAnswer) {
      return mapAnswer((MultipleChoiceEventAnswer) answer);
    } else if (answer instanceof ExtendedMatchingEventAnswer) {
      return mapAnswer((ExtendedMatchingEventAnswer) answer);
    }
    return null;
  }

  FreeFormEventAnswerDTO mapAnswer(FreeFormEventAnswer answer);

  MultipleChoiceEventAnswerDTO mapAnswer(MultipleChoiceEventAnswer answer);

  ExtendedMatchingEventAnswerDTO mapAnswer(ExtendedMatchingEventAnswer answer);

  AnswerSelectionDTO<String> mapFreeFormSelection(AnswerSelection<String> selection);

  AnswerSelectionDTO<Integer> mapOptionSelection(AnswerSelection<Integer> selection);

  CorrectAnswerSubmittedDTO mapToDTO(CorrectAnswerSubmitted event);

  HintTakenDTO mapToDTO(HintTaken event);

  LevelCompletedDTO mapToDTO(LevelCompleted event);

  LevelStartedDTO mapToDTO(LevelStarted event);

  SolutionDisplayedDTO mapToDTO(SolutionDisplayed event);

  TrainingRunFinishedDTO mapToDTO(TrainingRunFinished event);

  TrainingRunResumedDTO mapToDTO(TrainingRunResumed event);

  TrainingRunStartedDTO mapToDTO(TrainingRunStarted event);

  WrongAnswerSubmittedDTO mapToDTO(WrongAnswerSubmitted event);

  default LocalDateTime mapTimestamp(long timestamp) {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
  }

  default Duration mapTrainingTime(long trainingTime) {
    return Duration.ofMillis(trainingTime);
  }
}
