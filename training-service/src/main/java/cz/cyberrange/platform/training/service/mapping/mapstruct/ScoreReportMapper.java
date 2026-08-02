package cz.cyberrange.platform.training.service.mapping.mapstruct;

import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.dto.scorereport.ParticipantScoreRowDTO;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.service.services.score.RunWindow;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * The ScoreReportMapper is a utility class to map items into data transfer objects. It provides the
 * implementation of mappings between a training run, the span resolved for it, its trainee, and a
 * row of the training instance score report. Code is generated during compile time.
 */
@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ScoreReportMapper extends ParentMapper {

  /**
   * Maps one training run into its report row, leaving the scored part to be filled from the audit
   * events.
   *
   * @param run the run being reported.
   * @param window the span resolved for the run.
   * @param participant the trainee behind the run, or null when the reference did not resolve.
   * @return the row, with identity, state and timing set.
   */
  @Mapping(source = "run.id", target = "trainingRunId")
  @Mapping(source = "run.participantRef.userRefId", target = "userRefId")
  @Mapping(source = "participant.userRefSub", target = "login")
  @Mapping(source = "participant.userRefFullName", target = "name")
  @Mapping(source = "participant.mail", target = "mail")
  @Mapping(source = "window.finished", target = "finished")
  @Mapping(source = "window.startMillis", target = "startedAt")
  @Mapping(source = "window.endMillis", target = "endedAt")
  @Mapping(source = "window.durationSeconds", target = "durationSeconds")
  ParticipantScoreRowDTO mapToScoreRow(TrainingRun run, RunWindow window, UserRefDTO participant);

  /**
   * Substitutes the identity a trainee lookup did not supply, so that an unresolvable participant
   * still reads as a person rather than as a gap: absent text becomes empty, and a row with no name
   * of any kind falls back to the login and then to the bare user reference. A declarative default
   * cannot serve here, since it is the whole participant that is absent rather than one of its
   * properties.
   *
   * @param row the row being completed.
   */
  @AfterMapping
  default void applyIdentityFallbacks(@MappingTarget ParticipantScoreRowDTO row) {
    if (row.getLogin() == null) {
      row.setLogin("");
    }
    if (row.getMail() == null) {
      row.setMail("");
    }
    if (row.getName() == null) {
      row.setName(row.getLogin().isEmpty() ? String.valueOf(row.getUserRefId()) : row.getLogin());
    }
  }
}
