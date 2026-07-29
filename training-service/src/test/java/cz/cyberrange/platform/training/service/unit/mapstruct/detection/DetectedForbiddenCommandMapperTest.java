package cz.cyberrange.platform.training.service.unit.mapstruct.detection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import cz.cyberrange.platform.training.api.dto.cheatingdetection.DetectedForbiddenCommandDTO;
import cz.cyberrange.platform.training.api.enums.CommandType;
import cz.cyberrange.platform.training.persistence.model.detection.DetectedForbiddenCommand;
import cz.cyberrange.platform.training.service.mapping.mapstruct.EnumMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.detection.DetectedForbiddenCommandMapper;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

/** Unit tests for {@link DetectedForbiddenCommandMapper}. */
@DisplayName("DetectedForbiddenCommandMapper")
class DetectedForbiddenCommandMapperTest {

  private static final Long ENTITY_ID = 42L;
  private static final Long DETECTION_EVENT_ID = 10L;
  private static final String COMMAND = "nmap";
  private static final CommandType TYPE = CommandType.BASH;
  private static final String HOSTNAME = "attacker";
  private static final LocalDateTime OCCURRED_AT = LocalDateTime.of(2022, 1, 1, 5, 55, 23);

  private DetectedForbiddenCommandMapper sut;
  private EnumMapper enumMapper;

  private DetectedForbiddenCommand entity;
  private DetectedForbiddenCommandDTO dto;

  @BeforeEach
  void setUp() {
    sut = Mappers.getMapper(DetectedForbiddenCommandMapper.class);
    enumMapper = Mappers.getMapper(EnumMapper.class);

    entity = new DetectedForbiddenCommand();
    entity.setId(ENTITY_ID);
    entity.setCommand(COMMAND);
    entity.setType(cz.cyberrange.platform.training.persistence.model.enums.CommandType.BASH);
    entity.setDetectionEventId(DETECTION_EVENT_ID);
    entity.setHostname(HOSTNAME);
    entity.setOccurredAt(OCCURRED_AT);

    dto = new DetectedForbiddenCommandDTO();
    dto.setCommand(COMMAND);
    dto.setType(TYPE);
    dto.setHostname(HOSTNAME);
    dto.setOccurredAt(OCCURRED_AT);
  }

  @Nested
  @DisplayName("mapToEntity(DetectedForbiddenCommandDTO)")
  class MapToEntity {

    @Test
    @DisplayName("should map all DTO fields to entity")
    void shouldMapAllFieldsFromDtoToEntity() {
      DetectedForbiddenCommand result = sut.mapToEntity(dto);

      assertNotNull(result);
      assertEquals(dto.getCommand(), result.getCommand());
      assertEquals(enumMapper.mapCommandType(dto.getType()), result.getType());
      assertEquals(dto.getHostname(), result.getHostname());
      assertEquals(dto.getOccurredAt(), result.getOccurredAt());
    }

    @Test
    @DisplayName("should map null command to null")
    void shouldMapNullCommandToNull() {
      dto.setCommand(null);

      DetectedForbiddenCommand result = sut.mapToEntity(dto);

      assertNotNull(result);
      assertNull(result.getCommand());
    }

    @Test
    @DisplayName("should map DTO with empty properties")
    void shouldMapDtoWithEmptyProperties() {
      dto.setCommand("");
      dto.setType(null);
      dto.setHostname(null);
      dto.setOccurredAt(null);

      DetectedForbiddenCommand result = sut.mapToEntity(dto);

      assertNotNull(result);
      assertEquals("", result.getCommand());
      assertNull(result.getType());
      assertNull(result.getHostname());
      assertNull(result.getOccurredAt());
    }
  }

  @Nested
  @DisplayName("mapToDTO(DetectedForbiddenCommand)")
  class MapToDTO {

    @Test
    @DisplayName("should map all entity fields to DTO")
    void shouldMapAllFieldsFromEntityToDto() {
      DetectedForbiddenCommandDTO result = sut.mapToDTO(entity);

      assertNotNull(result);
      assertEquals(entity.getCommand(), result.getCommand());
      assertEquals(enumMapper.mapCommandType(entity.getType()), result.getType());
      assertEquals(entity.getHostname(), result.getHostname());
      assertEquals(entity.getOccurredAt(), result.getOccurredAt());
    }

    @Test
    @DisplayName("should map entity with null command")
    void shouldMapNullCommand() {
      entity.setCommand(null);

      DetectedForbiddenCommandDTO result = sut.mapToDTO(entity);

      assertNotNull(result);
      assertNull(result.getCommand());
    }

    @Test
    @DisplayName("should map entity with empty properties")
    void shouldMapEntityWithEmptyProperties() {
      entity.setCommand("");
      entity.setType(null);
      entity.setHostname(null);
      entity.setOccurredAt(null);

      DetectedForbiddenCommandDTO result = sut.mapToDTO(entity);

      assertNotNull(result);
      assertEquals("", result.getCommand());
      assertNull(result.getType());
      assertNull(result.getHostname());
      assertNull(result.getOccurredAt());
    }
  }

  @Nested
  @DisplayName("mapToList(Collection)")
  class MapToList {

    @Test
    @DisplayName("should map list of DTOs to list of entities")
    void shouldMapListOfDtosToEntities() {
      List<DetectedForbiddenCommandDTO> dtos = List.of(dto, dto);

      List<DetectedForbiddenCommand> result = sut.mapToList(dtos);

      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals(dto.getCommand(), result.get(0).getCommand());
      assertEquals(enumMapper.mapCommandType(dto.getType()), result.get(0).getType());
    }

    @Test
    @DisplayName("should return empty list for empty input")
    void shouldReturnEmptyListForEmptyInput() {
      List<DetectedForbiddenCommand> result = sut.mapToList(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      List<DetectedForbiddenCommand> result = sut.mapToList(null);

      assertNull(result);
    }

    @Test
    @DisplayName("should map DTOs with empty properties")
    void shouldMapDtosWithEmptyProperties() {
      DetectedForbiddenCommandDTO emptyDto = new DetectedForbiddenCommandDTO();
      emptyDto.setCommand("");
      emptyDto.setType(null);

      List<DetectedForbiddenCommand> result = sut.mapToList(List.of(emptyDto));

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
      List<DetectedForbiddenCommand> entities = List.of(entity, entity);

      List<DetectedForbiddenCommandDTO> result = sut.mapToListDTO(entities);

      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals(entity.getCommand(), result.get(0).getCommand());
      assertEquals(enumMapper.mapCommandType(entity.getType()), result.get(0).getType());
    }

    @Test
    @DisplayName("should return empty list for empty input")
    void shouldReturnEmptyListForEmptyInput() {
      List<DetectedForbiddenCommandDTO> result = sut.mapToListDTO(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      List<DetectedForbiddenCommandDTO> result = sut.mapToListDTO(null);

      assertNull(result);
    }

    @Test
    @DisplayName("should map entities with empty properties")
    void shouldMapEntitiesWithEmptyProperties() {
      DetectedForbiddenCommand emptyEntity = new DetectedForbiddenCommand();
      emptyEntity.setCommand("");
      emptyEntity.setType(null);

      List<DetectedForbiddenCommandDTO> result = sut.mapToListDTO(List.of(emptyEntity));

      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals("", result.get(0).getCommand());
      assertNull(result.get(0).getType());
    }
  }
}
