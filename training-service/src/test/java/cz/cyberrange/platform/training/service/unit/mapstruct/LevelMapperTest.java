package cz.cyberrange.platform.training.service.unit.mapstruct;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import cz.cyberrange.platform.training.api.dto.AbstractLevelCreateDTO;
import cz.cyberrange.platform.training.api.dto.AbstractLevelUpdateDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.AssessmentLevelContentDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.AssessmentLevelCreateDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.question.ExtendedMatchingOptionDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.question.ExtendedMatchingStatementDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.question.QuestionDTO;
import cz.cyberrange.platform.training.api.dto.export.InfoLevelExportDTO;
import cz.cyberrange.platform.training.api.dto.imports.AbstractLevelImportDTO;
import cz.cyberrange.platform.training.api.dto.imports.AssessmentLevelImportDTO;
import cz.cyberrange.platform.training.api.dto.imports.InfoLevelImportDTO;
import cz.cyberrange.platform.training.api.dto.imports.TrainingLevelImportDTO;
import cz.cyberrange.platform.training.api.dto.infolevel.InfoLevelCreateDTO;
import cz.cyberrange.platform.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.cyberrange.platform.training.api.dto.traininglevel.TrainingLevelCreateDTO;
import cz.cyberrange.platform.training.api.dto.traininglevel.TrainingLevelUpdateDTO;
import cz.cyberrange.platform.training.api.enums.AssessmentType;
import cz.cyberrange.platform.training.api.enums.QuestionType;
import cz.cyberrange.platform.training.persistence.model.AbstractLevel;
import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.question.ExtendedMatchingOption;
import cz.cyberrange.platform.training.persistence.model.question.Question;
import cz.cyberrange.platform.training.service.mapping.mapstruct.AttachmentMapperImpl;
import cz.cyberrange.platform.training.service.mapping.mapstruct.EnumMapperImpl;
import cz.cyberrange.platform.training.service.mapping.mapstruct.HintMapperImpl;
import cz.cyberrange.platform.training.service.mapping.mapstruct.LevelMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.LevelMapperImpl;
import cz.cyberrange.platform.training.service.mapping.mapstruct.MitreTechniqueMapperImpl;
import cz.cyberrange.platform.training.service.mapping.mapstruct.QuestionMapperImpl;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * Tests of {@link LevelMapper} run against a Spring context holding the generated implementations
 * of the mapper and of the mappers it uses.
 */
@DisplayName("LevelMapper")
@SpringJUnitConfig(
    classes = {
      EnumMapperImpl.class,
      LevelMapperImpl.class,
      HintMapperImpl.class,
      QuestionMapperImpl.class,
      AttachmentMapperImpl.class,
      MitreTechniqueMapperImpl.class
    })
class LevelMapperTest {

  private static final String TITLE = "Info Level";
  private static final String CONTENT = "Level content";
  private static final int ESTIMATED_DURATION = 15;
  private static final int MINIMAL_POSSIBLE_SOLVE_TIME = 7;

  @Autowired private LevelMapper sut;

  private InfoLevelImportDTO importDto;
  private InfoLevel entity;

  @BeforeEach
  void setUp() {
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
      assertEquals(Long.valueOf(MINIMAL_POSSIBLE_SOLVE_TIME), result.getMinimalPossibleSolveTime());
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

  @Nested
  @DisplayName("mapUpdateToEntity(AbstractLevelUpdateDTO)")
  class MapUpdateToEntity {

    @Test
    @DisplayName("should map an info level update to an info level")
    void shouldMapInfoLevelUpdateToInfoLevel() {
      InfoLevelUpdateDTO infoUpdate = new InfoLevelUpdateDTO();
      infoUpdate.setId(9L);
      infoUpdate.setTitle(TITLE);
      infoUpdate.setContent(CONTENT);
      infoUpdate.setEstimatedDuration(ESTIMATED_DURATION);
      AbstractLevelUpdateDTO update = infoUpdate;

      AbstractLevel result = sut.mapUpdateToEntity(update);

      InfoLevel infoLevel = assertInstanceOf(InfoLevel.class, result);
      assertEquals(Long.valueOf(9L), infoLevel.getId());
      assertEquals(TITLE, infoLevel.getTitle());
      assertEquals(CONTENT, infoLevel.getContent());
      assertEquals(ESTIMATED_DURATION, infoLevel.getEstimatedDuration());
    }

    @Test
    @DisplayName("should leave the id unset when the update carries none")
    void shouldLeaveIdUnsetWhenUpdateCarriesNone() {
      InfoLevelUpdateDTO infoUpdate = new InfoLevelUpdateDTO();
      infoUpdate.setTitle(TITLE);
      infoUpdate.setContent(CONTENT);
      AbstractLevelUpdateDTO update = infoUpdate;

      AbstractLevel result = sut.mapUpdateToEntity(update);

      assertNull(result.getId());
    }

    @Test
    @DisplayName("should keep the id of a training level update")
    void shouldKeepIdOfTrainingLevelUpdate() {
      TrainingLevelUpdateDTO trainingUpdate = new TrainingLevelUpdateDTO();
      trainingUpdate.setId(4L);
      trainingUpdate.setTitle(TITLE);
      trainingUpdate.setAnswer(" ");
      AbstractLevelUpdateDTO update = trainingUpdate;

      AbstractLevel result = sut.mapUpdateToEntity(update);

      TrainingLevel trainingLevel = assertInstanceOf(TrainingLevel.class, result);
      assertEquals(Long.valueOf(4L), trainingLevel.getId());
      assertNull(trainingLevel.getAnswer());
    }
  }

  @Nested
  @DisplayName("mapCreateToEntity(AbstractLevelCreateDTO)")
  class MapCreateToEntity {

    @Test
    @DisplayName("should map an info level create payload to an info level without an id")
    void shouldMapInfoLevelCreateToInfoLevelWithoutId() {
      InfoLevelCreateDTO infoCreate = new InfoLevelCreateDTO();
      infoCreate.setTitle(TITLE);
      infoCreate.setContent(CONTENT);
      infoCreate.setEstimatedDuration(ESTIMATED_DURATION);
      AbstractLevelCreateDTO create = infoCreate;

      AbstractLevel result = sut.mapCreateToEntity(create);

      InfoLevel infoLevel = assertInstanceOf(InfoLevel.class, result);
      assertNull(infoLevel.getId());
      assertEquals(TITLE, infoLevel.getTitle());
      assertEquals(CONTENT, infoLevel.getContent());
      assertEquals(ESTIMATED_DURATION, infoLevel.getEstimatedDuration());
    }

    @Test
    @DisplayName("should reduce a blank answer and a blank answer variable name to null")
    void shouldReduceBlankAnswersToNull() {
      TrainingLevelCreateDTO trainingCreate = new TrainingLevelCreateDTO();
      trainingCreate.setTitle(TITLE);
      trainingCreate.setAnswer("  ");
      trainingCreate.setAnswerVariableName("");
      AbstractLevelCreateDTO create = trainingCreate;

      AbstractLevel result = sut.mapCreateToEntity(create);

      TrainingLevel trainingLevel = assertInstanceOf(TrainingLevel.class, result);
      assertNull(trainingLevel.getAnswer());
      assertNull(trainingLevel.getAnswerVariableName());
    }

    @Test
    @DisplayName("should keep a filled answer of a training level")
    void shouldKeepFilledAnswer() {
      TrainingLevelCreateDTO create = new TrainingLevelCreateDTO();
      create.setTitle(TITLE);
      create.setAnswer("secret");

      TrainingLevel result = sut.mapCreateToEntity(create);

      assertEquals("secret", result.getAnswer());
    }
  }

  @Nested
  @DisplayName("mapImportToEntity(AbstractLevelImportDTO)")
  class MapImportToEntity {

    @Test
    @DisplayName("should map each import subtype to the entity of its level type")
    void shouldMapEachImportSubtypeToItsEntity() {
      AbstractLevelImportDTO info = importDto;
      AbstractLevelImportDTO training = new TrainingLevelImportDTO();
      AssessmentLevelImportDTO assessmentImport = new AssessmentLevelImportDTO();
      assessmentImport.setAssessmentType(AssessmentType.QUESTIONNAIRE);
      AbstractLevelImportDTO assessment = assessmentImport;

      assertInstanceOf(InfoLevel.class, sut.mapImportToEntity(info));
      assertInstanceOf(TrainingLevel.class, sut.mapImportToEntity(training));
      assertInstanceOf(AssessmentLevel.class, sut.mapImportToEntity(assessment));
    }

    @Test
    @DisplayName("should reduce a blank answer of an imported training level to null")
    void shouldReduceBlankAnswerOfImportedTrainingLevelToNull() {
      TrainingLevelImportDTO training = new TrainingLevelImportDTO();
      training.setTitle(TITLE);
      training.setAnswer("");
      training.setAnswerVariableName(" ");

      TrainingLevel result = sut.mapImportToEntity(training);

      assertNull(result.getAnswer());
      assertNull(result.getAnswerVariableName());
    }
  }

  @Nested
  @DisplayName("correct option binding of a test assessment")
  class CorrectOptionBinding {

    /** An extended matching statement of a payload: its order and the option it names correct. */
    private record StatementPayload(int order, Integer correctOptionOrder) {}

    @Test
    @DisplayName("should bind nested items by position and questions in ascending question order")
    void shouldBindNestedItemsByPosition() {
      AssessmentLevelCreateDTO create = createAssessmentPayload(AssessmentType.TEST);
      create.setQuestions(
          List.of(
              buildMatchingQuestion(
                  5, List.of(0, 1, 2), new StatementPayload(7, 2), new StatementPayload(9, 0)),
              buildMatchingQuestion(2, List.of(3, 4), new StatementPayload(1, 1))));

      AssessmentLevel result = sut.mapCreateToEntity(create);

      assertCorrectOption(result.getQuestions().get(0), 0, 1);
      assertCorrectOption(result.getQuestions().get(1), 0, 2);
      assertCorrectOption(result.getQuestions().get(1), 1, 0);
    }

    @Test
    @DisplayName("should bind an update payload and keep its id")
    void shouldBindUpdatePayloadAndKeepItsId() {
      AssessmentLevelUpdateDTO assessmentUpdate = new AssessmentLevelUpdateDTO();
      fillAssessment(assessmentUpdate, AssessmentType.TEST);
      assessmentUpdate.setId(11L);
      assessmentUpdate.setQuestions(
          List.of(buildMatchingQuestion(8, List.of(0, 1), new StatementPayload(4, 1))));
      AbstractLevelUpdateDTO update = assessmentUpdate;

      AbstractLevel result = sut.mapUpdateToEntity(update);

      AssessmentLevel assessmentLevel = assertInstanceOf(AssessmentLevel.class, result);
      assertEquals(Long.valueOf(11L), assessmentLevel.getId());
      assertCorrectOption(assessmentLevel.getQuestions().get(0), 0, 1);
    }

    @Test
    @DisplayName("should bind an imported payload")
    void shouldBindImportedPayload() {
      AssessmentLevelImportDTO imported = new AssessmentLevelImportDTO();
      imported.setTitle(TITLE);
      imported.setInstructions("Read");
      imported.setAssessmentType(AssessmentType.TEST);
      imported.setQuestions(
          List.of(
              buildMatchingQuestion(
                  6, List.of(0, 1), new StatementPayload(2, 1), new StatementPayload(3, 0))));

      AssessmentLevel result = sut.mapImportToEntity(imported);

      assertCorrectOption(result.getQuestions().get(0), 0, 1);
      assertCorrectOption(result.getQuestions().get(0), 1, 0);
    }

    @ParameterizedTest(name = "{0} assessment, correct option {1}")
    @CsvSource({"TEST,", "TEST,2", "TEST,-1", "QUESTIONNAIRE,9"})
    @DisplayName("should leave a statement unbound when it names no option of its question")
    void shouldLeaveStatementUnbound(AssessmentType type, Integer correctOptionOrder) {
      AssessmentLevelCreateDTO create = createAssessmentPayload(type);
      create.setQuestions(
          List.of(
              buildMatchingQuestion(
                  0, List.of(0, 1), new StatementPayload(0, correctOptionOrder))));

      AssessmentLevel result = sut.mapCreateToEntity(create);

      assertStatementUnbound(result.getQuestions().get(0), 0);
    }

    @Test
    @DisplayName("should map a test assessment without questions")
    void shouldMapTestAssessmentWithoutQuestions() {
      AssessmentLevelCreateDTO create = createAssessmentPayload(AssessmentType.TEST);
      create.setQuestions(null);

      assertNotNull(sut.mapCreateToEntity(create));
    }

    private AssessmentLevelCreateDTO createAssessmentPayload(AssessmentType type) {
      AssessmentLevelCreateDTO create = new AssessmentLevelCreateDTO();
      fillAssessment(create, type);
      return create;
    }

    private void fillAssessment(AssessmentLevelContentDTO level, AssessmentType type) {
      level.setTitle(TITLE);
      level.setInstructions("Read");
      level.setType(type);
    }

    private QuestionDTO buildMatchingQuestion(
        int order, List<Integer> optionOrders, StatementPayload... statementPayloads) {
      QuestionDTO question = new QuestionDTO();
      question.setQuestionType(QuestionType.EMI);
      question.setOrder(order);
      question.setText("Match");
      List<ExtendedMatchingOptionDTO> options = new ArrayList<>();
      for (int optionOrder : optionOrders) {
        ExtendedMatchingOptionDTO option = new ExtendedMatchingOptionDTO();
        option.setText("Option " + optionOrder);
        option.setOrder(optionOrder);
        options.add(option);
      }
      question.setExtendedMatchingOptions(options);
      List<ExtendedMatchingStatementDTO> statements = new ArrayList<>();
      for (StatementPayload statementPayload : statementPayloads) {
        ExtendedMatchingStatementDTO statement = new ExtendedMatchingStatementDTO();
        statement.setText("Statement " + statements.size());
        statement.setOrder(statementPayload.order());
        statement.setCorrectOptionOrder(statementPayload.correctOptionOrder());
        statements.add(statement);
      }
      question.setExtendedMatchingStatements(statements);
      return question;
    }

    private void assertCorrectOption(Question question, int statementIndex, int optionIndex) {
      ExtendedMatchingOption expected = question.getExtendedMatchingOptions().get(optionIndex);
      assertSame(
          expected,
          question.getExtendedMatchingStatements().get(statementIndex).getExtendedMatchingOption());
    }

    private void assertStatementUnbound(Question question, int statementIndex) {
      assertNull(
          question.getExtendedMatchingStatements().get(statementIndex).getExtendedMatchingOption());
    }
  }
}
