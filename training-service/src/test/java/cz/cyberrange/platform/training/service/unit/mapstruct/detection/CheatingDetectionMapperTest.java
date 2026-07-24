package cz.cyberrange.platform.training.service.unit.mapstruct.detection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import cz.cyberrange.platform.training.api.dto.cheatingdetection.CheatingDetectionDTO;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.detection.CheatingDetection;
import cz.cyberrange.platform.training.service.mapping.mapstruct.detection.CheatingDetectionMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

/** Unit tests for {@link CheatingDetectionMapper}. */
@DisplayName("CheatingDetectionMapper")
class CheatingDetectionMapperTest {

  private static final Long ENTITY_ID = 42L;
  private static final Long TRAINING_INSTANCE_ID = 10L;
  private static final String EXECUTED_BY = "John Doe";
  private static final LocalDateTime EXECUTE_TIME = LocalDateTime.of(2022, 1, 1, 5, 55, 23);
  private static final Long PROXIMITY_THRESHOLD = 120L;
  private static final cz.cyberrange.platform.training.api.enums.CheatingDetectionState API_STATE =
      cz.cyberrange.platform.training.api.enums.CheatingDetectionState.RUNNING;
  private static final cz.cyberrange.platform.training.persistence.model.enums
          .CheatingDetectionState
      MODEL_STATE =
          cz.cyberrange.platform.training.persistence.model.enums.CheatingDetectionState.RUNNING;
  private static final Long RESULTS = 20L;

  private CheatingDetectionMapper sut;

  private CheatingDetection entity;
  private CheatingDetectionDTO dto;

  @BeforeEach
  void setUp() {
    sut = Mappers.getMapper(CheatingDetectionMapper.class);

    entity = new CheatingDetection();
    entity.setId(ENTITY_ID);
    entity.setTrainingInstanceId(TRAINING_INSTANCE_ID);
    entity.setExecutedBy(EXECUTED_BY);
    entity.setExecuteTime(EXECUTE_TIME);
    entity.setProximityThreshold(PROXIMITY_THRESHOLD);
    entity.setCurrentState(MODEL_STATE);
    entity.setResults(RESULTS);
    entity.setAnswerSimilarityState(MODEL_STATE);
    entity.setLocationSimilarityState(MODEL_STATE);
    entity.setTimeProximityState(MODEL_STATE);
    entity.setMinimalSolveTimeState(MODEL_STATE);
    entity.setForbiddenCommandsState(MODEL_STATE);
    entity.setNoCommandsState(MODEL_STATE);
    entity.setCommands(new ArrayList<>());

    dto = new CheatingDetectionDTO();
    dto.setId(ENTITY_ID);
    dto.setTrainingInstanceId(TRAINING_INSTANCE_ID);
    dto.setExecutedBy(EXECUTED_BY);
    dto.setExecuteTime(EXECUTE_TIME);
    dto.setProximityThreshold(PROXIMITY_THRESHOLD);
    dto.setCurrentState(API_STATE);
    dto.setResults(RESULTS);
    dto.setAnswerSimilarityState(API_STATE);
    dto.setLocationSimilarityState(API_STATE);
    dto.setTimeProximityState(API_STATE);
    dto.setMinimalSolveTimeState(API_STATE);
    dto.setForbiddenCommandsState(API_STATE);
    dto.setNoCommandsState(API_STATE);
    dto.setForbiddenCommands(new ArrayList<>());
  }

  @Nested
  @DisplayName("mapToEntity(CheatingDetectionDTO)")
  class MapToEntity {

    @Test
    @DisplayName("should map all DTO fields to entity")
    void shouldMapAllFieldsFromDtoToEntity() {
      CheatingDetection result = sut.mapToEntity(dto);

      assertNotNull(result);
      assertEquals(dto.getTrainingInstanceId(), result.getTrainingInstanceId());
      assertEquals(dto.getExecutedBy(), result.getExecutedBy());
      assertEquals(dto.getExecuteTime(), result.getExecuteTime());
      assertEquals(dto.getProximityThreshold(), result.getProximityThreshold());
      assertEquals(dto.getCurrentState().name(), result.getCurrentState().name());
      assertEquals(dto.getResults(), result.getResults());
    }

    @Test
    @DisplayName("should map null executedBy to null")
    void shouldMapNullExecutedByToNull() {
      dto.setExecutedBy(null);

      CheatingDetection result = sut.mapToEntity(dto);

      assertNotNull(result);
      assertNull(result.getExecutedBy());
    }

    @Test
    @DisplayName("should map DTO with empty properties")
    void shouldMapDtoWithEmptyProperties() {
      dto.setId(null);
      dto.setTrainingInstanceId(null);
      dto.setExecutedBy("");
      dto.setExecuteTime(null);
      dto.setProximityThreshold(null);
      dto.setCurrentState(null);
      dto.setResults(null);

      CheatingDetection result = sut.mapToEntity(dto);

      assertNotNull(result);
      assertNull(result.getId());
      assertNull(result.getTrainingInstanceId());
      assertEquals("", result.getExecutedBy());
      assertNull(result.getExecuteTime());
      assertNull(result.getProximityThreshold());
      assertNull(result.getCurrentState());
      assertNull(result.getResults());
    }
  }

  @Nested
  @DisplayName("mapToDTO(CheatingDetection)")
  class MapToDTO {

    @Test
    @DisplayName("should map all entity fields to DTO")
    void shouldMapAllFieldsFromEntityToDto() {
      CheatingDetectionDTO result = sut.mapToDTO(entity);

      assertNotNull(result);
      assertEquals(entity.getId(), result.getId());
      assertEquals(entity.getTrainingInstanceId(), result.getTrainingInstanceId());
      assertEquals(entity.getExecutedBy(), result.getExecutedBy());
      assertEquals(entity.getExecuteTime(), result.getExecuteTime());
      assertEquals(entity.getProximityThreshold(), result.getProximityThreshold());
      assertEquals(entity.getCurrentState().name(), result.getCurrentState().name());
      assertEquals(entity.getResults(), result.getResults());
    }

    @Test
    @DisplayName("should map entity with null executedBy")
    void shouldMapNullExecutedBy() {
      entity.setExecutedBy(null);

      CheatingDetectionDTO result = sut.mapToDTO(entity);

      assertNotNull(result);
      assertNull(result.getExecutedBy());
    }

    @Test
    @DisplayName("should map entity with empty properties")
    void shouldMapEntityWithEmptyProperties() {
      entity.setId(null);
      entity.setTrainingInstanceId(null);
      entity.setExecutedBy("");
      entity.setExecuteTime(null);
      entity.setProximityThreshold(null);
      entity.setCurrentState(null);
      entity.setResults(null);

      CheatingDetectionDTO result = sut.mapToDTO(entity);

      assertNotNull(result);
      assertNull(result.getId());
      assertNull(result.getTrainingInstanceId());
      assertEquals("", result.getExecutedBy());
      assertNull(result.getExecuteTime());
      assertNull(result.getProximityThreshold());
      assertNull(result.getCurrentState());
      assertNull(result.getResults());
    }
  }

  @Nested
  @DisplayName("mapToList(Collection)")
  class MapToList {

    @Test
    @DisplayName("should map list of DTOs to list of entities")
    void shouldMapListOfDtosToEntities() {
      List<CheatingDetectionDTO> dtos = List.of(dto, dto);

      List<CheatingDetection> result = sut.mapToList(dtos);

      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals(dto.getTrainingInstanceId(), result.get(0).getTrainingInstanceId());
      assertEquals(dto.getExecutedBy(), result.get(0).getExecutedBy());
    }

    @Test
    @DisplayName("should return empty list for empty input")
    void shouldReturnEmptyListForEmptyInput() {
      List<CheatingDetection> result = sut.mapToList(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      List<CheatingDetection> result = sut.mapToList(null);

      assertNull(result);
    }

    @Test
    @DisplayName("should map DTOs with empty properties")
    void shouldMapDtosWithEmptyProperties() {
      CheatingDetectionDTO emptyDto = new CheatingDetectionDTO();
      emptyDto.setExecutedBy("");
      emptyDto.setTrainingInstanceId(null);

      List<CheatingDetection> result = sut.mapToList(List.of(emptyDto));

      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals("", result.get(0).getExecutedBy());
      assertNull(result.get(0).getTrainingInstanceId());
    }
  }

  @Nested
  @DisplayName("mapToListDTO(Collection)")
  class MapToListDto {

    @Test
    @DisplayName("should map list of entities to list of DTOs")
    void shouldMapListOfEntitiesToDtos() {
      List<CheatingDetection> entities = List.of(entity, entity);

      List<CheatingDetectionDTO> result = sut.mapToListDTO(entities);

      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals(entity.getTrainingInstanceId(), result.get(0).getTrainingInstanceId());
      assertEquals(entity.getExecutedBy(), result.get(0).getExecutedBy());
    }

    @Test
    @DisplayName("should return empty list for empty input")
    void shouldReturnEmptyListForEmptyInput() {
      List<CheatingDetectionDTO> result = sut.mapToListDTO(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      List<CheatingDetectionDTO> result = sut.mapToListDTO(null);

      assertNull(result);
    }

    @Test
    @DisplayName("should map entities with empty properties")
    void shouldMapEntitiesWithEmptyProperties() {
      CheatingDetection emptyEntity = new CheatingDetection();
      emptyEntity.setExecutedBy("");
      emptyEntity.setTrainingInstanceId(null);

      List<CheatingDetectionDTO> result = sut.mapToListDTO(List.of(emptyEntity));

      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals("", result.get(0).getExecutedBy());
      assertNull(result.get(0).getTrainingInstanceId());
    }
  }

  @Nested
  @DisplayName("mapToSet(Collection)")
  class MapToSet {

    @Test
    @DisplayName("should map list of DTOs to set of entities")
    void shouldMapListOfDtosToSet() {
      CheatingDetectionDTO dto2 = new CheatingDetectionDTO();
      dto2.setId(ENTITY_ID);
      dto2.setTrainingInstanceId(TRAINING_INSTANCE_ID + 1L);
      dto2.setExecutedBy(EXECUTED_BY);
      dto2.setExecuteTime(EXECUTE_TIME);
      dto2.setProximityThreshold(PROXIMITY_THRESHOLD);
      dto2.setCurrentState(API_STATE);
      dto2.setResults(RESULTS);
      dto2.setAnswerSimilarityState(API_STATE);
      dto2.setLocationSimilarityState(API_STATE);
      dto2.setTimeProximityState(API_STATE);
      dto2.setMinimalSolveTimeState(API_STATE);
      dto2.setForbiddenCommandsState(API_STATE);
      dto2.setNoCommandsState(API_STATE);
      dto2.setForbiddenCommands(new ArrayList<>());
      List<CheatingDetectionDTO> dtos = List.of(dto, dto2);

      Set<CheatingDetection> result = sut.mapToSet(dtos);

      assertNotNull(result);
      assertEquals(2, result.size());
    }

    @Test
    @DisplayName("should return empty set for empty input")
    void shouldReturnEmptySetForEmptyInput() {
      Set<CheatingDetection> result = sut.mapToSet(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      Set<CheatingDetection> result = sut.mapToSet(null);

      assertNull(result);
    }
  }

  @Nested
  @DisplayName("mapToSetDTO(Collection)")
  class MapToSetDto {

    @Test
    @DisplayName("should map list of entities to set of DTOs")
    void shouldMapListOfEntitiesToSet() {
      CheatingDetection entity2 = new CheatingDetection();
      entity2.setId(ENTITY_ID);
      entity2.setTrainingInstanceId(TRAINING_INSTANCE_ID + 1L);
      entity2.setExecutedBy(EXECUTED_BY);
      entity2.setExecuteTime(EXECUTE_TIME);
      entity2.setProximityThreshold(PROXIMITY_THRESHOLD);
      entity2.setCurrentState(MODEL_STATE);
      entity2.setResults(RESULTS);
      entity2.setAnswerSimilarityState(MODEL_STATE);
      entity2.setLocationSimilarityState(MODEL_STATE);
      entity2.setTimeProximityState(MODEL_STATE);
      entity2.setMinimalSolveTimeState(MODEL_STATE);
      entity2.setForbiddenCommandsState(MODEL_STATE);
      entity2.setNoCommandsState(MODEL_STATE);
      entity2.setCommands(new ArrayList<>());
      List<CheatingDetection> entities = List.of(entity, entity2);

      Set<CheatingDetectionDTO> result = sut.mapToSetDTO(entities);

      assertNotNull(result);
      assertEquals(2, result.size());
    }

    @Test
    @DisplayName("should return empty set for empty input")
    void shouldReturnEmptySetForEmptyInput() {
      Set<CheatingDetectionDTO> result = sut.mapToSetDTO(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      Set<CheatingDetectionDTO> result = sut.mapToSetDTO(null);

      assertNull(result);
    }
  }

  @Nested
  @DisplayName("mapToPage(Page)")
  class MapToPage {

    @Test
    @DisplayName("should map page of DTOs to page of entities")
    void shouldMapPageOfDtos() {
      List<CheatingDetectionDTO> dtos = List.of(dto);
      Page<CheatingDetectionDTO> page = new PageImpl<>(dtos, PageRequest.of(0, 10), 1);

      Page<CheatingDetection> result = sut.mapToPage(page);

      assertNotNull(result);
      assertEquals(1, result.getTotalElements());
      assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("should return empty page for empty input")
    void shouldReturnEmptyPageForEmptyInput() {
      Page<CheatingDetectionDTO> page = new PageImpl<>(Collections.emptyList());

      Page<CheatingDetection> result = sut.mapToPage(page);

      assertNotNull(result);
      assertEquals(0, result.getTotalElements());
    }
  }

  @Nested
  @DisplayName("mapToPageDTO(Page)")
  class MapToPageDto {

    @Test
    @DisplayName("should map page of entities to page of DTOs")
    void shouldMapPageOfEntitiesToDtos() {
      List<CheatingDetection> entities = List.of(entity);
      Page<CheatingDetection> page = new PageImpl<>(entities, PageRequest.of(0, 10), 1);

      Page<CheatingDetectionDTO> result = sut.mapToPageDTO(page);

      assertNotNull(result);
      assertEquals(1, result.getTotalElements());
      assertEquals(1, result.getContent().size());
      assertEquals(entity.getExecutedBy(), result.getContent().get(0).getExecutedBy());
    }

    @Test
    @DisplayName("should return empty page for empty input")
    void shouldReturnEmptyPageForEmptyInput() {
      Page<CheatingDetection> page = new PageImpl<>(Collections.emptyList());

      Page<CheatingDetectionDTO> result = sut.mapToPageDTO(page);

      assertNotNull(result);
      assertEquals(0, result.getTotalElements());
    }
  }

  @Nested
  @DisplayName("mapToPageResultResource(Page)")
  class MapToPageResultResource {

    @Test
    @DisplayName("should map page to PageResultResource with pagination")
    void shouldMapPageToPageResultResourceWithPagination() {
      List<CheatingDetection> entities = List.of(entity);
      Page<CheatingDetection> page = new PageImpl<>(entities, PageRequest.of(0, 10), 1);

      PageResultResource<CheatingDetectionDTO> result = sut.mapToPageResultResource(page);

      assertNotNull(result);
      assertNotNull(result.getContent());
      assertNotNull(result.getPagination());
      assertEquals(1, result.getContent().size());
      assertEquals(1, result.getPagination().getTotalElements());
      assertEquals(0, result.getPagination().getNumber());
      assertEquals(10, result.getPagination().getSize());
    }

    @Test
    @DisplayName("should return empty PageResultResource for empty page")
    void shouldReturnEmptyPageResultResourceForEmptyPage() {
      Page<CheatingDetection> page =
          new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

      PageResultResource<CheatingDetectionDTO> result = sut.mapToPageResultResource(page);

      assertNotNull(result);
      assertNotNull(result.getContent());
      assertEquals(0, result.getContent().size());
      assertEquals(0, result.getPagination().getTotalElements());
    }
  }
}
