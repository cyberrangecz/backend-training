package cz.cyberrange.platform.training.service.unit.mapstruct;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.dto.scorereport.ParticipantScoreRowDTO;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.model.enums.TRState;
import cz.cyberrange.platform.training.service.mapping.mapstruct.ScoreReportMapper;
import cz.cyberrange.platform.training.service.services.score.RunWindow;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

/**
 * Unit tests for {@link ScoreReportMapper}.
 *
 * <p>Covers field routing from a run, its resolved span and its trainee into a report row, and the
 * identity fallbacks applied when the trainee did not resolve or carries no full name.
 */
@DisplayName("ScoreReportMapper")
class ScoreReportMapperTest {

  private static final LocalDateTime START = LocalDateTime.of(2026, 1, 1, 0, 0, 0);
  private static final Long TRAINING_RUN_ID = 42L;
  private static final Long USER_REF_ID = 100L;

  private ScoreReportMapper sut;

  private TrainingRun run;
  private TrainingInstance instance;

  @BeforeEach
  void setUp() {
    sut = Mappers.getMapper(ScoreReportMapper.class);

    UserRef participantRef = new UserRef();
    participantRef.setUserRefId(USER_REF_ID);

    run = new TrainingRun();
    run.setId(TRAINING_RUN_ID);
    run.setParticipantRef(participantRef);
    run.setStartTime(START);
    run.setEndTime(START.plusSeconds(10));
    run.setState(TRState.FINISHED);

    instance = new TrainingInstance();
    instance.setEndTime(START.plusHours(1));
  }

  private long toEpochMillis(LocalDateTime dateTime) {
    return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
  }

  @Nested
  @DisplayName("mapToScoreRow(TrainingRun, RunWindow, UserRefDTO)")
  class MapToScoreRow {

    @Test
    @DisplayName(
        "should route identity, timing and state from the run, window and resolved trainee")
    void shouldRouteIdentityTimingAndStateFields() {
      RunWindow window = RunWindow.resolve(run, instance, toEpochMillis(START.plusHours(1)));
      UserRefDTO participant = new UserRefDTO();
      participant.setUserRefSub("jane.doe");
      participant.setUserRefFullName("Jane Doe");
      participant.setMail("jane.doe@example.cz");

      ParticipantScoreRowDTO row = sut.mapToScoreRow(run, window, participant);

      assertEquals(TRAINING_RUN_ID, row.getTrainingRunId());
      assertEquals(USER_REF_ID, row.getUserRefId());
      assertEquals("jane.doe", row.getLogin());
      assertEquals("Jane Doe", row.getName());
      assertEquals("jane.doe@example.cz", row.getMail());
      assertTrue(row.isFinished());
      assertEquals(toEpochMillis(START), row.getStartedAt());
      assertEquals(toEpochMillis(START.plusSeconds(10)), row.getEndedAt());
      assertEquals(10L, row.getDurationSeconds());
    }

    @Test
    @DisplayName("should leave end time and duration null for a still-running window")
    void shouldLeaveEndAndDurationNullForRunningWindow() {
      run.setState(TRState.RUNNING);
      RunWindow window = RunWindow.resolve(run, instance, toEpochMillis(START.plusMinutes(5)));

      ParticipantScoreRowDTO row = sut.mapToScoreRow(run, window, null);

      assertFalse(row.isFinished());
      assertNull(row.getEndedAt());
      assertNull(row.getDurationSeconds());
    }

    @Test
    @DisplayName("should degrade an unresolvable participant to empty login and mail")
    void shouldDegradeUnresolvableParticipantToEmptyLoginAndMail() {
      RunWindow window = RunWindow.resolve(run, instance, toEpochMillis(START.plusHours(1)));

      ParticipantScoreRowDTO row = sut.mapToScoreRow(run, window, null);

      assertEquals("", row.getLogin());
      assertEquals("", row.getMail());
    }

    @Test
    @DisplayName(
        "should fall back an unresolvable participant's name to the bare user reference id")
    void shouldFallBackUnresolvableParticipantNameToUserRefId() {
      RunWindow window = RunWindow.resolve(run, instance, toEpochMillis(START.plusHours(1)));

      ParticipantScoreRowDTO row = sut.mapToScoreRow(run, window, null);

      assertEquals(String.valueOf(USER_REF_ID), row.getName());
    }

    @Test
    @DisplayName("should fall back a participant with no full name to their login")
    void shouldFallBackParticipantWithNoFullNameToLogin() {
      RunWindow window = RunWindow.resolve(run, instance, toEpochMillis(START.plusHours(1)));
      UserRefDTO participant = new UserRefDTO();
      participant.setUserRefSub("jane.doe");
      participant.setUserRefFullName(null);

      ParticipantScoreRowDTO row = sut.mapToScoreRow(run, window, participant);

      assertEquals("jane.doe", row.getName());
    }
  }
}
