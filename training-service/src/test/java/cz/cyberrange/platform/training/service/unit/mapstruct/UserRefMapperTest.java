package cz.cyberrange.platform.training.service.unit.mapstruct;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.dto.export.UserRefExportDTO;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.service.mapping.mapstruct.UserRefMapper;
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

/**
 * Unit tests for {@link UserRefMapper}.
 *
 * <p>Tests all mapping methods between UserRef entity and DTOs. Uses {@link Mappers#getMapper}
 * since the mapper has cyclic dependency with TrainingInstanceMapper which MapStruct handles via
 * lazy initialization.
 */
@DisplayName("UserRefMapper")
class UserRefMapperTest {

  private static final Long ENTITY_ID = 42L;
  private static final Long USER_REF_ID = 100L;
  private static final String USER_REF_SUB = "user-12345";
  private static final String USER_REF_FULL_NAME = "John Doe";
  private static final String USER_REF_GIVEN_NAME = "John";
  private static final String USER_REF_FAMILY_NAME = "Doe";
  private static final String ISS = "https://oidc.provider.cz";
  private static final byte[] PICTURE = new byte[] {0x1, 0x2, 0x3};
  private static final String MAIL = "johndoe@example.cz";

  private UserRefMapper sut;

  private UserRef entity;
  private UserRefDTO dto;

  @BeforeEach
  void setUp() {
    sut = Mappers.getMapper(UserRefMapper.class);

    entity = new UserRef();
    entity.setId(ENTITY_ID);
    entity.setUserRefId(USER_REF_ID);

    dto = new UserRefDTO();
    dto.setUserRefId(USER_REF_ID);
    dto.setUserRefSub(USER_REF_SUB);
    dto.setUserRefFullName(USER_REF_FULL_NAME);
    dto.setUserRefGivenName(USER_REF_GIVEN_NAME);
    dto.setUserRefFamilyName(USER_REF_FAMILY_NAME);
    dto.setIss(ISS);
    dto.setPicture(PICTURE);
    dto.setMail(MAIL);
  }

  @Nested
  @DisplayName("mapToEntity(UserRefDTO)")
  class MapToEntity {

    @Test
    @DisplayName("should map all DTO fields to entity")
    void shouldMapAllFieldsFromDtoToEntity() {
      UserRef result = sut.mapToEntity(dto);

      assertNotNull(result);
      assertEquals(dto.getUserRefId(), result.getUserRefId());
    }

    @Test
    @DisplayName("should map DTO with null fields")
    void shouldMapDtoWithNullFields() {
      dto.setUserRefId(null);
      dto.setUserRefSub(null);
      dto.setUserRefFullName(null);
      dto.setUserRefGivenName(null);
      dto.setUserRefFamilyName(null);
      dto.setIss(null);
      dto.setPicture(null);
      dto.setMail(null);

      UserRef result = sut.mapToEntity(dto);

      assertNotNull(result);
      assertNull(result.getUserRefId());
    }
  }

  @Nested
  @DisplayName("mapToDTO(UserRef)")
  class MapToDTO {

    @Test
    @DisplayName("should map all entity fields to DTO")
    void shouldMapAllFieldsFromEntityToDto() {
      UserRefDTO result = sut.mapToDTO(entity);

      assertNotNull(result);
      assertEquals(entity.getUserRefId(), result.getUserRefId());
    }

    @Test
    @DisplayName("should map entity with null userRefId")
    void shouldMapEntityWithNullUserRefId() {
      entity.setUserRefId(null);

      UserRefDTO result = sut.mapToDTO(entity);

      assertNotNull(result);
      assertNull(result.getUserRefId());
    }
  }

  @Nested
  @DisplayName("mapToList(Collection)")
  class MapToList {

    @Test
    @DisplayName("should map list of DTOs to list of entities")
    void shouldMapListOfDTOsToEntities() {
      List<UserRefDTO> dtos = List.of(dto, dto);

      List<UserRef> result = sut.mapToList(dtos);

      assertNotNull(result);
      assertEquals(2, result.size());
    }

    @Test
    @DisplayName("should return empty list for empty input")
    void shouldReturnEmptyListForEmptyInput() {
      List<UserRef> result = sut.mapToList(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      List<UserRef> result = sut.mapToList(null);

      assertNull(result);
    }
  }

  @Nested
  @DisplayName("mapToListDTO(Collection)")
  class MapToListDTO {

    @Test
    @DisplayName("should map list of entities to list of DTOs")
    void shouldMapListOfEntitiesToDTOs() {
      List<UserRef> entities = List.of(entity, entity);

      List<UserRefDTO> result = sut.mapToListDTO(entities);

      assertNotNull(result);
      assertEquals(2, result.size());
    }

    @Test
    @DisplayName("should return empty list for empty input")
    void shouldReturnEmptyListForEmptyInput() {
      List<UserRefDTO> result = sut.mapToListDTO(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      List<UserRefDTO> result = sut.mapToListDTO(null);

      assertNull(result);
    }
  }

  @Nested
  @DisplayName("mapToSet(Collection)")
  class MapToSet {

    @Test
    @DisplayName("should map collection of DTOs to set of entities")
    void shouldMapCollectionOfDTOsToSetOfEntities() {
      UserRefDTO dto2 = new UserRefDTO();
      dto2.setUserRefId(USER_REF_ID + 1L);
      Collection<UserRefDTO> dtos = List.of(dto, dto2);

      Set<UserRef> result = sut.mapToSet(dtos);

      assertNotNull(result);
      assertEquals(2, result.size());
    }

    @Test
    @DisplayName("should return empty set for empty input")
    void shouldReturnEmptySetForEmptyInput() {
      Set<UserRef> result = sut.mapToSet(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      Set<UserRef> result = sut.mapToSet(null);

      assertNull(result);
    }
  }

  @Nested
  @DisplayName("mapToSetDTO(Collection)")
  class MapToSetDTO {

    @Test
    @DisplayName("should map collection of entities to set of DTOs")
    void shouldMapCollectionOfEntitiesToSetOfDTOs() {
      UserRef entity2 = new UserRef();
      entity2.setId(ENTITY_ID + 1L);
      entity2.setUserRefId(USER_REF_ID + 1L);
      Collection<UserRef> entities = List.of(entity, entity2);

      Set<UserRefDTO> result = sut.mapToSetDTO(entities);

      assertNotNull(result);
      assertEquals(2, result.size());
    }

    @Test
    @DisplayName("should return empty set for empty input")
    void shouldReturnEmptySetForEmptyInput() {
      Set<UserRefDTO> result = sut.mapToSetDTO(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      Set<UserRefDTO> result = sut.mapToSetDTO(null);

      assertNull(result);
    }
  }

  @Nested
  @DisplayName("mapUserRefExportDTOToUserRefDTO(Collection)")
  class MapUserRefExportDTOToUserRefDTO {

    @Test
    @DisplayName("should map collection of UserRefDTOs to UserRefExportDTOs")
    void shouldMapCollectionOfUserRefDTOsToExportDTOs() {
      Collection<UserRefDTO> userRefDtos = List.of(dto, dto);

      List<UserRefExportDTO> result = sut.mapUserRefExportDTOToUserRefDTO(userRefDtos);

      assertNotNull(result);
      assertEquals(2, result.size());
    }

    @Test
    @DisplayName("should return empty list for empty input")
    void shouldReturnEmptyListForEmptyInput() {
      List<UserRefExportDTO> result = sut.mapUserRefExportDTOToUserRefDTO(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      List<UserRefExportDTO> result = sut.mapUserRefExportDTOToUserRefDTO(null);

      assertNull(result);
    }
  }

  @Nested
  @DisplayName("mapToOptional(UserRefDTO)")
  class MapToOptionalDto {

    @Test
    @DisplayName("should return Optional with entity for non-null DTO")
    void shouldReturnOptionalWithEntityForNonNullDto() {
      Optional<UserRef> result = sut.mapToEntityOptional(dto);

      assertNotNull(result);
      assertTrue(result.isPresent());
      assertEquals(dto.getUserRefId(), result.get().getUserRefId());
    }

    @Test
    @DisplayName("should return empty Optional for null DTO")
    void shouldReturnEmptyOptionalForNullDto() {
      Optional<UserRef> result = sut.mapToEntityOptional(null);

      assertNotNull(result);
      assertFalse(result.isPresent());
    }
  }

  @Nested
  @DisplayName("mapToOptional(UserRef)")
  class MapToOptionalEntity {

    @Test
    @DisplayName("should return Optional with DTO for non-null entity")
    void shouldReturnOptionalWithDtoForNonNullEntity() {
      Optional<UserRefDTO> result = sut.mapToDTOOptional(entity);

      assertNotNull(result);
      assertTrue(result.isPresent());
      assertEquals(entity.getUserRefId(), result.get().getUserRefId());
    }

    @Test
    @DisplayName("should return empty Optional for null entity")
    void shouldReturnEmptyOptionalForNullEntity() {
      Optional<UserRefDTO> result = sut.mapToDTOOptional((UserRef) null);

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
      Page<UserRef> page = new PageImpl<>(List.of(entity, entity), PageRequest.of(0, 10), 2);

      Page<UserRefDTO> result = sut.mapToPageDTO(page);

      assertNotNull(result);
      assertEquals(2, result.getContent().size());
      assertEquals(2, result.getTotalElements());
    }

    @Test
    @DisplayName("should return empty page for empty input")
    void shouldReturnEmptyPageForEmptyInput() {
      Page<UserRef> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

      Page<UserRefDTO> result = sut.mapToPageDTO(page);

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
      Page<UserRefDTO> page = new PageImpl<>(List.of(dto, dto), PageRequest.of(0, 10), 2);

      Page<UserRef> result = sut.mapToPage(page);

      assertNotNull(result);
      assertEquals(2, result.getContent().size());
      assertEquals(2, result.getTotalElements());
    }

    @Test
    @DisplayName("should return empty page for empty input")
    void shouldReturnEmptyPageForEmptyInput() {
      Page<UserRefDTO> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

      Page<UserRef> result = sut.mapToPage(page);

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
      Page<UserRef> page = new PageImpl<>(List.of(entity, entity), PageRequest.of(0, 10), 2);

      PageResultResource<UserRefDTO> result = sut.mapToPageResultResource(page);

      assertNotNull(result);
      assertNotNull(result.getContent());
      assertEquals(2, result.getContent().size());
      assertNotNull(result.getPagination());
    }

    @Test
    @DisplayName("should return empty content for empty page")
    void shouldReturnEmptyContentForEmptyPage() {
      Page<UserRef> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

      PageResultResource<UserRefDTO> result = sut.mapToPageResultResource(page);

      assertNotNull(result);
      assertNotNull(result.getContent());
      assertEquals(0, result.getContent().size());
      assertNotNull(result.getPagination());
    }
  }
}
