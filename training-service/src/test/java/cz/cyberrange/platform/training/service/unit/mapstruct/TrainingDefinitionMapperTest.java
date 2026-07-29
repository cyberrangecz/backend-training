package cz.cyberrange.platform.training.service.unit.mapstruct;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionBasicDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionByIdDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionCreateDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionInfoDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionUpdateDTO;
import cz.cyberrange.platform.training.api.enums.TDState;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.BetaTestingGroup;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.service.mapping.mapstruct.BetaTestingGroupMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.EnumMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.TrainingDefinitionMapper;
import java.time.LocalDateTime;
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
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for {@link TrainingDefinitionMapper}.
 *
 * <p>Tests all mapping methods including the special {@code mapToPageResultResource} behavior that
 * sets {@code betaTestingGroupId} when the entity's betaTestingGroup is not null. Uses constructor
 * injection with manually constructed impl since the mapper has non-empty {@code uses}.
 */
@DisplayName("TrainingDefinitionMapper")
class TrainingDefinitionMapperTest {

  private static final Long ENTITY_ID = 42L;
  private static final Long BTG_ID = 7L;
  private static final String TITLE = "Test Training";
  private static final String DESCRIPTION = "Test description";
  private static final String[] PREREQUISITES = {"networking"};
  private static final String[] OUTCOMES = {"security"};
  private static final TDState STATE = TDState.UNRELEASED;
  private static final long ESTIMATED_DURATION = 30L;
  private static final LocalDateTime LAST_EDITED = LocalDateTime.of(2020, 1, 1, 12, 0);
  private static final String LAST_EDITED_BY = "admin";
  private static final LocalDateTime CREATED_AT = LocalDateTime.of(2019, 1, 1, 12, 0);

  private TrainingDefinitionMapper sut;
  private EnumMapper enumMapper;

  private UserRef userRef;
  private BetaTestingGroup betaTestingGroup;
  private TrainingDefinition entity;

  @BeforeEach
  void setUp() {
    BetaTestingGroupMapper betaTestingGroupMapper = Mappers.getMapper(BetaTestingGroupMapper.class);
    enumMapper = Mappers.getMapper(EnumMapper.class);
    sut = Mappers.getMapper(TrainingDefinitionMapper.class);
    ReflectionTestUtils.setField(sut, "enumMapper", enumMapper);
    ReflectionTestUtils.setField(sut, "betaTestingGroupMapper", betaTestingGroupMapper);

    userRef = new UserRef();
    userRef.setId(99L);
    userRef.setUserRefId(100L);

    betaTestingGroup = new BetaTestingGroup();
    betaTestingGroup.setId(BTG_ID);

    entity = new TrainingDefinition();
    entity.setId(ENTITY_ID);
    entity.setTitle(TITLE);
    entity.setDescription(DESCRIPTION);
    entity.setPrerequisites(PREREQUISITES);
    entity.setOutcomes(OUTCOMES);
    entity.setState(cz.cyberrange.platform.training.persistence.model.enums.TDState.UNRELEASED);
    entity.setEstimatedDuration(ESTIMATED_DURATION);
    entity.setLastEdited(LAST_EDITED);
    entity.setLastEditedBy(LAST_EDITED_BY);
    entity.setCreatedAt(CREATED_AT);
  }

  @Nested
  @DisplayName("mapToEntity(TrainingDefinitionByIdDTO)")
  class MapToEntity {

    @Test
    @DisplayName("should map all DTO fields to entity")
    void shouldMapAllFieldsFromDtoToEntity() {
      TrainingDefinitionByIdDTO dto = new TrainingDefinitionByIdDTO();
      dto.setId(ENTITY_ID);
      dto.setTitle(TITLE);
      dto.setDescription(DESCRIPTION);
      dto.setPrerequisites(PREREQUISITES);
      dto.setOutcomes(OUTCOMES);
      dto.setState(STATE);
      dto.setEstimatedDuration(ESTIMATED_DURATION);
      dto.setLastEdited(LAST_EDITED);
      dto.setLastEditedBy(LAST_EDITED_BY);
      dto.setBetaTestingGroupId(BTG_ID);
      dto.setCanBeArchived(true);
      dto.setCreatedAt(CREATED_AT);

      TrainingDefinition result = sut.mapToEntity(dto);

      assertNotNull(result);
      assertEquals(dto.getId(), result.getId());
      assertEquals(dto.getTitle(), result.getTitle());
      assertEquals(dto.getDescription(), result.getDescription());
      assertEquals(enumMapper.mapTDState(dto.getState()), result.getState());
      assertEquals(dto.getEstimatedDuration(), result.getEstimatedDuration());
    }

    @Test
    @DisplayName("should map DTO with null fields")
    void shouldMapDtoWithNullFields() {
      TrainingDefinitionByIdDTO dto = new TrainingDefinitionByIdDTO();
      dto.setId(null);
      dto.setTitle(null);
      dto.setDescription(null);
      dto.setPrerequisites(null);
      dto.setOutcomes(null);
      dto.setState(null);

      TrainingDefinition result = sut.mapToEntity(dto);

      assertNotNull(result);
      assertNull(result.getId());
      assertNull(result.getTitle());
      assertNull(result.getDescription());
      assertNull(result.getState());
    }
  }

  @Nested
  @DisplayName("mapToDTOById(TrainingDefinition)")
  class MapToDTOById {

    @Test
    @DisplayName("should map entity to ByIdDTO")
    void shouldMapEntityToByIdDTO() {
      TrainingDefinitionByIdDTO result = sut.mapToDTOById(entity);

      assertNotNull(result);
      assertEquals(entity.getId(), result.getId());
      assertEquals(entity.getTitle(), result.getTitle());
      assertEquals(entity.getDescription(), result.getDescription());
      assertEquals(enumMapper.mapTDState(entity.getState()), result.getState());
    }

    @Test
    @DisplayName("should map entity with null optional fields")
    void shouldMapEntityWithNullOptionalFields() {
      entity.setDescription(null);
      entity.setPrerequisites(null);
      entity.setOutcomes(null);
      entity.setBetaTestingGroup(null);

      TrainingDefinitionByIdDTO result = sut.mapToDTOById(entity);

      assertNotNull(result);
      assertEquals(entity.getId(), result.getId());
      assertNull(result.getDescription());
      assertNull(result.getBetaTestingGroupId());
    }
  }

  @Nested
  @DisplayName("mapToDTO(TrainingDefinition)")
  class MapToDTO {

    @Test
    @DisplayName("should map entity to full DTO")
    void shouldMapEntityToDto() {
      entity.setBetaTestingGroup(betaTestingGroup);

      TrainingDefinitionDTO result = sut.mapToDTO(entity);

      assertNotNull(result);
      assertEquals(entity.getId(), result.getId());
      assertEquals(entity.getTitle(), result.getTitle());
      assertEquals(entity.getDescription(), result.getDescription());
      assertEquals(entity.getPrerequisites().length, result.getPrerequisites().length);
      assertEquals(entity.getOutcomes().length, result.getOutcomes().length);
      assertEquals(entity.getEstimatedDuration(), result.getEstimatedDuration());
      assertEquals(entity.getLastEdited(), result.getLastEdited());
      assertEquals(entity.getLastEditedBy(), result.getLastEditedBy());
      assertEquals(entity.getCreatedAt(), result.getCreatedAt());
      assertEquals(BTG_ID, result.getBetaTestingGroupId());
    }

    @Test
    @DisplayName("should map entity with null betaTestingGroup")
    void shouldMapEntityWithNullBetaTestingGroup() {
      entity.setBetaTestingGroup(null);

      TrainingDefinitionDTO result = sut.mapToDTO(entity);

      assertNotNull(result);
      assertEquals(entity.getId(), result.getId());
      assertNull(result.getBetaTestingGroupId());
    }

    @Test
    @DisplayName("should map entity with empty properties")
    void shouldMapEntityWithEmptyProperties() {
      entity.setId(null);
      entity.setTitle("");
      entity.setDescription(null);
      entity.setPrerequisites(null);
      entity.setOutcomes(null);
      entity.setBetaTestingGroup(null);
      entity.setLastEdited(null);
      entity.setLastEditedBy(null);
      entity.setCreatedAt(null);

      TrainingDefinitionDTO result = sut.mapToDTO(entity);

      assertNotNull(result);
      assertNull(result.getId());
      assertEquals("", result.getTitle());
      assertNull(result.getBetaTestingGroupId());
    }
  }

  @Nested
  @DisplayName("mapToBasicDTO(TrainingDefinition)")
  class MapToBasicDTO {

    @Test
    @DisplayName("should map entity to basic DTO")
    void shouldMapEntityToBasicDto() {
      TrainingDefinitionBasicDTO result = sut.mapToBasicDTO(entity);

      assertNotNull(result);
      assertEquals(entity.getId(), result.getId());
      assertEquals(entity.getTitle(), result.getTitle());
      assertEquals(entity.getDescription(), result.getDescription());
      assertEquals(entity.getEstimatedDuration(), result.getEstimatedDuration());
    }

    @Test
    @DisplayName("should map entity with null fields")
    void shouldMapEntityWithNullFields() {
      entity.setId(null);
      entity.setTitle(null);
      entity.setDescription(null);

      TrainingDefinitionBasicDTO result = sut.mapToBasicDTO(entity);

      assertNotNull(result);
      assertNull(result.getId());
      assertNull(result.getTitle());
      assertNull(result.getDescription());
    }

    @Test
    @DisplayName(
        "should expose levels as an empty, non-null array when the definition has no levels")
    void shouldDefaultLevelsToEmptyArray() {
      TrainingDefinitionBasicDTO result = sut.mapToBasicDTO(entity);

      assertNotNull(result.getLevels());
      assertTrue(result.getLevels().isEmpty());
    }
  }

  @Nested
  @DisplayName("mapToInfoDTO(TrainingDefinition)")
  class MapToInfoDTO {

    @Test
    @DisplayName("should map entity to info DTO")
    void shouldMapEntityToInfoDto() {
      TrainingDefinitionInfoDTO result = sut.mapToInfoDTO(entity);

      assertNotNull(result);
      assertEquals(entity.getId(), result.getId());
      assertEquals(entity.getTitle(), result.getTitle());
      assertEquals(enumMapper.mapTDState(entity.getState()), result.getState());
    }

    @Test
    @DisplayName("should map entity with null fields")
    void shouldMapEntityWithNullFields() {
      entity.setId(null);
      entity.setTitle(null);
      entity.setState(null);

      TrainingDefinitionInfoDTO result = sut.mapToInfoDTO(entity);

      assertNotNull(result);
      assertNull(result.getId());
      assertNull(result.getTitle());
      assertNull(result.getState());
    }
  }

  @Nested
  @DisplayName("mapCreateToEntity(TrainingDefinitionCreateDTO)")
  class MapCreateToEntity {

    @Test
    @DisplayName("should map create DTO to entity")
    void shouldMapCreateDtoToEntity() {
      TrainingDefinitionCreateDTO dto = new TrainingDefinitionCreateDTO();
      dto.setTitle(TITLE);
      dto.setDescription(DESCRIPTION);
      dto.setState(STATE);
      dto.setDefaultContent(false);

      TrainingDefinition result = sut.mapCreateToEntity(dto);

      assertNotNull(result);
      assertEquals(dto.getTitle(), result.getTitle());
      assertEquals(dto.getDescription(), result.getDescription());
      assertEquals(enumMapper.mapTDState(dto.getState()), result.getState());
    }

    @Test
    @DisplayName("should map create DTO with null description")
    void shouldMapCreateDtoWithNullDescription() {
      TrainingDefinitionCreateDTO dto = new TrainingDefinitionCreateDTO();
      dto.setTitle(TITLE);
      dto.setDescription(null);
      dto.setState(STATE);

      TrainingDefinition result = sut.mapCreateToEntity(dto);

      assertNotNull(result);
      assertNull(result.getDescription());
    }
  }

  @Nested
  @DisplayName("mapUpdateToEntity(TrainingDefinitionUpdateDTO)")
  class MapUpdateToEntity {

    @Test
    @DisplayName("should map update DTO to entity")
    void shouldMapUpdateDtoToEntity() {
      TrainingDefinitionUpdateDTO dto = new TrainingDefinitionUpdateDTO();
      dto.setId(ENTITY_ID);
      dto.setTitle(TITLE);
      dto.setDescription(DESCRIPTION);
      dto.setState(STATE);
      dto.setShowStepperBar(true);

      TrainingDefinition result = sut.mapUpdateToEntity(dto);

      assertNotNull(result);
      assertEquals(dto.getId(), result.getId());
      assertEquals(dto.getTitle(), result.getTitle());
      assertEquals(dto.getDescription(), result.getDescription());
      assertEquals(enumMapper.mapTDState(dto.getState()), result.getState());
    }

    @Test
    @DisplayName("should map update DTO with null fields")
    void shouldMapUpdateDtoWithNullFields() {
      TrainingDefinitionUpdateDTO dto = new TrainingDefinitionUpdateDTO();
      dto.setId(ENTITY_ID);
      dto.setTitle(null);
      dto.setDescription(null);
      dto.setState(null);

      TrainingDefinition result = sut.mapUpdateToEntity(dto);

      assertNotNull(result);
      assertNull(result.getTitle());
      assertNull(result.getDescription());
      assertNull(result.getState());
    }
  }

  @Nested
  @DisplayName("mapToList(Collection)")
  class MapToList {

    @Test
    @DisplayName("should map collection of DTOs to list of entities")
    void shouldMapCollectionOfDtosToEntities() {
      TrainingDefinitionByIdDTO dto1 = new TrainingDefinitionByIdDTO();
      dto1.setId(1L);
      dto1.setTitle("TD1");
      dto1.setState(STATE);
      TrainingDefinitionByIdDTO dto2 = new TrainingDefinitionByIdDTO();
      dto2.setId(2L);
      dto2.setTitle("TD2");
      dto2.setState(STATE);

      List<TrainingDefinition> result = sut.mapToList(List.of(dto1, dto2));

      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals("TD1", result.get(0).getTitle());
      assertEquals("TD2", result.get(1).getTitle());
    }

    @Test
    @DisplayName("should return empty list for empty input")
    void shouldReturnEmptyListForEmptyInput() {
      List<TrainingDefinition> result = sut.mapToList(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      List<TrainingDefinition> result = sut.mapToList(null);

      assertNull(result);
    }
  }

  @Nested
  @DisplayName("mapToListDTO(Collection)")
  class MapToListDto {

    @Test
    @DisplayName("should map collection of entities to list of DTOs")
    void shouldMapCollectionOfEntitiesToDtos() {
      TrainingDefinition entity1 = new TrainingDefinition();
      entity1.setId(1L);
      entity1.setTitle("TD1");
      entity1.setState(cz.cyberrange.platform.training.persistence.model.enums.TDState.UNRELEASED);
      entity1.setLastEdited(LAST_EDITED);
      entity1.setLastEditedBy(LAST_EDITED_BY);
      entity1.setCreatedAt(CREATED_AT);
      TrainingDefinition entity2 = new TrainingDefinition();
      entity2.setId(2L);
      entity2.setTitle("TD2");
      entity2.setState(cz.cyberrange.platform.training.persistence.model.enums.TDState.UNRELEASED);
      entity2.setLastEdited(LAST_EDITED);
      entity2.setLastEditedBy(LAST_EDITED_BY);
      entity2.setCreatedAt(CREATED_AT);

      List<TrainingDefinitionByIdDTO> result = sut.mapToListDTO(List.of(entity1, entity2));

      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals("TD1", result.get(0).getTitle());
      assertEquals("TD2", result.get(1).getTitle());
    }

    @Test
    @DisplayName("should return empty list for empty input")
    void shouldReturnEmptyListForEmptyInput() {
      List<TrainingDefinitionByIdDTO> result = sut.mapToListDTO(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      List<TrainingDefinitionByIdDTO> result = sut.mapToListDTO(null);

      assertNull(result);
    }
  }

  @Nested
  @DisplayName("mapToSet(Collection)")
  class MapToSet {

    @Test
    @DisplayName("should map collection of DTOs to set of entities")
    void shouldMapCollectionOfDtosToSet() {
      TrainingDefinitionByIdDTO dto = new TrainingDefinitionByIdDTO();
      dto.setId(ENTITY_ID);
      dto.setTitle(TITLE);
      dto.setState(STATE);

      Set<TrainingDefinition> result = sut.mapToSet(List.of(dto));

      assertNotNull(result);
      assertEquals(1, result.size());
    }

    @Test
    @DisplayName("should return empty set for empty input")
    void shouldReturnEmptySetForEmptyInput() {
      Set<TrainingDefinition> result = sut.mapToSet(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      Set<TrainingDefinition> result = sut.mapToSet(null);

      assertNull(result);
    }
  }

  @Nested
  @DisplayName("mapToSetDTO(Collection)")
  class MapToSetDto {

    @Test
    @DisplayName("should map collection of entities to set of DTOs")
    void shouldMapCollectionOfEntitiesToSet() {
      entity.setLastEdited(LAST_EDITED);
      entity.setLastEditedBy(LAST_EDITED_BY);
      entity.setCreatedAt(CREATED_AT);

      Set<TrainingDefinitionByIdDTO> result = sut.mapToSetDTO(List.of(entity));

      assertNotNull(result);
      assertEquals(1, result.size());
    }

    @Test
    @DisplayName("should return empty set for empty input")
    void shouldReturnEmptySetForEmptyInput() {
      Set<TrainingDefinitionByIdDTO> result = sut.mapToSetDTO(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      Set<TrainingDefinitionByIdDTO> result = sut.mapToSetDTO(null);

      assertNull(result);
    }
  }

  @Nested
  @DisplayName("mapToOptional(TrainingDefinitionByIdDTO)")
  class MapToOptionalDto {

    @Test
    @DisplayName("should return present Optional for non-null DTO")
    void shouldReturnPresentOptionalForNonNullDto() {
      TrainingDefinitionByIdDTO dto = new TrainingDefinitionByIdDTO();
      dto.setId(ENTITY_ID);
      dto.setTitle(TITLE);
      dto.setState(STATE);

      Optional<TrainingDefinition> result = sut.mapToEntityOptional(dto);

      assertNotNull(result);
      assertTrue(result.isPresent());
      assertEquals(dto.getId(), result.get().getId());
    }

    @Test
    @DisplayName("should return empty Optional for null DTO")
    void shouldReturnEmptyOptionalForNullDto() {
      Optional<TrainingDefinition> result = sut.mapToEntityOptional(null);

      assertNotNull(result);
      assertFalse(result.isPresent());
    }
  }

  @Nested
  @DisplayName("mapToOptional(TrainingDefinition)")
  class MapToOptionalEntity {

    @Test
    @DisplayName("should return present Optional for non-null entity")
    void shouldReturnPresentOptionalForNonNullEntity() {
      Optional<TrainingDefinitionByIdDTO> result = sut.mapToDTOOptional(entity);

      assertNotNull(result);
      assertTrue(result.isPresent());
      assertEquals(entity.getId(), result.get().getId());
    }

    @Test
    @DisplayName("should return empty Optional for null entity")
    void shouldReturnEmptyOptionalForNullEntity() {
      Optional<TrainingDefinitionByIdDTO> result = sut.mapToDTOOptional(null);

      assertNotNull(result);
      assertFalse(result.isPresent());
    }
  }

  @Nested
  @DisplayName("mapToPageDTO(Page)")
  class MapToPageDto {

    @Test
    @DisplayName("should map page of entities to page of DTOs")
    void shouldMapPageOfEntitiesToPageOfDtos() {
      entity.setLastEdited(LAST_EDITED);
      entity.setLastEditedBy(LAST_EDITED_BY);
      entity.setCreatedAt(CREATED_AT);
      Page<TrainingDefinition> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);

      var result = sut.mapToPageDTO(page);

      assertNotNull(result);
      assertEquals(1, result.getContent().size());
      assertEquals(entity.getId(), result.getContent().get(0).getId());
      assertEquals(0, result.getPageable().getPageNumber());
    }

    @Test
    @DisplayName("should return empty page for empty input")
    void shouldReturnEmptyPageForEmptyInput() {
      Page<TrainingDefinition> page =
          new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

      var result = sut.mapToPageDTO(page);

      assertNotNull(result);
      assertEquals(0, result.getContent().size());
    }
  }

  @Nested
  @DisplayName("mapToPage(Page)")
  class MapToPage {

    @Test
    @DisplayName("should map page of DTOs to page of entities")
    void shouldMapPageOfDtosToPageOfEntities() {
      TrainingDefinitionByIdDTO dto = new TrainingDefinitionByIdDTO();
      dto.setId(ENTITY_ID);
      dto.setTitle(TITLE);
      dto.setState(STATE);
      Page<TrainingDefinitionByIdDTO> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);

      var result = sut.mapToPage(page);

      assertNotNull(result);
      assertEquals(1, result.getContent().size());
      assertEquals(dto.getId(), result.getContent().get(0).getId());
    }

    @Test
    @DisplayName("should return empty page for empty input")
    void shouldReturnEmptyPageForEmptyInput() {
      Page<TrainingDefinitionByIdDTO> page =
          new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

      var result = sut.mapToPage(page);

      assertNotNull(result);
      assertEquals(0, result.getContent().size());
    }
  }

  @Nested
  @DisplayName("mapToPageResultResource(Page)")
  class MapToPageResultResource {

    @Test
    @DisplayName("should map page with betaTestingGroup and set betaTestingGroupId")
    void shouldMapPageWithBetaTestingGroupAndSetBetaTestingGroupId() {
      entity.setBetaTestingGroup(betaTestingGroup);
      Page<TrainingDefinition> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);

      PageResultResource<TrainingDefinitionDTO> result = sut.mapToPageResultResource(page);

      assertNotNull(result);
      assertNotNull(result.getContent());
      assertEquals(1, result.getContent().size());
      assertEquals(BTG_ID, result.getContent().get(0).getBetaTestingGroupId());
      assertEquals(0, result.getPagination().getNumber());
      assertEquals(1, result.getPagination().getNumberOfElements());
    }

    @Test
    @DisplayName("should map page with null betaTestingGroup and leave betaTestingGroupId null")
    void shouldMapPageWithNullBetaTestingGroupAndLeaveBetaTestingGroupIdNull() {
      entity.setBetaTestingGroup(null);
      Page<TrainingDefinition> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);

      PageResultResource<TrainingDefinitionDTO> result = sut.mapToPageResultResource(page);

      assertNotNull(result);
      assertEquals(1, result.getContent().size());
      assertNull(result.getContent().get(0).getBetaTestingGroupId());
    }

    @Test
    @DisplayName("should map empty page")
    void shouldMapEmptyPage() {
      Page<TrainingDefinition> page =
          new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

      PageResultResource<TrainingDefinitionDTO> result = sut.mapToPageResultResource(page);

      assertNotNull(result);
      assertEquals(0, result.getContent().size());
      assertEquals(0, result.getPagination().getTotalElements());
    }

    @Test
    @DisplayName("should map page with multiple entities")
    void shouldMapPageWithMultipleEntities() {
      TrainingDefinition entity2 = new TrainingDefinition();
      entity2.setId(2L);
      entity2.setTitle("TD2");
      entity2.setState(cz.cyberrange.platform.training.persistence.model.enums.TDState.UNRELEASED);
      entity2.setLastEdited(LAST_EDITED);
      entity2.setLastEditedBy(LAST_EDITED_BY);
      entity2.setCreatedAt(CREATED_AT);
      Page<TrainingDefinition> page =
          new PageImpl<>(List.of(entity, entity2), PageRequest.of(0, 10), 2);

      PageResultResource<TrainingDefinitionDTO> result = sut.mapToPageResultResource(page);

      assertNotNull(result);
      assertEquals(2, result.getContent().size());
    }
  }

  @Nested
  @DisplayName("mapToPageResultResourceInfoDTO(Page)")
  class MapToPageResultResourceInfoDto {

    @Test
    @DisplayName("should map page to info DTO page result")
    void shouldMapPageToInfoDtoPageResult() {
      Page<TrainingDefinition> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);

      PageResultResource<TrainingDefinitionInfoDTO> result =
          sut.mapToPageResultResourceInfoDTO(page);

      assertNotNull(result);
      assertEquals(1, result.getContent().size());
      assertEquals(entity.getId(), result.getContent().get(0).getId());
      assertEquals(entity.getTitle(), result.getContent().get(0).getTitle());
      assertEquals(enumMapper.mapTDState(entity.getState()), result.getContent().get(0).getState());
    }

    @Test
    @DisplayName("should return empty page result for empty page")
    void shouldReturnEmptyPageResultForEmptyPage() {
      Page<TrainingDefinition> page =
          new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

      PageResultResource<TrainingDefinitionInfoDTO> result =
          sut.mapToPageResultResourceInfoDTO(page);

      assertNotNull(result);
      assertEquals(0, result.getContent().size());
    }
  }
}
