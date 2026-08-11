package cz.cyberrange.platform.training.service.unit.mapstruct;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import cz.cyberrange.platform.training.api.dto.archive.TrainingInstanceArchiveDTO;
import cz.cyberrange.platform.training.api.dto.archive.TrainingRunArchiveDTO;
import cz.cyberrange.platform.training.api.dto.export.ExportTrainingDefinitionAndLevelsDTO;
import cz.cyberrange.platform.training.api.dto.export.TrainingRunExportDTO;
import cz.cyberrange.platform.training.api.dto.imports.ImportTrainingDefinitionDTO;
import cz.cyberrange.platform.training.api.enums.TDState;
import cz.cyberrange.platform.training.api.enums.TRState;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.service.mapping.mapstruct.EnumMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.ExportImportMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.UserRefMapper;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for {@link ExportImportMapper}.
 *
 * <p>Tests all mapping methods for export/import scenarios. Uses constructor injection with {@link
 * ExportImportMapperImpl} since the mapper uses {@link UserRefMapper}.
 */
@DisplayName("ExportImportMapper")
class ExportImportMapperTest {

  private static final Long ENTITY_ID = 42L;
  private static final Long INSTANCE_DEFINITION_ID = 77L;
  private static final Long PARTICIPANT_REF_ID = 100L;
  private static final String TITLE = "Test Training";
  private static final String DESCRIPTION = "Test Description";
  private static final String[] PREREQUISITES = {"Prereq1", "Prereq2"};
  private static final String[] OUTCOMES = {"Outcome1", "Outcome2"};
  private static final TDState STATE = TDState.RELEASED;
  private static final long ESTIMATED_DURATION = 60L;
  private static final String LAST_EDITED_BY = "admin";
  private static final String ACCESS_TOKEN = "access-token-123";
  private static final boolean LOCAL_ENVIRONMENT = true;
  private static final boolean SHOW_STEPPER_BAR = true;
  private static final boolean BACKWARD_MODE = false;
  private static final LocalDateTime START_TIME = LocalDateTime.of(2024, 1, 1, 10, 0);
  private static final LocalDateTime END_TIME = LocalDateTime.of(2024, 1, 2, 10, 0);
  private static final String EVENT_LOG_REFERENCE = "event-log-ref";
  private static final TRState RUN_STATE = TRState.RUNNING;

  private ExportImportMapper sut;
  private EnumMapper enumMapper;
  private UserRefMapper userRefMapper;

  private TrainingDefinition trainingDefinitionEntity;
  private TrainingInstance trainingInstanceEntity;
  private TrainingRun trainingRunEntity;
  private UserRef participantRef;

  private ImportTrainingDefinitionDTO importDto;
  private ExportTrainingDefinitionAndLevelsDTO exportDto;
  private TrainingInstanceArchiveDTO instanceArchiveDto;
  private TrainingRunExportDTO runExportDto;
  private TrainingRunArchiveDTO runArchiveDto;

  @BeforeEach
  void setUp() {
    userRefMapper = Mappers.getMapper(UserRefMapper.class);
    enumMapper = Mappers.getMapper(EnumMapper.class);
    sut = Mappers.getMapper(ExportImportMapper.class);
    ReflectionTestUtils.setField(sut, "enumMapper", enumMapper);

    participantRef = new UserRef();
    participantRef.setId(1L);
    participantRef.setUserRefId(PARTICIPANT_REF_ID);

    trainingDefinitionEntity = new TrainingDefinition();
    trainingDefinitionEntity.setId(ENTITY_ID);
    trainingDefinitionEntity.setTitle(TITLE);
    trainingDefinitionEntity.setDescription(DESCRIPTION);
    trainingDefinitionEntity.setPrerequisites(PREREQUISITES);
    trainingDefinitionEntity.setOutcomes(OUTCOMES);
    trainingDefinitionEntity.setState(
        cz.cyberrange.platform.training.persistence.model.enums.TDState.RELEASED);
    trainingDefinitionEntity.setEstimatedDuration(ESTIMATED_DURATION);
    trainingDefinitionEntity.setLastEdited(START_TIME);
    trainingDefinitionEntity.setLastEditedBy(LAST_EDITED_BY);
    trainingDefinitionEntity.setCreatedAt(START_TIME);

    trainingInstanceEntity = new TrainingInstance();
    trainingInstanceEntity.setId(ENTITY_ID);
    trainingInstanceEntity.setTitle(TITLE);
    trainingInstanceEntity.setStartTime(START_TIME);
    trainingInstanceEntity.setEndTime(END_TIME);
    trainingInstanceEntity.setAccessToken(ACCESS_TOKEN);
    trainingInstanceEntity.setLocalEnvironment(LOCAL_ENVIRONMENT);
    trainingInstanceEntity.setShowStepperBar(SHOW_STEPPER_BAR);
    trainingInstanceEntity.setBackwardMode(BACKWARD_MODE);
    trainingInstanceEntity.setLastEdited(START_TIME);
    trainingInstanceEntity.setLastEditedBy(LAST_EDITED_BY);

    TrainingDefinition instanceDefinition = new TrainingDefinition();
    instanceDefinition.setId(INSTANCE_DEFINITION_ID);
    trainingInstanceEntity.setTrainingDefinition(instanceDefinition);

    trainingRunEntity = new TrainingRun();
    trainingRunEntity.setId(ENTITY_ID);
    trainingRunEntity.setStartTime(START_TIME);
    trainingRunEntity.setEndTime(END_TIME);
    trainingRunEntity.setEventLogReference(EVENT_LOG_REFERENCE);
    trainingRunEntity.setState(
        cz.cyberrange.platform.training.persistence.model.enums.TRState.RUNNING);
    trainingRunEntity.setParticipantRef(participantRef);

    importDto = new ImportTrainingDefinitionDTO();
    importDto.setTitle(TITLE);
    importDto.setDescription(DESCRIPTION);
    importDto.setPrerequisites(PREREQUISITES);
    importDto.setOutcomes(OUTCOMES);
    importDto.setState(STATE);
    importDto.setEstimatedDuration((int) ESTIMATED_DURATION);

    exportDto = new ExportTrainingDefinitionAndLevelsDTO();
    exportDto.setTitle(TITLE);
    exportDto.setDescription(DESCRIPTION);
    exportDto.setPrerequisites(PREREQUISITES);
    exportDto.setOutcomes(OUTCOMES);
    exportDto.setState(STATE);
    exportDto.setEstimatedDuration((int) ESTIMATED_DURATION);

    instanceArchiveDto = new TrainingInstanceArchiveDTO();
    instanceArchiveDto.setId(ENTITY_ID);
    instanceArchiveDto.setStartTime(START_TIME);
    instanceArchiveDto.setEndTime(END_TIME);
    instanceArchiveDto.setTitle(TITLE);
    instanceArchiveDto.setAccessToken(ACCESS_TOKEN);
    instanceArchiveDto.setLocalEnvironment(LOCAL_ENVIRONMENT);
    instanceArchiveDto.setShowStepperBar(SHOW_STEPPER_BAR);
    instanceArchiveDto.setBackwardMode(BACKWARD_MODE);

    runExportDto = new TrainingRunExportDTO();
    runExportDto.setStartTime(START_TIME);
    runExportDto.setEndTime(END_TIME);
    runExportDto.setEventLogReference(EVENT_LOG_REFERENCE);
    runExportDto.setState(RUN_STATE);

    runArchiveDto = new TrainingRunArchiveDTO();
    runArchiveDto.setId(ENTITY_ID);
    runArchiveDto.setStartTime(START_TIME);
    runArchiveDto.setEndTime(END_TIME);
    runArchiveDto.setEventLogReference(EVENT_LOG_REFERENCE);
    runArchiveDto.setState(RUN_STATE);
    runArchiveDto.setParticipantRefId(PARTICIPANT_REF_ID);
  }

  @Nested
  @DisplayName("mapToDTO(TrainingDefinition)")
  class MapToDtoTrainingDefinition {

    @Test
    @DisplayName("should map training definition entity to export DTO")
    void shouldMapTrainingDefinitionEntityToExportDto() {
      ExportTrainingDefinitionAndLevelsDTO result = sut.mapToDTO(trainingDefinitionEntity);

      assertNotNull(result);
      assertEquals(trainingDefinitionEntity.getTitle(), result.getTitle());
      assertEquals(trainingDefinitionEntity.getDescription(), result.getDescription());
      assertEquals(
          trainingDefinitionEntity.getPrerequisites().length, result.getPrerequisites().length);
      assertEquals(trainingDefinitionEntity.getOutcomes().length, result.getOutcomes().length);
      assertEquals(enumMapper.mapTDState(trainingDefinitionEntity.getState()), result.getState());
      assertEquals(trainingDefinitionEntity.getEstimatedDuration(), result.getEstimatedDuration());
    }

    @Test
    @DisplayName("should map entity with null fields")
    void shouldMapEntityWithNullFields() {
      trainingDefinitionEntity.setDescription(null);
      trainingDefinitionEntity.setPrerequisites(null);
      trainingDefinitionEntity.setOutcomes(null);

      ExportTrainingDefinitionAndLevelsDTO result = sut.mapToDTO(trainingDefinitionEntity);

      assertNotNull(result);
      assertNull(result.getDescription());
      assertNull(result.getPrerequisites());
      assertNull(result.getOutcomes());
    }

    @Test
    @DisplayName("should map entity with empty fields")
    void shouldMapEntityWithEmptyFields() {
      trainingDefinitionEntity.setDescription("");
      trainingDefinitionEntity.setPrerequisites(new String[] {});
      trainingDefinitionEntity.setOutcomes(new String[] {});

      ExportTrainingDefinitionAndLevelsDTO result = sut.mapToDTO(trainingDefinitionEntity);

      assertNotNull(result);
      assertEquals("", result.getDescription());
    }
  }

  @Nested
  @DisplayName("mapToEntity(ImportTrainingDefinitionDTO)")
  class MapToEntityImportDto {

    @Test
    @DisplayName("should map import DTO to training definition entity")
    void shouldMapImportDtoToTrainingDefinitionEntity() {
      TrainingDefinition result = sut.mapToEntity(importDto);

      assertNotNull(result);
      assertEquals(importDto.getTitle(), result.getTitle());
      assertEquals(importDto.getDescription(), result.getDescription());
      assertEquals(importDto.getPrerequisites().length, result.getPrerequisites().length);
      assertEquals(importDto.getOutcomes().length, result.getOutcomes().length);
      assertEquals(enumMapper.mapTDState(importDto.getState()), result.getState());
      assertEquals((long) importDto.getEstimatedDuration(), result.getEstimatedDuration());
    }

    @Test
    @DisplayName("should map DTO with null fields")
    void shouldMapDtoWithNullFields() {
      importDto.setDescription(null);
      importDto.setPrerequisites(null);
      importDto.setOutcomes(null);

      TrainingDefinition result = sut.mapToEntity(importDto);

      assertNotNull(result);
      assertNull(result.getDescription());
      assertNull(result.getPrerequisites());
      assertNull(result.getOutcomes());
    }

    @Test
    @DisplayName("should map DTO with empty fields")
    void shouldMapDtoWithEmptyFields() {
      importDto.setDescription("");
      importDto.setPrerequisites(new String[] {});
      importDto.setOutcomes(new String[] {});

      TrainingDefinition result = sut.mapToEntity(importDto);

      assertNotNull(result);
      assertEquals("", result.getDescription());
    }
  }

  @Nested
  @DisplayName("mapToDTO(TrainingInstance)")
  class MapToDtoTrainingInstance {

    @Test
    @DisplayName("should map training instance entity to archive DTO")
    void shouldMapTrainingInstanceEntityToArchiveDto() {
      trainingInstanceEntity.setOrganizers(new HashSet<>(Set.of(participantRef)));

      TrainingInstanceArchiveDTO result = sut.mapToDTO(trainingInstanceEntity);

      assertNotNull(result);
      assertEquals(trainingInstanceEntity.getId(), result.getId());
      assertEquals(INSTANCE_DEFINITION_ID, result.getDefinitionId());
      assertNotEquals(result.getId(), result.getDefinitionId());
      assertEquals(trainingInstanceEntity.getTitle(), result.getTitle());
      assertEquals(trainingInstanceEntity.getStartTime(), result.getStartTime());
      assertEquals(trainingInstanceEntity.getEndTime(), result.getEndTime());
      assertEquals(trainingInstanceEntity.getAccessToken(), result.getAccessToken());
      assertEquals(trainingInstanceEntity.isLocalEnvironment(), result.isLocalEnvironment());
      assertEquals(trainingInstanceEntity.isShowStepperBar(), result.isShowStepperBar());
      assertEquals(trainingInstanceEntity.isBackwardMode(), result.isBackwardMode());
    }

    @Test
    @DisplayName("should map entity with null organizers")
    void shouldMapEntityWithNullOrganizers() {
      trainingInstanceEntity.setOrganizers(null);

      TrainingInstanceArchiveDTO result = sut.mapToDTO(trainingInstanceEntity);

      assertNotNull(result);
      assertNotNull(result.getOrganizersRefIds());
    }

    @Test
    @DisplayName("should map entity with empty organizers")
    void shouldMapEntityWithEmptyOrganizers() {
      trainingInstanceEntity.setOrganizers(new HashSet<>());

      TrainingInstanceArchiveDTO result = sut.mapToDTO(trainingInstanceEntity);

      assertNotNull(result);
      assertNotNull(result.getOrganizersRefIds());
    }
  }

  @Nested
  @DisplayName("mapToDTO(TrainingRun)")
  class MapToDtoTrainingRun {

    @Test
    @DisplayName("should map training run entity to export DTO")
    void shouldMapTrainingRunEntityToExportDto() {
      TrainingRunExportDTO result = sut.mapToDTO(trainingRunEntity);

      assertNotNull(result);
      assertEquals(trainingRunEntity.getStartTime(), result.getStartTime());
      assertEquals(trainingRunEntity.getEndTime(), result.getEndTime());
      assertEquals(trainingRunEntity.getEventLogReference(), result.getEventLogReference());
      assertEquals(enumMapper.mapTRState(trainingRunEntity.getState()), result.getState());
      assertNotNull(result.getParticipantRef());
    }

    @Test
    @DisplayName("should map entity with null event log reference")
    void shouldMapEntityWithNullEventLogReference() {
      trainingRunEntity.setEventLogReference(null);

      TrainingRunExportDTO result = sut.mapToDTO(trainingRunEntity);

      assertNotNull(result);
      assertNull(result.getEventLogReference());
    }

    @Test
    @DisplayName("should map entity with null participant ref")
    void shouldMapEntityWithNullParticipantRef() {
      trainingRunEntity.setParticipantRef(null);

      TrainingRunExportDTO result = sut.mapToDTO(trainingRunEntity);

      assertNotNull(result);
      assertNull(result.getParticipantRef());
    }
  }

  @Nested
  @DisplayName("mapToArchiveDTO(TrainingRun)")
  class MapToArchiveDtoTrainingRun {

    @Test
    @DisplayName("should map training run entity to archive DTO")
    void shouldMapTrainingRunEntityToArchiveDto() {
      TrainingRunArchiveDTO result = sut.mapToArchiveDTO(trainingRunEntity);

      assertNotNull(result);
      assertEquals(trainingRunEntity.getId(), result.getId());
      assertEquals(trainingRunEntity.getStartTime(), result.getStartTime());
      assertEquals(trainingRunEntity.getEndTime(), result.getEndTime());
      assertEquals(trainingRunEntity.getEventLogReference(), result.getEventLogReference());
      assertEquals(enumMapper.mapTRState(trainingRunEntity.getState()), result.getState());
      assertEquals(
          trainingRunEntity.getParticipantRef().getUserRefId(), result.getParticipantRefId());
    }

    @Test
    @DisplayName("should map entity with null event log reference")
    void shouldMapEntityWithNullEventLogReference() {
      trainingRunEntity.setEventLogReference(null);

      TrainingRunArchiveDTO result = sut.mapToArchiveDTO(trainingRunEntity);

      assertNotNull(result);
      assertNull(result.getEventLogReference());
    }

    @Test
    @DisplayName("should map entity with null participant ref")
    void shouldMapEntityWithNullParticipantRef() {
      trainingRunEntity.setParticipantRef(null);

      TrainingRunArchiveDTO result = sut.mapToArchiveDTO(trainingRunEntity);

      assertNotNull(result);
      assertNull(result.getParticipantRefId());
    }
  }
}
