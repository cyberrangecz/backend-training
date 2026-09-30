package cz.cyberrange.platform.training.service.unit.mapstruct;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import cz.cyberrange.platform.training.api.dto.export.InfoLevelExportDTO;
import cz.cyberrange.platform.training.api.dto.imports.InfoLevelImportDTO;
import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import cz.cyberrange.platform.training.service.mapping.mapstruct.LevelMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

/** Unit tests for {@link LevelMapper}. */
@DisplayName("LevelMapper")
class LevelMapperTest {

  private static final String TITLE = "Info Level";
  private static final String CONTENT = "Level content";
  private static final int ESTIMATED_DURATION = 15;
  private static final int MINIMAL_POSSIBLE_SOLVE_TIME = 7;

  private LevelMapper sut;

  private InfoLevelImportDTO importDto;
  private InfoLevel entity;

  @BeforeEach
  void setUp() {
    sut = Mappers.getMapper(LevelMapper.class);

    importDto = new InfoLevelImportDTO();
    importDto.setTitle(TITLE);
    importDto.setContent(CONTENT);
    importDto.setEstimatedDuration(ESTIMATED_DURATION);
    importDto.setMinimalPossibleSolveTime(MINIMAL_POSSIBLE_SOLVE_TIME);

    entity = new InfoLevel();
    entity.setTitle(TITLE);
    entity.setContent(CONTENT);
    entity.setEstimatedDuration((long) ESTIMATED_DURATION);
    entity.setMinimalPossibleSolveTime((long) MINIMAL_POSSIBLE_SOLVE_TIME);
  }

  @Nested
  @DisplayName("minimal possible solve time")
  class MinimalPossibleSolveTime {

    @Test
    @DisplayName("should carry the imported value onto the entity")
    void shouldCarryImportedValueOntoEntity() {
      InfoLevel result = sut.mapImportToEntity(importDto);

      assertNotNull(result);
      assertEquals(
          Long.valueOf(MINIMAL_POSSIBLE_SOLVE_TIME), result.getMinimalPossibleSolveTime());
    }

    @Test
    @DisplayName("should leave the entity value unset when the imported level omitted it")
    void shouldLeaveEntityValueUnsetWhenOmitted() {
      InfoLevelImportDTO withoutSolveTime = new InfoLevelImportDTO();
      withoutSolveTime.setTitle(TITLE);
      withoutSolveTime.setContent(CONTENT);
      withoutSolveTime.setEstimatedDuration(ESTIMATED_DURATION);

      InfoLevel result = sut.mapImportToEntity(withoutSolveTime);

      assertNotNull(result);
      assertNull(result.getMinimalPossibleSolveTime());
    }

    @Test
    @DisplayName("should carry the entity value into the export")
    void shouldCarryEntityValueIntoExport() {
      InfoLevelExportDTO result = sut.mapToExportInfoLevelDTO(entity);

      assertNotNull(result);
      assertEquals(
          Integer.valueOf(MINIMAL_POSSIBLE_SOLVE_TIME), result.getMinimalPossibleSolveTime());
    }

    @Test
    @DisplayName("should export no value when the entity holds none")
    void shouldExportNoValueWhenEntityHoldsNone() {
      entity.setMinimalPossibleSolveTime(null);

      InfoLevelExportDTO result = sut.mapToExportInfoLevelDTO(entity);

      assertNotNull(result);
      assertNull(result.getMinimalPossibleSolveTime());
    }
  }
}
