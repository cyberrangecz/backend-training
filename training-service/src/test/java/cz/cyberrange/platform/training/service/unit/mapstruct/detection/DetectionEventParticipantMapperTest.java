package cz.cyberrange.platform.training.service.unit.mapstruct.detection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import cz.cyberrange.platform.training.api.dto.cheatingdetection.DetectionEventParticipantDTO;
import cz.cyberrange.platform.training.persistence.model.detection.DetectionEventParticipant;
import cz.cyberrange.platform.training.service.mapping.mapstruct.detection.DetectionEventParticipantMapper;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

/** Unit tests for {@link DetectionEventParticipantMapper}. */
@DisplayName("DetectionEventParticipantMapper")
class DetectionEventParticipantMapperTest {

  private static final Long ENTITY_ID = 42L;
  private static final Long USER_ID = 100L;
  private static final Long DETECTION_EVENT_ID = 10L;
  private static final Long CHEATING_DETECTION_ID = 5L;
  private static final String IP_ADDRESS = "1.1.1.1";
  private static final String PARTICIPANT_NAME = "John Doe";
  private static final Long SOLVED_IN_TIME = 20L;
  private static final LocalDateTime OCCURRED_AT = LocalDateTime.of(2022, 1, 1, 5, 55, 23);

  private DetectionEventParticipantMapper sut;

  private DetectionEventParticipant entity;
  private DetectionEventParticipantDTO dto;

  @BeforeEach
  void setUp() {
    sut = Mappers.getMapper(DetectionEventParticipantMapper.class);

    entity = new DetectionEventParticipant();
    entity.setId(ENTITY_ID);
    entity.setUserId(USER_ID);
    entity.setDetectionEventId(DETECTION_EVENT_ID);
    entity.setCheatingDetectionId(CHEATING_DETECTION_ID);
    entity.setIpAddress(IP_ADDRESS);
    entity.setParticipantName(PARTICIPANT_NAME);
    entity.setSolvedInTime(SOLVED_IN_TIME);
    entity.setOccurredAt(OCCURRED_AT);

    dto = new DetectionEventParticipantDTO();
    dto.setUserId(USER_ID);
    dto.setDetectionEventId(DETECTION_EVENT_ID);
    dto.setIpAddress(IP_ADDRESS);
    dto.setParticipantName(PARTICIPANT_NAME);
    dto.setSolvedInTime(SOLVED_IN_TIME);
    dto.setOccurredAt(OCCURRED_AT);
  }

  @Nested
  @DisplayName("mapToEntity(DetectionEventParticipantDTO)")
  class MapToEntity {

    @Test
    @DisplayName("should map all DTO fields to entity")
    void shouldMapAllFieldsFromDtoToEntity() {
      DetectionEventParticipant result = sut.mapToEntity(dto);

      assertNotNull(result);
      assertEquals(dto.getIpAddress(), result.getIpAddress());
      assertEquals(dto.getOccurredAt(), result.getOccurredAt());
      assertEquals(dto.getParticipantName(), result.getParticipantName());
      assertEquals(dto.getSolvedInTime(), result.getSolvedInTime());
      assertEquals(dto.getUserId(), result.getUserId());
      assertEquals(dto.getDetectionEventId(), result.getDetectionEventId());
    }

    @Test
    @DisplayName("should map null ipAddress to null")
    void shouldMapNullIpAddressToNull() {
      dto.setIpAddress(null);

      DetectionEventParticipant result = sut.mapToEntity(dto);

      assertNotNull(result);
      assertNull(result.getIpAddress());
    }

    @Test
    @DisplayName("should map DTO with empty properties")
    void shouldMapDtoWithEmptyProperties() {
      dto.setIpAddress(null);
      dto.setOccurredAt(null);
      dto.setParticipantName("");
      dto.setSolvedInTime(null);
      dto.setUserId(null);
      dto.setDetectionEventId(null);

      DetectionEventParticipant result = sut.mapToEntity(dto);

      assertNotNull(result);
      assertNull(result.getIpAddress());
      assertNull(result.getOccurredAt());
      assertEquals("", result.getParticipantName());
      assertNull(result.getSolvedInTime());
      assertNull(result.getUserId());
      assertNull(result.getDetectionEventId());
    }
  }

  @Nested
  @DisplayName("mapToDTO(DetectionEventParticipant)")
  class MapToDTO {

    @Test
    @DisplayName("should map all entity fields to DTO")
    void shouldMapAllFieldsFromEntityToDto() {
      DetectionEventParticipantDTO result = sut.mapToDTO(entity);

      assertNotNull(result);
      assertEquals(entity.getIpAddress(), result.getIpAddress());
      assertEquals(entity.getOccurredAt(), result.getOccurredAt());
      assertEquals(entity.getParticipantName(), result.getParticipantName());
      assertEquals(entity.getSolvedInTime(), result.getSolvedInTime());
      assertEquals(entity.getUserId(), result.getUserId());
      assertEquals(entity.getDetectionEventId(), result.getDetectionEventId());
    }

    @Test
    @DisplayName("should map entity with null ipAddress")
    void shouldMapNullIpAddress() {
      entity.setIpAddress(null);

      DetectionEventParticipantDTO result = sut.mapToDTO(entity);

      assertNotNull(result);
      assertNull(result.getIpAddress());
    }

    @Test
    @DisplayName("should map entity with empty properties")
    void shouldMapEntityWithEmptyProperties() {
      entity.setIpAddress(null);
      entity.setOccurredAt(null);
      entity.setParticipantName("");
      entity.setSolvedInTime(null);
      entity.setUserId(null);
      entity.setDetectionEventId(null);

      DetectionEventParticipantDTO result = sut.mapToDTO(entity);

      assertNotNull(result);
      assertNull(result.getIpAddress());
      assertNull(result.getOccurredAt());
      assertEquals("", result.getParticipantName());
      assertNull(result.getSolvedInTime());
      assertNull(result.getUserId());
      assertNull(result.getDetectionEventId());
    }
  }

  @Nested
  @DisplayName("mapToList(Collection)")
  class MapToList {

    @Test
    @DisplayName("should map list of DTOs to list of entities")
    void shouldMapListOfDtosToEntities() {
      List<DetectionEventParticipantDTO> dtos = List.of(dto, dto);

      List<DetectionEventParticipant> result = sut.mapToList(dtos);

      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals(dto.getParticipantName(), result.get(0).getParticipantName());
      assertEquals(dto.getUserId(), result.get(0).getUserId());
    }

    @Test
    @DisplayName("should return empty list for empty input")
    void shouldReturnEmptyListForEmptyInput() {
      List<DetectionEventParticipant> result = sut.mapToList(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      List<DetectionEventParticipant> result = sut.mapToList(null);

      assertNull(result);
    }

    @Test
    @DisplayName("should map DTOs with empty properties")
    void shouldMapDtosWithEmptyProperties() {
      DetectionEventParticipantDTO emptyDto = new DetectionEventParticipantDTO();
      emptyDto.setParticipantName("");
      emptyDto.setUserId(null);

      List<DetectionEventParticipant> result = sut.mapToList(List.of(emptyDto));

      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals("", result.get(0).getParticipantName());
      assertNull(result.get(0).getUserId());
    }
  }

  @Nested
  @DisplayName("mapToListDTO(Collection)")
  class MapToListDto {

    @Test
    @DisplayName("should map list of entities to list of DTOs")
    void shouldMapListOfEntitiesToDtos() {
      List<DetectionEventParticipant> entities = List.of(entity, entity);

      List<DetectionEventParticipantDTO> result = sut.mapToListDTO(entities);

      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals(entity.getParticipantName(), result.get(0).getParticipantName());
      assertEquals(entity.getUserId(), result.get(0).getUserId());
    }

    @Test
    @DisplayName("should return empty list for empty input")
    void shouldReturnEmptyListForEmptyInput() {
      List<DetectionEventParticipantDTO> result = sut.mapToListDTO(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      List<DetectionEventParticipantDTO> result = sut.mapToListDTO(null);

      assertNull(result);
    }

    @Test
    @DisplayName("should map entities with empty properties")
    void shouldMapEntitiesWithEmptyProperties() {
      DetectionEventParticipant emptyEntity = new DetectionEventParticipant();
      emptyEntity.setParticipantName("");
      emptyEntity.setUserId(null);

      List<DetectionEventParticipantDTO> result = sut.mapToListDTO(List.of(emptyEntity));

      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals("", result.get(0).getParticipantName());
      assertNull(result.get(0).getUserId());
    }
  }
}
