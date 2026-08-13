package cz.cyberrange.platform.training.service.unit.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.dto.scorereport.TrainingInstanceScoreReportDTO;
import cz.cyberrange.platform.training.opensearch.events.commands.query.CommandEventsService;
import cz.cyberrange.platform.training.opensearch.events.training.query.TrainingEventsService;
import cz.cyberrange.platform.training.service.facade.ExportImportFacade;
import cz.cyberrange.platform.training.service.mapping.mapstruct.EventMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.ExportImportMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.LevelMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.TrainingDefinitionMapper;
import cz.cyberrange.platform.training.service.services.ExportImportService;
import cz.cyberrange.platform.training.service.services.TrainingDefinitionService;
import cz.cyberrange.platform.training.service.services.UserService;
import cz.cyberrange.platform.training.service.services.api.SandboxApiService;
import cz.cyberrange.platform.training.service.services.score.ScoreReportService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for {@link ExportImportFacade#exportUserScoreFromTrainingInstance(Long)}.
 *
 * <p>Covers only participant resolution and delegation to {@link ScoreReportService}; report
 * assembly itself is covered by {@code ScoreReportServiceTest}.
 */
@DisplayName("ExportImportFacade.exportUserScoreFromTrainingInstance")
class ExportUserScoreFacadeTest {

  private static final Long TRAINING_INSTANCE_ID = 1L;

  @Mock private ExportImportService exportImportService;
  @Mock private TrainingDefinitionService trainingDefinitionService;
  @Mock private SandboxApiService sandboxApiService;
  @Mock private UserService userService;
  @Mock private ExportImportMapper exportImportMapper;
  @Mock private LevelMapper levelMapper;
  @Mock private TrainingDefinitionMapper trainingDefinitionMapper;
  @Mock private ObjectMapper objectMapper;
  @Mock private CommandEventsService commandEventsService;
  @Mock private TrainingEventsService trainingEventsService;
  @Mock private EventMapper eventMapper;
  @Mock private ScoreReportService scoreReportService;

  private ExportImportFacade sut;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    sut =
        new ExportImportFacade(
            exportImportService,
            trainingDefinitionService,
            sandboxApiService,
            userService,
            exportImportMapper,
            levelMapper,
            trainingDefinitionMapper,
            objectMapper,
            commandEventsService,
            trainingEventsService,
            eventMapper,
            scoreReportService);
  }

  private UserRefDTO userRefDTO(long userRefId) {
    UserRefDTO dto = new UserRefDTO();
    dto.setUserRefId(userRefId);
    return dto;
  }

  @Test
  @DisplayName(
      "resolves every participant reference of the instance and delegates to the report service")
  void resolvesParticipantsAndDelegatesToScoreReportService() {
    UserRefDTO userRef10 = userRefDTO(10L);
    UserRefDTO userRef20 = userRefDTO(20L);
    TrainingInstanceScoreReportDTO expectedReport = new TrainingInstanceScoreReportDTO();

    given(scoreReportService.findParticipantRefIds(TRAINING_INSTANCE_ID))
        .willReturn(Set.of(10L, 20L));
    given(userService.getUsersRefDTOByGivenUserIds(anyList()))
        .willReturn(List.of(userRef10, userRef20));

    ArgumentCaptor<Map<Long, UserRefDTO>> participantsCaptor = ArgumentCaptor.forClass(Map.class);
    given(
            scoreReportService.createReport(
                eq(TRAINING_INSTANCE_ID), participantsCaptor.capture(), anyLong()))
        .willReturn(expectedReport);

    TrainingInstanceScoreReportDTO result =
        sut.exportUserScoreFromTrainingInstance(TRAINING_INSTANCE_ID);

    assertEquals(expectedReport, result);
    assertEquals(Map.of(10L, userRef10, 20L, userRef20), participantsCaptor.getValue());
  }

  @Test
  @DisplayName(
      "calls the batch user lookup exactly once, with every participant reference of the instance")
  void callsBatchUserLookupExactlyOnceWithEveryParticipantReference() {
    given(scoreReportService.findParticipantRefIds(TRAINING_INSTANCE_ID))
        .willReturn(Set.of(1L, 2L, 3L));
    given(userService.getUsersRefDTOByGivenUserIds(anyList())).willReturn(List.of());
    given(scoreReportService.createReport(any(), any(), anyLong()))
        .willReturn(new TrainingInstanceScoreReportDTO());

    sut.exportUserScoreFromTrainingInstance(TRAINING_INSTANCE_ID);

    ArgumentCaptor<List<Long>> refIdsCaptor = ArgumentCaptor.forClass(List.class);
    then(userService).should(times(1)).getUsersRefDTOByGivenUserIds(refIdsCaptor.capture());
    assertThat(refIdsCaptor.getValue()).containsExactlyInAnyOrder(1L, 2L, 3L);
  }

  @Test
  @DisplayName(
      "reads the clock once and forwards an instant bracketed by the call's wall-clock interval")
  void forwardsInstantWithinWallClockBracket() {
    given(scoreReportService.findParticipantRefIds(TRAINING_INSTANCE_ID)).willReturn(Set.of());
    given(userService.getUsersRefDTOByGivenUserIds(anyList())).willReturn(List.of());

    ArgumentCaptor<Long> nowMillisCaptor = ArgumentCaptor.forClass(Long.class);
    given(
            scoreReportService.createReport(
                eq(TRAINING_INSTANCE_ID), any(), nowMillisCaptor.capture()))
        .willReturn(new TrainingInstanceScoreReportDTO());

    long before = System.currentTimeMillis();
    sut.exportUserScoreFromTrainingInstance(TRAINING_INSTANCE_ID);
    long after = System.currentTimeMillis();

    long forwardedInstant = nowMillisCaptor.getValue();
    assertTrue(forwardedInstant >= before && forwardedInstant <= after);
  }

  @Test
  @DisplayName("returns the report produced by the score report service unchanged")
  void returnsReportProducedByScoreReportServiceUnchanged() {
    TrainingInstanceScoreReportDTO expectedReport = new TrainingInstanceScoreReportDTO();
    given(scoreReportService.findParticipantRefIds(TRAINING_INSTANCE_ID)).willReturn(Set.of());
    given(userService.getUsersRefDTOByGivenUserIds(anyList())).willReturn(List.of());
    given(scoreReportService.createReport(any(), any(), anyLong())).willReturn(expectedReport);

    TrainingInstanceScoreReportDTO result =
        sut.exportUserScoreFromTrainingInstance(TRAINING_INSTANCE_ID);

    assertEquals(expectedReport, result);
  }
}
