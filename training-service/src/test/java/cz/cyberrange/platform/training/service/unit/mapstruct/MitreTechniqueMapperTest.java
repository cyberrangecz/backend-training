package cz.cyberrange.platform.training.service.unit.mapstruct;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import cz.cyberrange.platform.training.api.dto.technique.MitreTechniqueDTO;
import cz.cyberrange.platform.training.persistence.model.MitreTechnique;
import cz.cyberrange.platform.training.service.mapping.mapstruct.MitreTechniqueMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

/**
 * Unit tests for {@link MitreTechniqueMapper}.
 *
 * <p>Tests all mapping methods including the {@code @Named} qualified mappings that strip IDs for
 * export scenarios. Uses {@link Mappers#getMapper} since the mapper has no Spring dependencies
 * (empty {@code uses}).
 */
@DisplayName("MitreTechniqueMapper")
class MitreTechniqueMapperTest {

  private static final Long ENTITY_ID = 42L;
  private static final String TECHNIQUE_KEY = "T1548.001";
  private static final String OTHER_TECHNIQUE_KEY = "T1548.002";

  private MitreTechniqueMapper sut;

  private MitreTechnique entity;
  private MitreTechniqueDTO dto;

  @BeforeEach
  void setUp() {
    sut = Mappers.getMapper(MitreTechniqueMapper.class);

    entity = new MitreTechnique();
    entity.setId(ENTITY_ID);
    entity.setTechniqueKey(TECHNIQUE_KEY);

    dto = new MitreTechniqueDTO();
    dto.setId(ENTITY_ID);
    dto.setTechniqueKey(TECHNIQUE_KEY);
  }

  @Nested
  @DisplayName("mapToEntity(MitreTechniqueDTO)")
  class MapToEntity {

    @Test
    @DisplayName("should map all DTO fields to entity")
    void shouldMapAllFieldsFromDtoToEntity() {
      MitreTechnique result = sut.mapToEntity(dto);

      assertNotNull(result);
      assertEquals(dto.getId(), result.getId());
      assertEquals(dto.getTechniqueKey(), result.getTechniqueKey());
    }

    @Test
    @DisplayName("should map null techniqueKey to null")
    void shouldMapNullTechniqueKeyToNull() {
      dto.setTechniqueKey(null);

      MitreTechnique result = sut.mapToEntity(dto);

      assertNotNull(result);
      assertNull(result.getTechniqueKey());
    }

    @Test
    @DisplayName("should map DTO with empty properties")
    void shouldMapDtoWithEmptyProperties() {
      dto.setId(null);
      dto.setTechniqueKey("");

      MitreTechnique result = sut.mapToEntity(dto);

      assertNotNull(result);
      assertNull(result.getId());
      assertEquals("", result.getTechniqueKey());
    }
  }

  @Nested
  @DisplayName("mapToDTO(MitreTechnique)")
  class MapToDTO {

    @Test
    @DisplayName("should map all entity fields to DTO")
    void shouldMapAllFieldsFromEntityToDTO() {
      MitreTechniqueDTO result = sut.mapToDTO(entity);

      assertNotNull(result);
      assertEquals(entity.getId(), result.getId());
      assertEquals(entity.getTechniqueKey(), result.getTechniqueKey());
    }

    @Test
    @DisplayName("should map entity with null techniqueKey")
    void shouldMapNullTechniqueKey() {
      entity.setTechniqueKey(null);

      MitreTechniqueDTO result = sut.mapToDTO(entity);

      assertNotNull(result);
      assertNull(result.getTechniqueKey());
    }

    @Test
    @DisplayName("should map entity with empty properties")
    void shouldMapEntityWithEmptyProperties() {
      entity.setId(null);
      entity.setTechniqueKey("");

      MitreTechniqueDTO result = sut.mapToDTO(entity);

      assertNotNull(result);
      assertNull(result.getId());
      assertEquals("", result.getTechniqueKey());
    }
  }

  @Nested
  @DisplayName("mapToDTOIgnoreId(MitreTechnique)")
  class MapToDTOIgnoreId {

    @Test
    @DisplayName("should map techniqueKey but ignore id")
    void shouldMapTechniqueKeyButIgnoreId() {
      MitreTechniqueDTO result = sut.mapToDTOIgnoreId(entity);

      assertNotNull(result);
      assertNull(result.getId());
      assertEquals(entity.getTechniqueKey(), result.getTechniqueKey());
    }

    @Test
    @DisplayName("should produce null id even when entity has id")
    void shouldProduceNullIdWhenEntityHasId() {
      MitreTechniqueDTO result = sut.mapToDTOIgnoreId(entity);

      assertNull(result.getId());
    }

    @Test
    @DisplayName("should map entity with empty techniqueKey and null id")
    void shouldMapEntityWithEmptyTechniqueKeyAndNullId() {
      entity.setId(null);
      entity.setTechniqueKey("");

      MitreTechniqueDTO result = sut.mapToDTOIgnoreId(entity);

      assertNotNull(result);
      assertNull(result.getId());
      assertEquals("", result.getTechniqueKey());
    }
  }

  @Nested
  @DisplayName("mapDTOsToList(Collection)")
  class MapDTOsToList {

    @Test
    @DisplayName("should map list of DTOs to list of entities")
    void shouldMapListOfDTOsToEntities() {
      List<MitreTechniqueDTO> dtos = List.of(dto, dto);

      List<MitreTechnique> result = sut.mapDTOsToList(dtos);

      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals(dto.getId(), result.get(0).getId());
      assertEquals(dto.getTechniqueKey(), result.get(0).getTechniqueKey());
    }

    @Test
    @DisplayName("should return empty list for empty input")
    void shouldReturnEmptyListForEmptyInput() {
      List<MitreTechnique> result = sut.mapDTOsToList(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      List<MitreTechnique> result = sut.mapDTOsToList(null);

      assertNull(result);
    }

    @Test
    @DisplayName("should map DTOs with empty properties")
    void shouldMapDtosWithEmptyProperties() {
      MitreTechniqueDTO emptyDto = new MitreTechniqueDTO();
      emptyDto.setId(null);
      emptyDto.setTechniqueKey("");

      List<MitreTechnique> result = sut.mapDTOsToList(List.of(emptyDto));

      assertNotNull(result);
      assertEquals(1, result.size());
      assertNull(result.get(0).getId());
      assertEquals("", result.get(0).getTechniqueKey());
    }
  }

  @Nested
  @DisplayName("mapToListDTO(Collection)")
  class MapToListDTO {

    @Test
    @DisplayName("should map list of entities to list of DTOs")
    void shouldMapListOfEntitiesToDTOs() {
      List<MitreTechnique> entities = List.of(entity, entity);

      List<MitreTechniqueDTO> result = sut.mapToListDTO(entities);

      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals(entity.getId(), result.get(0).getId());
      assertEquals(entity.getTechniqueKey(), result.get(0).getTechniqueKey());
    }

    @Test
    @DisplayName("should return empty list for empty input")
    void shouldReturnEmptyListForEmptyInput() {
      List<MitreTechniqueDTO> result = sut.mapToListDTO(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      List<MitreTechniqueDTO> result = sut.mapToListDTO(null);

      assertNull(result);
    }

    @Test
    @DisplayName("should map entities with empty properties")
    void shouldMapEntitiesWithEmptyProperties() {
      MitreTechnique emptyEntity = new MitreTechnique();
      emptyEntity.setId(null);
      emptyEntity.setTechniqueKey("");

      List<MitreTechniqueDTO> result = sut.mapToListDTO(List.of(emptyEntity));

      assertNotNull(result);
      assertEquals(1, result.size());
      assertNull(result.get(0).getId());
      assertEquals("", result.get(0).getTechniqueKey());
    }
  }

  @Nested
  @DisplayName("mapToListDTOIgnoreIds(Collection)")
  class MapToListDTOIgnoreIds {

    @Test
    @DisplayName("should collapse entities sharing a technique key into one DTO with a null id")
    void shouldCollapseEntitiesSharingTechniqueKey() {
      List<MitreTechnique> entities = new ArrayList<>();
      entities.add(entity);
      entities.add(entity);

      Set<MitreTechniqueDTO> result = sut.mapToListDTOIgnoreIds(entities);

      assertNotNull(result);
      assertEquals(1, result.size());
      result.forEach(
          dtoItem -> {
            assertNull(dtoItem.getId());
            assertEquals(TECHNIQUE_KEY, dtoItem.getTechniqueKey());
          });
    }

    @Test
    @DisplayName("should map entities with distinct technique keys to distinct DTOs with null ids")
    void shouldMapEntitiesWithDistinctTechniqueKeys() {
      MitreTechnique otherEntity = new MitreTechnique();
      otherEntity.setId(99L);
      otherEntity.setTechniqueKey(OTHER_TECHNIQUE_KEY);

      Set<MitreTechniqueDTO> result = sut.mapToListDTOIgnoreIds(List.of(entity, otherEntity));

      assertNotNull(result);
      assertEquals(2, result.size());
      result.forEach(dtoItem -> assertNull(dtoItem.getId()));
      assertEquals(
          Set.of(TECHNIQUE_KEY, OTHER_TECHNIQUE_KEY),
          result.stream().map(MitreTechniqueDTO::getTechniqueKey).collect(Collectors.toSet()));
    }

    @Test
    @DisplayName("should return empty set for empty input")
    void shouldReturnEmptySetForEmptyInput() {
      Set<MitreTechniqueDTO> result = sut.mapToListDTOIgnoreIds(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      Set<MitreTechniqueDTO> result = sut.mapToListDTOIgnoreIds(null);

      assertNull(result);
    }

    @Test
    @DisplayName("should map entities with empty techniqueKey and strip ids")
    void shouldMapEntitiesWithEmptyTechniqueKeyAndStripIds() {
      MitreTechnique emptyEntity = new MitreTechnique();
      emptyEntity.setId(99L);
      emptyEntity.setTechniqueKey("");

      Set<MitreTechniqueDTO> result = sut.mapToListDTOIgnoreIds(List.of(emptyEntity));

      assertNotNull(result);
      assertEquals(1, result.size());
      result.forEach(
          dtoItem -> {
            assertNull(dtoItem.getId());
            assertEquals("", dtoItem.getTechniqueKey());
          });
    }
  }
}
