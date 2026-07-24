package cz.cyberrange.platform.training.service.unit.mapstruct;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import cz.cyberrange.platform.training.api.dto.export.AttachmentExportDTO;
import cz.cyberrange.platform.training.api.dto.imports.AttachmentImportDTO;
import cz.cyberrange.platform.training.persistence.model.Attachment;
import cz.cyberrange.platform.training.service.mapping.mapstruct.AttachmentMapper;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

/**
 * Unit tests for {@link AttachmentMapper}.
 *
 * <p>Tests all mapping methods for Attachment entity and DTOs. Uses {@link Mappers#getMapper} since
 * the mapper has no Spring dependencies (empty {@code uses}).
 */
@DisplayName("AttachmentMapper")
class AttachmentMapperTest {

  private static final Long ENTITY_ID = 42L;
  private static final String CONTENT = "https://example.com/file.pdf";
  private static final LocalDateTime CREATION_TIME = LocalDateTime.of(2024, 1, 15, 10, 30);

  private AttachmentMapper sut;

  private Attachment entity;
  private AttachmentImportDTO importDto;
  private AttachmentExportDTO exportDto;

  @BeforeEach
  void setUp() {
    sut = Mappers.getMapper(AttachmentMapper.class);

    entity = new Attachment();
    entity.setId(ENTITY_ID);
    entity.setContent(CONTENT);
    entity.setCreationTime(CREATION_TIME);

    importDto = new AttachmentImportDTO();
    importDto.setContent(CONTENT);

    exportDto = new AttachmentExportDTO();
    exportDto.setContent(CONTENT);
  }

  @Nested
  @DisplayName("mapImportDTOToEntity(AttachmentImportDTO)")
  class MapImportDTOToEntity {

    @Test
    @DisplayName("should map all DTO fields to entity")
    void shouldMapAllFieldsFromDtoToEntity() {
      Attachment result = sut.mapImportDTOToEntity(importDto);

      assertNotNull(result);
      assertEquals(importDto.getContent(), result.getContent());
    }

    @Test
    @DisplayName("should map null content to null")
    void shouldMapNullContentToNull() {
      importDto.setContent(null);

      Attachment result = sut.mapImportDTOToEntity(importDto);

      assertNotNull(result);
      assertNull(result.getContent());
    }

    @Test
    @DisplayName("should map DTO with empty content")
    void shouldMapDtoWithEmptyContent() {
      importDto.setContent("");

      Attachment result = sut.mapImportDTOToEntity(importDto);

      assertNotNull(result);
      assertEquals("", result.getContent());
    }
  }

  @Nested
  @DisplayName("mapToExportDTO(Attachment)")
  class MapToExportDTO {

    @Test
    @DisplayName("should map all entity fields to export DTO")
    void shouldMapAllFieldsFromEntityToDto() {
      AttachmentExportDTO result = sut.mapToExportDTO(entity);

      assertNotNull(result);
      assertEquals(entity.getContent(), result.getContent());
    }

    @Test
    @DisplayName("should map entity with null content")
    void shouldMapNullContent() {
      entity.setContent(null);

      AttachmentExportDTO result = sut.mapToExportDTO(entity);

      assertNotNull(result);
      assertNull(result.getContent());
    }

    @Test
    @DisplayName("should map entity with empty content")
    void shouldMapEntityWithEmptyContent() {
      entity.setContent("");

      AttachmentExportDTO result = sut.mapToExportDTO(entity);

      assertNotNull(result);
      assertEquals("", result.getContent());
    }
  }

  @Nested
  @DisplayName("mapImportDTOsToList(Collection)")
  class MapImportDTOsToList {

    @Test
    @DisplayName("should map list of DTOs to list of entities")
    void shouldMapListOfDTOsToEntities() {
      List<AttachmentImportDTO> dtos = List.of(importDto, importDto);

      List<Attachment> result = sut.mapImportDTOsToList(dtos);

      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals(importDto.getContent(), result.get(0).getContent());
    }

    @Test
    @DisplayName("should return empty list for empty input")
    void shouldReturnEmptyListForEmptyInput() {
      List<Attachment> result = sut.mapImportDTOsToList(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      List<Attachment> result = sut.mapImportDTOsToList(null);

      assertNull(result);
    }

    @Test
    @DisplayName("should map DTOs with empty properties")
    void shouldMapDtosWithEmptyProperties() {
      AttachmentImportDTO emptyDto = new AttachmentImportDTO();
      emptyDto.setContent("");

      List<Attachment> result = sut.mapImportDTOsToList(List.of(emptyDto));

      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals("", result.get(0).getContent());
    }
  }

  @Nested
  @DisplayName("mapToListExportDTO(Collection)")
  class MapToListExportDTO {

    @Test
    @DisplayName("should map list of entities to list of export DTOs")
    void shouldMapListOfEntitiesToDTOs() {
      List<Attachment> entities = List.of(entity, entity);

      List<AttachmentExportDTO> result = sut.mapToListExportDTO(entities);

      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals(entity.getContent(), result.get(0).getContent());
    }

    @Test
    @DisplayName("should return empty list for empty input")
    void shouldReturnEmptyListForEmptyInput() {
      List<AttachmentExportDTO> result = sut.mapToListExportDTO(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      List<AttachmentExportDTO> result = sut.mapToListExportDTO(null);

      assertNull(result);
    }

    @Test
    @DisplayName("should map entities with empty content")
    void shouldMapEntitiesWithEmptyContent() {
      Attachment emptyEntity = new Attachment();
      emptyEntity.setContent("");

      List<AttachmentExportDTO> result = sut.mapToListExportDTO(List.of(emptyEntity));

      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals("", result.get(0).getContent());
    }
  }
}
