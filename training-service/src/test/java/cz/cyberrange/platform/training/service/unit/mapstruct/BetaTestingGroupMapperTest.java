package cz.cyberrange.platform.training.service.unit.mapstruct;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cz.cyberrange.platform.training.api.dto.betatestinggroup.BetaTestingGroupDTO;
import cz.cyberrange.platform.training.api.dto.betatestinggroup.BetaTestingGroupUpdateDTO;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.BetaTestingGroup;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.service.mapping.mapstruct.BetaTestingGroupMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.UserRefMapper;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

/**
 * Unit tests for {@link BetaTestingGroupMapper}.
 *
 * <p>Tests all mapping methods between BetaTestingGroup entity and DTOs. Uses constructor injection
 * with {@link BetaTestingGroupMapperImpl} since the mapper uses {@link UserRefMapper}.
 */
@DisplayName("BetaTestingGroupMapper")
class BetaTestingGroupMapperTest {

  private static final Long ENTITY_ID = 42L;
  private static final Long OTHER_ENTITY_ID = 43L;
  private static final Long ORGANIZER_REF_ID_1 = 100L;
  private static final Long ORGANIZER_REF_ID_2 = 200L;

  private BetaTestingGroupMapper sut;
  private UserRefMapper userRefMapper;

  private BetaTestingGroup entity;
  private BetaTestingGroupDTO dto;
  private BetaTestingGroupUpdateDTO updateDto;
  private UserRef organizer1;
  private UserRef organizer2;

  @BeforeEach
  void setUp() {
    userRefMapper = Mappers.getMapper(UserRefMapper.class);
    sut = Mappers.getMapper(BetaTestingGroupMapper.class);

    organizer1 = new UserRef();
    organizer1.setId(1L);
    organizer1.setUserRefId(ORGANIZER_REF_ID_1);

    organizer2 = new UserRef();
    organizer2.setId(2L);
    organizer2.setUserRefId(ORGANIZER_REF_ID_2);

    entity = new BetaTestingGroup();
    entity.setId(ENTITY_ID);
    entity.setOrganizers(new HashSet<>(Set.of(organizer1, organizer2)));

    dto = new BetaTestingGroupDTO();
    dto.setId(ENTITY_ID);
    dto.setOrganizersRefIds(Set.of(ORGANIZER_REF_ID_1, ORGANIZER_REF_ID_2));

    updateDto = new BetaTestingGroupUpdateDTO();
    updateDto.setOrganizersRefIds(Set.of(ORGANIZER_REF_ID_1, ORGANIZER_REF_ID_2));
  }

  @Nested
  @DisplayName("mapToEntity(BetaTestingGroupDTO)")
  class MapToEntity {

    @Test
    @DisplayName("should map DTO with organizers to entity")
    void shouldMapDtoWithOrganizersToEntity() {
      BetaTestingGroup result = sut.mapToEntity(dto);

      assertNotNull(result);
      assertEquals(dto.getId(), result.getId());
    }

    @Test
    @DisplayName("should map DTO with null id")
    void shouldMapDtoWithNullId() {
      dto.setId(null);

      BetaTestingGroup result = sut.mapToEntity(dto);

      assertNotNull(result);
      assertNull(result.getId());
    }

    @Test
    @DisplayName("should map DTO with empty organizers")
    void shouldMapDtoWithEmptyOrganizers() {
      dto.setOrganizersRefIds(Collections.emptySet());

      BetaTestingGroup result = sut.mapToEntity(dto);

      assertNotNull(result);
      assertTrue(result.getOrganizers().isEmpty());
    }
  }

  @Nested
  @DisplayName("mapToDTO(BetaTestingGroup)")
  class MapToDTO {

    @Test
    @DisplayName("should map entity with organizers to DTO")
    void shouldMapEntityWithOrganizersToDto() {
      BetaTestingGroupDTO result = sut.mapToDTO(entity);

      assertNotNull(result);
      assertEquals(entity.getId(), result.getId());
      assertNotNull(result.getOrganizersRefIds());
      assertEquals(2, result.getOrganizersRefIds().size());
    }

    @Test
    @DisplayName("should map entity with null organizers")
    void shouldMapEntityWithNullOrganizers() {
      entity.setOrganizers(null);

      BetaTestingGroupDTO result = sut.mapToDTO(entity);

      assertNotNull(result);
      assertNotNull(result.getOrganizersRefIds());
    }

    @Test
    @DisplayName("should map entity with empty organizers")
    void shouldMapEntityWithEmptyOrganizers() {
      entity.setOrganizers(Collections.emptySet());

      BetaTestingGroupDTO result = sut.mapToDTO(entity);

      assertNotNull(result);
      assertTrue(result.getOrganizersRefIds().isEmpty());
    }
  }

  @Nested
  @DisplayName("mapCreateToEntity(BetaTestingGroupUpdateDTO)")
  class MapCreateToEntity {

    @Test
    @DisplayName("should map update DTO to entity")
    void shouldMapUpdateDtoToEntity() {
      BetaTestingGroup result = sut.mapCreateToEntity(updateDto);

      assertNotNull(result);
      assertNull(result.getId());
      assertNotNull(result.getOrganizers());
    }

    @Test
    @DisplayName("should map update DTO with empty organizers")
    void shouldMapUpdateDtoWithEmptyOrganizers() {
      updateDto.setOrganizersRefIds(Collections.emptySet());

      BetaTestingGroup result = sut.mapCreateToEntity(updateDto);

      assertNotNull(result);
      assertTrue(result.getOrganizers().isEmpty());
    }

    @Test
    @DisplayName("should map update DTO with null organizers")
    void shouldMapUpdateDtoWithNullOrganizers() {
      updateDto.setOrganizersRefIds(null);

      BetaTestingGroup result = sut.mapCreateToEntity(updateDto);

      assertNotNull(result);
    }
  }

  @Nested
  @DisplayName("mapToList(Collection)")
  class MapToList {

    @Test
    @DisplayName("should map list of DTOs to list of entities")
    void shouldMapListOfDTOsToEntities() {
      List<BetaTestingGroupDTO> dtos = List.of(dto, dto);

      List<BetaTestingGroup> result = sut.mapToList(dtos);

      assertNotNull(result);
      assertEquals(2, result.size());
    }

    @Test
    @DisplayName("should return empty list for empty input")
    void shouldReturnEmptyListForEmptyInput() {
      List<BetaTestingGroup> result = sut.mapToList(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      List<BetaTestingGroup> result = sut.mapToList(null);

      assertNull(result);
    }
  }

  @Nested
  @DisplayName("mapToListDTO(Collection)")
  class MapToListDTO {

    @Test
    @DisplayName("should map list of entities to list of DTOs")
    void shouldMapListOfEntitiesToDTOs() {
      List<BetaTestingGroup> entities = List.of(entity, entity);

      List<BetaTestingGroupDTO> result = sut.mapToListDTO(entities);

      assertNotNull(result);
      assertEquals(2, result.size());
    }

    @Test
    @DisplayName("should return empty list for empty input")
    void shouldReturnEmptyListForEmptyInput() {
      List<BetaTestingGroupDTO> result = sut.mapToListDTO(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      List<BetaTestingGroupDTO> result = sut.mapToListDTO(null);

      assertNull(result);
    }
  }

  @Nested
  @DisplayName("mapToSet(Collection)")
  class MapToSet {

    @Test
    @DisplayName("should map collection of DTOs to set of entities")
    void shouldMapCollectionOfDTOsToSetOfEntities() {
      Collection<BetaTestingGroupDTO> dtos = List.of(dto, dto);

      Set<BetaTestingGroup> result = sut.mapToSet(dtos);

      assertNotNull(result);
      assertEquals(1, result.size());
    }

    @Test
    @DisplayName("should return empty set for empty input")
    void shouldReturnEmptySetForEmptyInput() {
      Set<BetaTestingGroup> result = sut.mapToSet(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      Set<BetaTestingGroup> result = sut.mapToSet(null);

      assertNull(result);
    }
  }

  @Nested
  @DisplayName("mapToSetDTO(Collection)")
  class MapToSetDTO {

    @Test
    @DisplayName("should map collection of entities to set of DTOs")
    void shouldMapCollectionOfEntitiesToSetOfDTOs() {
      BetaTestingGroup otherEntity = new BetaTestingGroup();
      otherEntity.setId(OTHER_ENTITY_ID);
      otherEntity.setOrganizers(new HashSet<>(Set.of(organizer1)));
      Collection<BetaTestingGroup> entities = List.of(entity, otherEntity);

      Set<BetaTestingGroupDTO> result = sut.mapToSetDTO(entities);

      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals(
          Set.of(ENTITY_ID, OTHER_ENTITY_ID),
          result.stream().map(BetaTestingGroupDTO::getId).collect(Collectors.toSet()));
    }

    @Test
    @DisplayName("should collapse equal entities into one DTO")
    void shouldCollapseEqualEntitiesIntoOneDto() {
      Collection<BetaTestingGroup> entities = List.of(entity, entity);

      Set<BetaTestingGroupDTO> result = sut.mapToSetDTO(entities);

      assertNotNull(result);
      assertEquals(1, result.size());
    }

    @Test
    @DisplayName("should return empty set for empty input")
    void shouldReturnEmptySetForEmptyInput() {
      Set<BetaTestingGroupDTO> result = sut.mapToSetDTO(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      Set<BetaTestingGroupDTO> result = sut.mapToSetDTO(null);

      assertNull(result);
    }
  }

  @Nested
  @DisplayName("mapToOptional(BetaTestingGroupDTO)")
  class MapToOptionalDto {

    @Test
    @DisplayName("should return Optional with entity for non-null DTO")
    void shouldReturnOptionalWithEntityForNonNullDto() {
      Optional<BetaTestingGroup> result = sut.mapToEntityOptional(dto);

      assertNotNull(result);
      assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("should return empty Optional for null DTO")
    void shouldReturnEmptyOptionalForNullDto() {
      Optional<BetaTestingGroup> result = sut.mapToEntityOptional(null);

      assertNotNull(result);
      assertFalse(result.isPresent());
    }
  }

  @Nested
  @DisplayName("mapToOptional(BetaTestingGroup)")
  class MapToOptionalEntity {

    @Test
    @DisplayName("should return Optional with DTO for non-null entity")
    void shouldReturnOptionalWithDtoForNonNullEntity() {
      Optional<BetaTestingGroupDTO> result = sut.mapToDTOOptional(entity);

      assertNotNull(result);
      assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("should return empty Optional for null entity")
    void shouldReturnEmptyOptionalForNullEntity() {
      Optional<BetaTestingGroupDTO> result = sut.mapToDTOOptional((BetaTestingGroup) null);

      assertNotNull(result);
      assertFalse(result.isPresent());
    }
  }

  @Nested
  @DisplayName("mapToPageDTO(Page)")
  class MapToPageDTO {

    @Test
    @DisplayName("should map page of entities to page of DTOs")
    void shouldMapPageOfEntitiesToPageOfDTOs() {
      Page<BetaTestingGroup> page =
          new PageImpl<>(List.of(entity, entity), PageRequest.of(0, 10), 2);

      Page<BetaTestingGroupDTO> result = sut.mapToPageDTO(page);

      assertNotNull(result);
      assertEquals(2, result.getContent().size());
      assertEquals(2, result.getTotalElements());
    }

    @Test
    @DisplayName("should return empty page for empty input")
    void shouldReturnEmptyPageForEmptyInput() {
      Page<BetaTestingGroup> page =
          new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

      Page<BetaTestingGroupDTO> result = sut.mapToPageDTO(page);

      assertNotNull(result);
      assertEquals(0, result.getContent().size());
    }
  }

  @Nested
  @DisplayName("mapToPage(Page)")
  class MapToPage {

    @Test
    @DisplayName("should map page of DTOs to page of entities")
    void shouldMapPageOfDTOsToPageOfEntities() {
      Page<BetaTestingGroupDTO> page = new PageImpl<>(List.of(dto, dto), PageRequest.of(0, 10), 2);

      Page<BetaTestingGroup> result = sut.mapToPage(page);

      assertNotNull(result);
      assertEquals(2, result.getContent().size());
      assertEquals(2, result.getTotalElements());
    }

    @Test
    @DisplayName("should return empty page for empty input")
    void shouldReturnEmptyPageForEmptyInput() {
      Page<BetaTestingGroupDTO> page =
          new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

      Page<BetaTestingGroup> result = sut.mapToPage(page);

      assertNotNull(result);
      assertEquals(0, result.getContent().size());
    }
  }

  @Nested
  @DisplayName("mapToPageResultResource(Page)")
  class MapToPageResultResource {

    @Test
    @DisplayName("should map page to PageResultResource with content and pagination")
    void shouldMapPageToPageResultResourceWithContentAndPagination() {
      Page<BetaTestingGroup> page =
          new PageImpl<>(List.of(entity, entity), PageRequest.of(0, 10), 2);

      PageResultResource<BetaTestingGroupDTO> result = sut.mapToPageResultResource(page);

      assertNotNull(result);
      assertNotNull(result.getContent());
      assertEquals(2, result.getContent().size());
      assertNotNull(result.getPagination());
    }

    @Test
    @DisplayName("should return empty content for empty page")
    void shouldReturnEmptyContentForEmptyPage() {
      Page<BetaTestingGroup> page =
          new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

      PageResultResource<BetaTestingGroupDTO> result = sut.mapToPageResultResource(page);

      assertNotNull(result);
      assertNotNull(result.getContent());
      assertEquals(0, result.getContent().size());
      assertNotNull(result.getPagination());
    }
  }
}
