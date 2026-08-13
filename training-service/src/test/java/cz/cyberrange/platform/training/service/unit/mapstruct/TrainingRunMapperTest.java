package cz.cyberrange.platform.training.service.unit.mapstruct;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cz.cyberrange.platform.training.api.dto.run.TrainingRunBasicDTO;
import cz.cyberrange.platform.training.api.enums.TRState;
import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.service.mapping.mapstruct.EnumMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.TrainingRunMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for the basic-view mapping methods of {@link TrainingRunMapper}.
 *
 * <p>Every field declared by {@link TrainingRunBasicDTO} is asserted against a distinct source
 * value, so a target field left without a mapping source fails the test instead of matching a
 * fixture default. The four flattened id and order fields are reachable only through the entity's
 * {@code trainingInstance} and {@code currentLevel} relations, both of which the fixture populates.
 */
@DisplayName("TrainingRunMapper")
class TrainingRunMapperTest {

  private static final Long RUN_ID = 5L;
  private static final Long SECOND_RUN_ID = 6L;
  private static final Long INSTANCE_ID = 10L;
  private static final Long SECOND_INSTANCE_ID = 11L;
  private static final Long DEFINITION_ID = 77L;
  private static final Long SECOND_DEFINITION_ID = 78L;
  private static final Long CURRENT_LEVEL_ID = 300L;
  private static final Long SECOND_CURRENT_LEVEL_ID = 301L;
  private static final int CURRENT_LEVEL_ORDER = 3;
  private static final int SECOND_CURRENT_LEVEL_ORDER = 4;
  private static final Long PARTICIPANT_REF_ID = 100L;
  private static final String SANDBOX_INSTANCE_REF_ID = "sandbox-uuid-123";
  private static final LocalDateTime START_TIME = LocalDateTime.of(2024, 1, 1, 10, 0);
  private static final LocalDateTime END_TIME = LocalDateTime.of(2024, 1, 2, 10, 0);

  private TrainingRunMapper sut;
  private TrainingRun trainingRunEntity;

  @BeforeEach
  void setUp() {
    sut = Mappers.getMapper(TrainingRunMapper.class);
    ReflectionTestUtils.setField(sut, "enumMapper", Mappers.getMapper(EnumMapper.class));
    trainingRunEntity =
        runEntity(RUN_ID, INSTANCE_ID, DEFINITION_ID, CURRENT_LEVEL_ID, CURRENT_LEVEL_ORDER);
  }

  private TrainingRun runEntity(
      Long runId, Long instanceId, Long definitionId, Long currentLevelId, int currentLevelOrder) {
    TrainingDefinition definition = new TrainingDefinition();
    definition.setId(definitionId);

    TrainingInstance instance = new TrainingInstance();
    instance.setId(instanceId);
    instance.setTrainingDefinition(definition);

    InfoLevel currentLevel = new InfoLevel();
    currentLevel.setId(currentLevelId);
    currentLevel.setOrder(currentLevelOrder);

    UserRef participantRef = new UserRef();
    participantRef.setUserRefId(PARTICIPANT_REF_ID);

    TrainingRun run = new TrainingRun();
    run.setId(runId);
    run.setStartTime(START_TIME);
    run.setEndTime(END_TIME);
    run.setState(cz.cyberrange.platform.training.persistence.model.enums.TRState.RUNNING);
    run.setParticipantRef(participantRef);
    run.setSandboxInstanceRefId(SANDBOX_INSTANCE_REF_ID);
    run.setTrainingInstance(instance);
    run.setCurrentLevel(currentLevel);
    return run;
  }

  /**
   * Builds a run whose current level is unset, assigning the field directly because {@link
   * TrainingRun#setCurrentLevel} dereferences its argument.
   *
   * @return a run entity with a populated instance relation and no current level.
   */
  private TrainingRun runEntityWithoutCurrentLevel() {
    TrainingRun run =
        runEntity(RUN_ID, INSTANCE_ID, DEFINITION_ID, CURRENT_LEVEL_ID, CURRENT_LEVEL_ORDER);
    ReflectionTestUtils.setField(run, "currentLevel", null);
    return run;
  }

  @Nested
  @DisplayName("mapToBasicDTO(TrainingRun)")
  class MapToBasicDto {

    @Test
    @DisplayName("should map every basic DTO field from the entity")
    void shouldMapEveryBasicDtoField() {
      TrainingRunBasicDTO result = sut.mapToBasicDTO(trainingRunEntity);

      assertNotNull(result);
      assertEquals(RUN_ID, result.getId());
      assertEquals(TRState.RUNNING, result.getState());
      assertEquals(START_TIME, result.getStartTime());
      assertEquals(END_TIME, result.getEndTime());
      assertEquals(SANDBOX_INSTANCE_REF_ID, result.getSandboxInstanceRefId());
      assertNotNull(result.getParticipantRef());
      assertEquals(PARTICIPANT_REF_ID, result.getParticipantRef().getUserRefId());
      assertEquals(INSTANCE_ID, result.getTrainingInstanceId());
      assertEquals(DEFINITION_ID, result.getTrainingDefinitionId());
      assertEquals(CURRENT_LEVEL_ID, result.getCurrentLevelId());
      assertEquals(CURRENT_LEVEL_ORDER, result.getCurrentLevelOrder());
    }

    @Test
    @DisplayName("should source each flattened id from its own relation")
    void shouldSourceEachFlattenedIdFromItsOwnRelation() {
      TrainingRunBasicDTO result = sut.mapToBasicDTO(trainingRunEntity);

      assertNotEquals(result.getId(), result.getTrainingInstanceId());
      assertNotEquals(result.getTrainingInstanceId(), result.getTrainingDefinitionId());
      assertNotEquals(result.getTrainingInstanceId(), result.getCurrentLevelId());
    }

    @Test
    @DisplayName("should leave instance and definition ids null when the run has no instance")
    void shouldLeaveInstanceAndDefinitionIdsNullWhenRunHasNoInstance() {
      trainingRunEntity.setTrainingInstance(null);

      TrainingRunBasicDTO result = sut.mapToBasicDTO(trainingRunEntity);

      assertNotNull(result);
      assertNull(result.getTrainingInstanceId());
      assertNull(result.getTrainingDefinitionId());
      assertEquals(CURRENT_LEVEL_ID, result.getCurrentLevelId());
    }

    @Test
    @DisplayName("should leave definition id null when the instance has no definition")
    void shouldLeaveDefinitionIdNullWhenInstanceHasNoDefinition() {
      trainingRunEntity.getTrainingInstance().setTrainingDefinition(null);

      TrainingRunBasicDTO result = sut.mapToBasicDTO(trainingRunEntity);

      assertNotNull(result);
      assertEquals(INSTANCE_ID, result.getTrainingInstanceId());
      assertNull(result.getTrainingDefinitionId());
    }

    @Test
    @DisplayName("should leave current level id and order null when the run has no current level")
    void shouldLeaveCurrentLevelFieldsNullWhenRunHasNoCurrentLevel() {
      TrainingRunBasicDTO result = sut.mapToBasicDTO(runEntityWithoutCurrentLevel());

      assertNotNull(result);
      assertNull(result.getCurrentLevelId());
      assertNull(result.getCurrentLevelOrder());
      assertEquals(INSTANCE_ID, result.getTrainingInstanceId());
    }

    @Test
    @DisplayName("should map null entity to null")
    void shouldMapNullEntityToNull() {
      assertNull(sut.mapToBasicDTO(null));
    }
  }

  @Nested
  @DisplayName("mapToBasicDtoList(List<TrainingRun>)")
  class MapToBasicDtoList {

    @Test
    @DisplayName("should map every element preserving order and per-element relations")
    void shouldMapEveryElementPreservingOrder() {
      TrainingRun second =
          runEntity(
              SECOND_RUN_ID,
              SECOND_INSTANCE_ID,
              SECOND_DEFINITION_ID,
              SECOND_CURRENT_LEVEL_ID,
              SECOND_CURRENT_LEVEL_ORDER);

      List<TrainingRunBasicDTO> result = sut.mapToBasicDtoList(List.of(trainingRunEntity, second));

      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals(RUN_ID, result.get(0).getId());
      assertEquals(INSTANCE_ID, result.get(0).getTrainingInstanceId());
      assertEquals(DEFINITION_ID, result.get(0).getTrainingDefinitionId());
      assertEquals(CURRENT_LEVEL_ID, result.get(0).getCurrentLevelId());
      assertEquals(CURRENT_LEVEL_ORDER, result.get(0).getCurrentLevelOrder());
      assertEquals(SECOND_RUN_ID, result.get(1).getId());
      assertEquals(SECOND_INSTANCE_ID, result.get(1).getTrainingInstanceId());
      assertEquals(SECOND_DEFINITION_ID, result.get(1).getTrainingDefinitionId());
      assertEquals(SECOND_CURRENT_LEVEL_ID, result.get(1).getCurrentLevelId());
      assertEquals(SECOND_CURRENT_LEVEL_ORDER, result.get(1).getCurrentLevelOrder());
    }

    @Test
    @DisplayName("should map empty list to empty list")
    void shouldMapEmptyListToEmptyList() {
      List<TrainingRunBasicDTO> result = sut.mapToBasicDtoList(List.of());

      assertNotNull(result);
      assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should map null list to null")
    void shouldMapNullListToNull() {
      assertNull(sut.mapToBasicDtoList(null));
    }
  }
}
