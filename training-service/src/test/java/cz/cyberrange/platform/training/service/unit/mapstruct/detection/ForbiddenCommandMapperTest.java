package cz.cyberrange.platform.training.service.unit.mapstruct.detection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import cz.cyberrange.platform.training.api.dto.cheatingdetection.ForbiddenCommandDTO;
import cz.cyberrange.platform.training.api.enums.CommandType;
import cz.cyberrange.platform.training.persistence.model.detection.ForbiddenCommand;
import cz.cyberrange.platform.training.service.mapping.mapstruct.EnumMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.detection.ForbiddenCommandMapper;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

/** Unit tests for {@link ForbiddenCommandMapper}. */
@DisplayName("ForbiddenCommandMapper")
class ForbiddenCommandMapperTest {

  private static final Long ENTITY_ID = 42L;
  private static final Long CHEATING_DETECTION_ID = 10L;
  private static final String COMMAND = "nmap";
  private static final CommandType TYPE = CommandType.BASH;

  private ForbiddenCommandMapper sut;
  private EnumMapper enumMapper;

  private ForbiddenCommand entity;
  private ForbiddenCommandDTO dto;

  @BeforeEach
  void setUp() {
    sut = Mappers.getMapper(ForbiddenCommandMapper.class);
    enumMapper = Mappers.getMapper(EnumMapper.class);

    entity = new ForbiddenCommand();
    entity.setId(ENTITY_ID);
    entity.setCommand(COMMAND);
    entity.setType(cz.cyberrange.platform.training.persistence.model.enums.CommandType.BASH);

    dto = new ForbiddenCommandDTO();
    dto.setCommand(COMMAND);
    dto.setType(TYPE);
    dto.setCheatingDetectionId(CHEATING_DETECTION_ID);
  }

  @Nested
  @DisplayName("mapToEntity(ForbiddenCommandDTO)")
  class MapToEntity {

    @Test
    @DisplayName("should map all DTO fields to entity")
    void shouldMapAllFieldsFromDtoToEntity() {
      ForbiddenCommand result = sut.mapToEntity(dto);

      assertNotNull(result);
      assertEquals(dto.getCommand(), result.getCommand());
      assertEquals(enumMapper.mapCommandType(dto.getType()), result.getType());
    }

    @Test
    @DisplayName("should map null command to null")
    void shouldMapNullCommandToNull() {
      dto.setCommand(null);

      ForbiddenCommand result = sut.mapToEntity(dto);

      assertNotNull(result);
      assertNull(result.getCommand());
    }

    @Test
    @DisplayName("should map DTO with empty properties")
    void shouldMapDtoWithEmptyProperties() {
      dto.setCommand("");
      dto.setType(null);

      ForbiddenCommand result = sut.mapToEntity(dto);

      assertNotNull(result);
      assertEquals("", result.getCommand());
      assertNull(result.getType());
    }
  }

  @Nested
  @DisplayName("mapToDTO(ForbiddenCommand)")
  class MapToDTO {

    @Test
    @DisplayName("should map all entity fields to DTO")
    void shouldMapAllFieldsFromEntityToDto() {
      ForbiddenCommandDTO result = sut.mapToDTO(entity);

      assertNotNull(result);
      assertEquals(entity.getCommand(), result.getCommand());
      assertEquals(enumMapper.mapCommandType(entity.getType()), result.getType());
    }

    @Test
    @DisplayName("should map entity with null command")
    void shouldMapNullCommand() {
      entity.setCommand(null);

      ForbiddenCommandDTO result = sut.mapToDTO(entity);

      assertNotNull(result);
      assertNull(result.getCommand());
    }

    @Test
    @DisplayName("should map entity with empty properties")
    void shouldMapEntityWithEmptyProperties() {
      entity.setCommand("");
      entity.setType(null);

      ForbiddenCommandDTO result = sut.mapToDTO(entity);

      assertNotNull(result);
      assertEquals("", result.getCommand());
      assertNull(result.getType());
    }
  }

  @Nested
  @DisplayName("mapToList(Collection)")
  class MapToList {

    @Test
    @DisplayName("should map list of DTOs to list of entities")
    void shouldMapListOfDtosToEntities() {
      List<ForbiddenCommandDTO> dtos = List.of(dto, dto);

      List<ForbiddenCommand> result = sut.mapToList(dtos);

      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals(dto.getCommand(), result.get(0).getCommand());
      assertEquals(enumMapper.mapCommandType(dto.getType()), result.get(0).getType());
    }

    @Test
    @DisplayName("should return empty list for empty input")
    void shouldReturnEmptyListForEmptyInput() {
      List<ForbiddenCommand> result = sut.mapToList(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      List<ForbiddenCommand> result = sut.mapToList(null);

      assertNull(result);
    }

    @Test
    @DisplayName("should map DTOs with empty properties")
    void shouldMapDtosWithEmptyProperties() {
      ForbiddenCommandDTO emptyDto = new ForbiddenCommandDTO();
      emptyDto.setCommand("");
      emptyDto.setType(null);

      List<ForbiddenCommand> result = sut.mapToList(List.of(emptyDto));

      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals("", result.get(0).getCommand());
      assertNull(result.get(0).getType());
    }
  }

  @Nested
  @DisplayName("mapToListDTO(Collection)")
  class MapToListDto {

    @Test
    @DisplayName("should map list of entities to list of DTOs")
    void shouldMapListOfEntitiesToDtos() {
      List<ForbiddenCommand> entities = List.of(entity, entity);

      List<ForbiddenCommandDTO> result = sut.mapToListDTO(entities);

      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals(entity.getCommand(), result.get(0).getCommand());
      assertEquals(enumMapper.mapCommandType(entity.getType()), result.get(0).getType());
    }

    @Test
    @DisplayName("should return empty list for empty input")
    void shouldReturnEmptyListForEmptyInput() {
      List<ForbiddenCommandDTO> result = sut.mapToListDTO(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      List<ForbiddenCommandDTO> result = sut.mapToListDTO(null);

      assertNull(result);
    }

    @Test
    @DisplayName("should map entities with empty properties")
    void shouldMapEntitiesWithEmptyProperties() {
      ForbiddenCommand emptyEntity = new ForbiddenCommand();
      emptyEntity.setCommand("");
      emptyEntity.setType(null);

      List<ForbiddenCommandDTO> result = sut.mapToListDTO(List.of(emptyEntity));

      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals("", result.get(0).getCommand());
      assertNull(result.get(0).getType());
    }
  }
}
