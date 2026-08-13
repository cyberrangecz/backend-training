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
 * Mapper for converting {@link TrainingCommand} entities to {@link CommandEventDTO} data transfer
 * objects. Code is generated during compile time.
 */
@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventMapper {

  @Mapping(target = "type", constant = "COMMAND")
  CommandEventDTO mapToDTO(TrainingCommand command);

  List<CommandEventDTO> mapToListDTO(List<TrainingCommand> commands);

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

  default List<TrainingEventDTO> mapToEventListDTO(List<AbstractAuditPOJO> events) {
    if (events == null) {
      return new ArrayList<>();
    }
    return events.stream().map(this::mapToDTO).collect(Collectors.toList());
  }

  AssessmentAnsweredDTO mapToDTO(AssessmentAnswered event);

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
