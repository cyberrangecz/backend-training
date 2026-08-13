package cz.cyberrange.platform.training.service.unit.mapstruct;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cz.cyberrange.platform.training.api.dto.traininginstance.TrainingInstanceBasicDTO;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.service.mapping.mapstruct.TrainingInstanceMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

/**
 * Unit tests for the basic-view mapping methods of {@link TrainingInstanceMapper}.
 *
 * <p>Every field declared by {@link TrainingInstanceBasicDTO} is asserted against a distinct source
 * value, so a target field left without a mapping source fails the test instead of matching a
 * fixture default.
 */
@DisplayName("TrainingInstanceMapper")
class TrainingInstanceMapperTest {

  private static final Long INSTANCE_ID = 10L;
  private static final Long DEFINITION_ID = 77L;
  private static final Long SECOND_INSTANCE_ID = 11L;
  private static final Long SECOND_DEFINITION_ID = 78L;
  private static final String TITLE = "Instance Title";
  private static final String SECOND_TITLE = "Second Instance Title";
  private static final LocalDateTime START_TIME = LocalDateTime.of(2024, 1, 1, 10, 0);
  private static final LocalDateTime END_TIME = LocalDateTime.of(2024, 1, 2, 10, 0);

  private TrainingInstanceMapper sut;
  private TrainingInstance trainingInstanceEntity;

  @BeforeEach
  void setUp() {
    sut = Mappers.getMapper(TrainingInstanceMapper.class);
    trainingInstanceEntity = instanceEntity(INSTANCE_ID, DEFINITION_ID, TITLE);
  }

  private TrainingInstance instanceEntity(Long instanceId, Long definitionId, String title) {
    TrainingDefinition definition = new TrainingDefinition();
    definition.setId(definitionId);

    TrainingInstance instance = new TrainingInstance();
    instance.setId(instanceId);
    instance.setTitle(title);
    instance.setStartTime(START_TIME);
    instance.setEndTime(END_TIME);
    instance.setTrainingDefinition(definition);
    return instance;
  }

  @Nested
  @DisplayName("mapToBasicDTO(TrainingInstance)")
  class MapToBasicDto {

    @Test
    @DisplayName("should map every basic DTO field from the entity")
    void shouldMapEveryBasicDtoField() {
      TrainingInstanceBasicDTO result = sut.mapToBasicDTO(trainingInstanceEntity);

      assertNotNull(result);
      assertEquals(INSTANCE_ID, result.getId());
      assertEquals(TITLE, result.getTitle());
      assertEquals(START_TIME, result.getStartTime());
      assertEquals(END_TIME, result.getEndTime());
      assertEquals(DEFINITION_ID, result.getDefinitionId());
    }

    @Test
    @DisplayName("should source definition id from the associated definition, not the instance")
    void shouldSourceDefinitionIdFromAssociatedDefinition() {
      TrainingInstanceBasicDTO result = sut.mapToBasicDTO(trainingInstanceEntity);

      assertEquals(DEFINITION_ID, result.getDefinitionId());
      assertNotEquals(result.getId(), result.getDefinitionId());
    }

    @Test
    @DisplayName("should leave definition id null when the entity has no definition")
    void shouldLeaveDefinitionIdNullWhenEntityHasNoDefinition() {
      trainingInstanceEntity.setTrainingDefinition(null);

      TrainingInstanceBasicDTO result = sut.mapToBasicDTO(trainingInstanceEntity);

      assertNotNull(result);
      assertEquals(INSTANCE_ID, result.getId());
      assertNull(result.getDefinitionId());
    }

    @Test
    @DisplayName("should map null entity to null")
    void shouldMapNullEntityToNull() {
      assertNull(sut.mapToBasicDTO(null));
    }
  }

  @Nested
  @DisplayName("mapToBasicDtoList(List<TrainingInstance>)")
  class MapToBasicDtoList {

    @Test
    @DisplayName("should map every element preserving order and per-element definition id")
    void shouldMapEveryElementPreservingOrder() {
      TrainingInstance second =
          instanceEntity(SECOND_INSTANCE_ID, SECOND_DEFINITION_ID, SECOND_TITLE);

      List<TrainingInstanceBasicDTO> result =
          sut.mapToBasicDtoList(List.of(trainingInstanceEntity, second));

      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals(INSTANCE_ID, result.get(0).getId());
      assertEquals(DEFINITION_ID, result.get(0).getDefinitionId());
      assertEquals(SECOND_INSTANCE_ID, result.get(1).getId());
      assertEquals(SECOND_DEFINITION_ID, result.get(1).getDefinitionId());
      assertEquals(SECOND_TITLE, result.get(1).getTitle());
    }

    @Test
    @DisplayName("should map empty list to empty list")
    void shouldMapEmptyListToEmptyList() {
      List<TrainingInstanceBasicDTO> result = sut.mapToBasicDtoList(List.of());

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
