package cz.cyberrange.platform.training.service.unit.mapstruct;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cz.cyberrange.platform.training.api.dto.export.HintExportDTO;
import cz.cyberrange.platform.training.api.dto.hint.HintBasicDTO;
import cz.cyberrange.platform.training.api.dto.hint.HintDTO;
import cz.cyberrange.platform.training.api.dto.hint.TakenHintDTO;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.Hint;
import cz.cyberrange.platform.training.persistence.model.HintInfo;
import cz.cyberrange.platform.training.service.mapping.mapstruct.HintMapper;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Unit tests for {@link HintMapper}.
 *
 * <p>Tests all mapping methods including field renames (hintId→id, hintContent→content,
 * hintTitle→title), HintExportDTO, collections, Optional, Page, and PageResultResource. Uses {@link
 * Mappers#getMapper} since the mapper has no Spring dependencies (empty {@code uses}).
 */
@DisplayName("HintMapper")
class HintMapperTest {

  private static final Long ENTITY_ID = 42L;
  private static final String ENTITY_TITLE = "Test Hint";
  private static final String ENTITY_CONTENT = "This is hint content";
  private static final int ENTITY_ORDER = 1;
  private static final int ENTITY_PENALTY = 10;

  private HintMapper sut;

  private Hint entity;
  private HintDTO dto;

  @BeforeEach
  void setUp() {
    sut = Mappers.getMapper(HintMapper.class);

    entity = new Hint();
    entity.setId(ENTITY_ID);
    entity.setTitle(ENTITY_TITLE);
    entity.setContent(ENTITY_CONTENT);
    entity.setHintPenalty(ENTITY_PENALTY);
    entity.setOrder(ENTITY_ORDER);

    dto = new HintDTO();
    dto.setId(ENTITY_ID);
    dto.setTitle(ENTITY_TITLE);
    dto.setContent(ENTITY_CONTENT);
    dto.setHintPenalty(ENTITY_PENALTY);
    dto.setOrder(ENTITY_ORDER);
  }

  // --- mapToEntity ---

  @Nested
  @DisplayName("mapToEntity(HintDTO)")
  class MapToEntity {

    @Test
    @DisplayName("should map all DTO fields to entity")
    void shouldMapAllFieldsFromDtoToEntity() {
      Hint result = sut.mapToEntity(dto);

      assertNotNull(result);
      assertEquals(dto.getId(), result.getId());
      assertEquals(dto.getTitle(), result.getTitle());
      assertEquals(dto.getContent(), result.getContent());
      assertEquals(dto.getHintPenalty(), result.getHintPenalty());
      assertEquals(dto.getOrder(), result.getOrder());
    }

    @Test
    @DisplayName("should map missing title to empty string")
    void shouldMapMissingTitleToEmptyString() {
      dto.setTitle(null);

      Hint result = sut.mapToEntity(dto);

      assertNotNull(result);
      assertEquals("", result.getTitle());
    }

    @Test
    @DisplayName("should map DTO with empty properties")
    void shouldMapDtoWithEmptyProperties() {
      dto.setId(null);
      dto.setTitle("");
      dto.setContent("");
      dto.setHintPenalty(null);

      Hint result = sut.mapToEntity(dto);

      assertNotNull(result);
      assertNull(result.getId());
      assertEquals("", result.getTitle());
      assertEquals("", result.getContent());
      assertNull(result.getHintPenalty());
    }
  }

  // --- mapToDTO ---

  @Nested
  @DisplayName("mapToDTO(Hint)")
  class MapToDto {

    @Test
    @DisplayName("should map all entity fields to DTO")
    void shouldMapAllFieldsFromEntityToDto() {
      HintDTO result = sut.mapToDTO(entity);

      assertNotNull(result);
      assertEquals(entity.getId(), result.getId());
      assertEquals(entity.getTitle(), result.getTitle());
      assertEquals(entity.getContent(), result.getContent());
      assertEquals(entity.getHintPenalty(), result.getHintPenalty());
      assertEquals(entity.getOrder(), result.getOrder());
    }

    @Test
    @DisplayName("should map entity with missing title to empty string")
    void shouldMapMissingTitleToEmptyString() {
      entity.setTitle(null);

      HintDTO result = sut.mapToDTO(entity);

      assertNotNull(result);
      assertEquals("", result.getTitle());
    }

    @Test
    @DisplayName("should map entity with empty properties")
    void shouldMapEntityWithEmptyProperties() {
      entity.setId(null);
      entity.setTitle("");
      entity.setContent("");
      entity.setHintPenalty(null);

      HintDTO result = sut.mapToDTO(entity);

      assertNotNull(result);
      assertNull(result.getId());
      assertEquals("", result.getTitle());
      assertEquals("", result.getContent());
      assertNull(result.getHintPenalty());
    }
  }

  // --- mapToBasicDTO ---

  @Nested
  @DisplayName("mapToBasicDTO(Hint)")
  class MapToBasicDto {

    @Test
    @DisplayName("should map id, title, hintPenalty from entity")
    void shouldMapIdTitleHintPenaltyFromEntity() {
      HintBasicDTO result = sut.mapToBasicDTO(entity);

      assertNotNull(result);
      assertEquals(entity.getId(), result.getId());
      assertEquals(entity.getTitle(), result.getTitle());
      assertEquals(entity.getHintPenalty(), result.getHintPenalty());
    }

    @Test
    @DisplayName("should not map content or order (basic DTO)")
    void shouldNotMapContentOrOrder() {
      entity.setContent("Should not appear");
      entity.setOrder(99);

      HintBasicDTO result = sut.mapToBasicDTO(entity);

      // HintBasicDTO does not have content or order fields, so we just verify the mapped ones
      assertNotNull(result);
      assertEquals(entity.getTitle(), result.getTitle());
    }
  }

  // --- mapToDTO(HintInfo) -> TakenHintDTO with field renames ---

  @Nested
  @DisplayName("mapToDTO(HintInfo) -> TakenHintDTO field renames")
  class MapHintInfoToTakenHintDto {

    @Test
    @DisplayName("should map hintId to id")
    void shouldMapHintIdToId() {
      HintInfo hintInfo = createHintInfo();

      TakenHintDTO result = sut.mapToDTO(hintInfo);

      assertNotNull(result);
      assertEquals(hintInfo.getHintId(), result.getId());
    }

    @Test
    @DisplayName("should map hintTitle to title")
    void shouldMapHintTitleToTitle() {
      HintInfo hintInfo = createHintInfo();

      TakenHintDTO result = sut.mapToDTO(hintInfo);

      assertNotNull(result);
      assertEquals(hintInfo.getHintTitle(), result.getTitle());
    }

    @Test
    @DisplayName("should map hintContent to content")
    void shouldMapHintContentToContent() {
      HintInfo hintInfo = createHintInfo();

      TakenHintDTO result = sut.mapToDTO(hintInfo);

      assertNotNull(result);
      assertEquals(hintInfo.getHintContent(), result.getContent());
    }

    @Test
    @DisplayName("should map order field")
    void shouldMapOrderField() {
      HintInfo hintInfo = createHintInfo();

      TakenHintDTO result = sut.mapToDTO(hintInfo);

      assertNotNull(result);
      assertEquals(hintInfo.getOrder(), result.getOrder());
    }

    @Test
    @DisplayName("should map hintInfo with all fields null except required")
    void shouldMapHintInfoWithMinimalFields() {
      HintInfo hintInfo = new HintInfo();
      hintInfo.setHintId(0L);
      hintInfo.setHintTitle("");
      hintInfo.setHintContent("");
      hintInfo.setOrder(0);

      TakenHintDTO result = sut.mapToDTO(hintInfo);

      assertNotNull(result);
      assertEquals(0, result.getId().longValue());
      assertEquals("", result.getTitle());
      assertEquals("", result.getContent());
      assertEquals(0, result.getOrder());
    }
  }

  // --- mapToHintExportDTO ---

  @Nested
  @DisplayName("mapToHintExportDTO(Hint)")
  class MapToHintExportDto {

    @Test
    @DisplayName("should map title, content, hintPenalty, order to export DTO")
    void shouldMapTitleContentHintPenaltyOrder() {
      HintExportDTO result = sut.mapToHintExportDTO(entity);

      assertNotNull(result);
      assertEquals(entity.getTitle(), result.getTitle());
      assertEquals(entity.getContent(), result.getContent());
      assertEquals(entity.getHintPenalty(), result.getHintPenalty());
      assertEquals(entity.getOrder(), result.getOrder());
    }

    @Test
    @DisplayName("should map entity with null id (export DTO has no id field)")
    void shouldMapEntityWithNullId() {
      entity.setId(null);

      HintExportDTO result = sut.mapToHintExportDTO(entity);

      assertNotNull(result);
      assertEquals(entity.getTitle(), result.getTitle());
    }

    @Test
    @DisplayName("should map entity with empty content")
    void shouldMapEntityWithEmptyContent() {
      entity.setContent("");

      HintExportDTO result = sut.mapToHintExportDTO(entity);

      assertNotNull(result);
      assertEquals("", result.getContent());
    }
  }

  // --- mapToList ---

  @Nested
  @DisplayName("mapToList(Collection<HintDTO>)")
  class MapToList {

    @Test
    @DisplayName("should map list of DTOs to list of entities")
    void shouldMapListOfDtosToEntities() {
      List<HintDTO> dtos = List.of(dto, dto);

      List<Hint> result = sut.mapToList(dtos);

      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals(dto.getId(), result.get(0).getId());
      assertEquals(dto.getTitle(), result.get(0).getTitle());
    }

    @Test
    @DisplayName("should return empty list for empty input")
    void shouldReturnEmptyListForEmptyInput() {
      List<Hint> result = sut.mapToList(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      List<Hint> result = sut.mapToList(null);

      assertNull(result);
    }

    @Test
    @DisplayName("should map DTOs with empty properties")
    void shouldMapDtosWithEmptyProperties() {
      HintDTO emptyDto = new HintDTO();
      emptyDto.setTitle("");
      emptyDto.setContent("");
      emptyDto.setHintPenalty(null);

      List<Hint> result = sut.mapToList(List.of(emptyDto));

      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals("", result.get(0).getTitle());
    }
  }

  // --- mapToListDTO ---

  @Nested
  @DisplayName("mapToListDTO(Collection<Hint>)")
  class MapToListDto {

    @Test
    @DisplayName("should map list of entities to list of DTOs")
    void shouldMapListOfEntitiesToDtos() {
      List<Hint> entities = List.of(entity, entity);

      List<HintDTO> result = sut.mapToListDTO(entities);

      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals(entity.getId(), result.get(0).getId());
      assertEquals(entity.getTitle(), result.get(0).getTitle());
    }

    @Test
    @DisplayName("should return empty list for empty input")
    void shouldReturnEmptyListForEmptyInput() {
      List<HintDTO> result = sut.mapToListDTO(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      List<HintDTO> result = sut.mapToListDTO(null);

      assertNull(result);
    }

    @Test
    @DisplayName("should map entities with empty properties")
    void shouldMapEntitiesWithEmptyProperties() {
      Hint emptyEntity = new Hint();
      emptyEntity.setTitle("");
      emptyEntity.setContent("");
      emptyEntity.setHintPenalty(null);

      List<HintDTO> result = sut.mapToListDTO(List.of(emptyEntity));

      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals("", result.get(0).getTitle());
    }
  }

  // --- mapToSet ---

  @Nested
  @DisplayName("mapToSet(Collection<HintDTO>)")
  class MapToSet {

    @Test
    @DisplayName("should map collection of DTOs to set of entities")
    void shouldMapCollectionOfDtosToSet() {
      Collection<HintDTO> dtos = List.of(dto, dto);

      Set<Hint> result = sut.mapToSet(dtos);

      assertNotNull(result);
      // Set size depends on equals/hashCode of Hint
      assertTrue(result.size() >= 1);
    }

    @Test
    @DisplayName("should return empty set for empty input")
    void shouldReturnEmptySetForEmptyInput() {
      Set<Hint> result = sut.mapToSet(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      Set<Hint> result = sut.mapToSet(null);

      assertNull(result);
    }
  }

  // --- mapToSetDTO ---

  @Nested
  @DisplayName("mapToSetDTO(Collection<Hint>)")
  class MapToSetDto {

    @Test
    @DisplayName("should map collection of entities to set of DTOs")
    void shouldMapCollectionOfEntitiesToSet() {
      Collection<Hint> entities = List.of(entity, entity);

      Set<HintDTO> result = sut.mapToSetDTO(entities);

      assertNotNull(result);
      // Set size depends on equals/hashCode of HintDTO
      assertTrue(result.size() >= 1);
    }

    @Test
    @DisplayName("should return empty set for empty input")
    void shouldReturnEmptySetForEmptyInput() {
      Set<HintDTO> result = sut.mapToSetDTO(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      Set<HintDTO> result = sut.mapToSetDTO(null);

      assertNull(result);
    }
  }

  // --- mapToSetInfoDTO ---

  @Nested
  @DisplayName("mapToSetInfoDTO(Collection<HintInfo>)")
  class MapToSetInfoDto {

    @Test
    @DisplayName("should map collection of HintInfo to set of TakenHintDTOs")
    void shouldMapCollectionOfHintInfoToSet() {
      HintInfo hintInfo1 = createHintInfo();
      hintInfo1.setHintId(1L);
      HintInfo hintInfo2 = createHintInfo();
      hintInfo2.setHintId(2L);
      Collection<HintInfo> entities = List.of(hintInfo1, hintInfo2);

      Set<TakenHintDTO> result = sut.mapToSetInfoDTO(entities);

      assertNotNull(result);
      assertEquals(2, result.size());
    }

    @Test
    @DisplayName("should return empty set for empty input")
    void shouldReturnEmptySetForEmptyInput() {
      Set<TakenHintDTO> result = sut.mapToSetInfoDTO(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      Set<TakenHintDTO> result = sut.mapToSetInfoDTO(null);

      assertNull(result);
    }
  }

  // --- mapToOptional(HintDTO) ---

  @Nested
  @DisplayName("mapToOptional(HintDTO)")
  class MapToOptionalFromDto {

    @Test
    @DisplayName("should return Optional of entity when DTO is non-null")
    void shouldReturnOptionalOfEntityWhenDtoIsNonNull() {
      Optional<Hint> result = sut.mapToEntityOptional(dto);

      assertNotNull(result);
      assertTrue(result.isPresent());
      assertEquals(dto.getTitle(), result.get().getTitle());
    }

    @Test
    @DisplayName("should return empty Optional when DTO is null")
    void shouldReturnEmptyOptionalWhenDtoIsNull() {
      Optional<Hint> result = sut.mapToEntityOptional(null);

      assertNotNull(result);
      assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should return present Optional with empty title when DTO title is missing")
    void shouldReturnPresentOptionalWithEmptyTitleWhenDtoTitleMissing() {
      dto.setTitle(null);

      Optional<Hint> result = sut.mapToEntityOptional(dto);

      assertNotNull(result);
      assertTrue(result.isPresent());
      assertEquals("", result.get().getTitle());
    }
  }

  // --- mapToOptional(Hint) ---

  @Nested
  @DisplayName("mapToOptional(Hint)")
  class MapToOptionalFromEntity {

    @Test
    @DisplayName("should return Optional of DTO when entity is non-null")
    void shouldReturnOptionalOfDtoWhenEntityIsNonNull() {
      Optional<HintDTO> result = sut.mapToDTOOptional(entity);

      assertNotNull(result);
      assertTrue(result.isPresent());
      assertEquals(entity.getTitle(), result.get().getTitle());
    }

    @Test
    @DisplayName("should return empty Optional when entity is null")
    void shouldReturnEmptyOptionalWhenEntityIsNull() {
      Optional<HintDTO> result = sut.mapToDTOOptional(null);

      assertNotNull(result);
      assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should return present Optional with empty title when entity title is missing")
    void shouldReturnPresentOptionalWithEmptyTitleWhenEntityTitleMissing() {
      entity.setTitle(null);

      Optional<HintDTO> result = sut.mapToDTOOptional(entity);

      assertNotNull(result);
      assertTrue(result.isPresent());
      assertEquals("", result.get().getTitle());
    }
  }

  // --- mapToPageDTO ---

  @Nested
  @DisplayName("mapToPageDTO(Page<Hint>)")
  class MapToPageDto {

    @Test
    @DisplayName("should map Page of entities to Page of DTOs")
    void shouldMapPageOfEntitiesToPageOfDtos() {
      Pageable pageable = PageRequest.of(0, 10);
      Page<Hint> page = new PageImpl<>(List.of(entity, entity), pageable, 2L);

      Page<HintDTO> result = sut.mapToPageDTO(page);

      assertNotNull(result);
      assertEquals(2, result.getContent().size());
      assertEquals(entity.getId(), result.getContent().get(0).getId());
      assertEquals(0, result.getNumber());
      assertEquals(10, result.getSize());
      assertEquals(2L, result.getTotalElements());
    }

    @Test
    @DisplayName("should return empty page for empty input")
    void shouldReturnEmptyPageForEmptyInput() {
      Pageable pageable = PageRequest.of(0, 10);
      Page<Hint> page = new PageImpl<>(Collections.emptyList(), pageable, 0L);

      Page<HintDTO> result = sut.mapToPageDTO(page);

      assertNotNull(result);
      assertEquals(0, result.getContent().size());
    }
  }

  // --- mapToPage ---

  @Nested
  @DisplayName("mapToPage(Page<HintDTO>)")
  class MapToPage {

    @Test
    @DisplayName("should map Page of DTOs to Page of entities")
    void shouldMapPageOfDtosToPageOfEntities() {
      Pageable pageable = PageRequest.of(0, 10);
      Page<HintDTO> page = new PageImpl<>(List.of(dto, dto), pageable, 2L);

      Page<Hint> result = sut.mapToPage(page);

      assertNotNull(result);
      assertEquals(2, result.getContent().size());
      assertEquals(dto.getId(), result.getContent().get(0).getId());
    }

    @Test
    @DisplayName("should return empty page for empty input")
    void shouldReturnEmptyPageForEmptyInput() {
      Pageable pageable = PageRequest.of(0, 10);
      Page<HintDTO> page = new PageImpl<>(Collections.emptyList(), pageable, 0L);

      Page<Hint> result = sut.mapToPage(page);

      assertNotNull(result);
      assertEquals(0, result.getContent().size());
    }
  }

  // --- mapToPageResultResource ---

  @Nested
  @DisplayName("mapToPageResultResource(Page<Hint>)")
  class MapToPageResultResource {

    @Test
    @DisplayName("should map page to PageResultResource with content and pagination")
    void shouldMapPageToPageResultResourceWithContentAndPagination() {
      Pageable pageable = PageRequest.of(1, 10);
      Page<Hint> page = new PageImpl<>(List.of(entity), pageable, 25L);

      PageResultResource<HintDTO> result = sut.mapToPageResultResource(page);

      assertNotNull(result);
      assertNotNull(result.getContent());
      assertEquals(1, result.getContent().size());
      assertEquals(entity.getId(), result.getContent().get(0).getId());

      assertNotNull(result.getPagination());
      assertEquals(1, result.getPagination().getNumber());
      assertEquals(1, result.getPagination().getNumberOfElements());
      assertEquals(10, result.getPagination().getSize());
      assertEquals(25L, result.getPagination().getTotalElements());
      assertEquals(3, result.getPagination().getTotalPages());
    }

    @Test
    @DisplayName("should map empty page to PageResultResource with empty content")
    void shouldMapEmptyPageToPageResultResourceWithEmptyContent() {
      Pageable pageable = PageRequest.of(0, 10);
      Page<Hint> page = new PageImpl<>(Collections.emptyList(), pageable, 0L);

      PageResultResource<HintDTO> result = sut.mapToPageResultResource(page);

      assertNotNull(result);
      assertNotNull(result.getContent());
      assertEquals(0, result.getContent().size());
      assertNotNull(result.getPagination());
      assertEquals(0, result.getPagination().getNumber());
      assertEquals(0L, result.getPagination().getTotalElements());
    }

    @Test
    @DisplayName("should preserve DTO field values in content list")
    void shouldPreserveDtoFieldValuesInContentList() {
      Pageable pageable = PageRequest.of(0, 10);
      Page<Hint> page = new PageImpl<>(List.of(entity), pageable, 1L);

      PageResultResource<HintDTO> result = sut.mapToPageResultResource(page);

      HintDTO dtoItem = result.getContent().get(0);
      assertEquals(entity.getId(), dtoItem.getId());
      assertEquals(entity.getTitle(), dtoItem.getTitle());
      assertEquals(entity.getContent(), dtoItem.getContent());
      assertEquals(entity.getHintPenalty(), dtoItem.getHintPenalty());
      assertEquals(entity.getOrder(), dtoItem.getOrder());
    }
  }

  // --- createPagination (inherited from ParentMapper) ---

  @Nested
  @DisplayName("createPagination (inherited from ParentMapper)")
  class CreatePagination {

    @Test
    @DisplayName("should create pagination metadata for page")
    void shouldCreatePaginationMetadataForPage() {
      Pageable pageable = PageRequest.of(2, 10);
      Page<Hint> page = new PageImpl<>(Collections.emptyList(), pageable, 55L);

      PageResultResource.Pagination result = sut.createPagination(page);

      assertNotNull(result);
      assertEquals(2, result.getNumber());
      assertEquals(0, result.getNumberOfElements());
      assertEquals(10, result.getSize());
      assertEquals(55L, result.getTotalElements());
      assertEquals(6, result.getTotalPages());
    }
  }

  // Helper factory method

  private HintInfo createHintInfo() {
    HintInfo hintInfo = new HintInfo();
    hintInfo.setTrainingLevelId(1L);
    hintInfo.setHintId(ENTITY_ID);
    hintInfo.setHintTitle(ENTITY_TITLE);
    hintInfo.setHintContent(ENTITY_CONTENT);
    hintInfo.setOrder(ENTITY_ORDER);
    return hintInfo;
  }
}
