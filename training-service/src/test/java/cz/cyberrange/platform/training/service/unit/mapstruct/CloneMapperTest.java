package cz.cyberrange.platform.training.service.unit.mapstruct;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cz.cyberrange.platform.training.persistence.model.AccessLevel;
import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import cz.cyberrange.platform.training.persistence.model.Attachment;
import cz.cyberrange.platform.training.persistence.model.Hint;
import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.enums.TDState;
import cz.cyberrange.platform.training.persistence.model.question.ExtendedMatchingOption;
import cz.cyberrange.platform.training.persistence.model.question.ExtendedMatchingStatement;
import cz.cyberrange.platform.training.persistence.model.question.Question;
import cz.cyberrange.platform.training.persistence.model.question.QuestionChoice;
import cz.cyberrange.platform.training.service.mapping.mapstruct.CloneMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

/**
 * Unit tests for {@link CloneMapper}.
 *
 * <p>Tests all clone methods to verify that id is set to null, state is set to UNRELEASED for
 * TrainingDefinition, and back-references are null. Uses {@link Mappers#getMapper} since the mapper
 * has no Spring dependencies (empty {@code uses}).
 */
@DisplayName("CloneMapper")
class CloneMapperTest {

  private static final Long ENTITY_ID = 42L;
  private static final String ENTITY_TITLE = "Test Title";
  private static final String ENTITY_CONTENT = "Test Content";
  private static final int ENTITY_ORDER = 1;
  private static final int ENTITY_PENALTY = 10;

  private CloneMapper sut;

  @BeforeEach
  void setUp() {
    sut = Mappers.getMapper(CloneMapper.class);
  }

  @Nested
  @DisplayName("clone(TrainingDefinition)")
  class CloneTrainingDefinition {

    @Test
    @DisplayName("should set id to null")
    void shouldSetIdToNull() {
      TrainingDefinition entity = createTrainingDefinition();
      entity.setId(ENTITY_ID);

      TrainingDefinition result = sut.clone(entity);

      assertNull(result.getId());
    }

    @Test
    @DisplayName("should set state to UNRELEASED")
    void shouldSetStateToUnreleased() {
      TrainingDefinition entity = createTrainingDefinition();
      entity.setState(TDState.RELEASED);

      TrainingDefinition result = sut.clone(entity);

      assertEquals(TDState.UNRELEASED, result.getState());
    }

    @Test
    @DisplayName("should create new empty authors HashSet")
    void shouldCreateNewEmptyAuthorsHashSet() {
      TrainingDefinition entity = createTrainingDefinition();
      entity.setAuthors(new HashSet<>());

      TrainingDefinition result = sut.clone(entity);

      assertNotNull(result.getAuthors());
      assertTrue(result.getAuthors().isEmpty());
    }

    @Test
    @DisplayName("should set betaTestingGroup to null")
    void shouldSetBetaTestingGroupToNull() {
      TrainingDefinition entity = createTrainingDefinition();
      entity.setBetaTestingGroup(null);

      TrainingDefinition result = sut.clone(entity);

      assertNull(result.getBetaTestingGroup());
    }

    @Test
    @DisplayName("should preserve title, description, prerequisites, outcomes")
    void shouldPreserveNonIgnoredFields() {
      TrainingDefinition entity = createTrainingDefinition();
      entity.setTitle(ENTITY_TITLE);
      entity.setDescription("Test Description");
      entity.setPrerequisites(new String[] {"prereq1"});
      entity.setOutcomes(new String[] {"outcome1"});

      TrainingDefinition result = sut.clone(entity);

      assertEquals(ENTITY_TITLE, result.getTitle());
      assertEquals("Test Description", result.getDescription());
      assertEquals("prereq1", result.getPrerequisites()[0]);
      assertEquals("outcome1", result.getOutcomes()[0]);
    }
  }

  @Nested
  @DisplayName("clone(InfoLevel)")
  class CloneInfoLevel {

    @Test
    @DisplayName("should set id to null")
    void shouldSetIdToNull() {
      InfoLevel entity = createInfoLevel();
      entity.setId(ENTITY_ID);

      InfoLevel result = sut.clone(entity);

      assertNull(result.getId());
    }

    @Test
    @DisplayName("should set trainingDefinition to null")
    void shouldSetTrainingDefinitionToNull() {
      InfoLevel entity = createInfoLevel();

      InfoLevel result = sut.clone(entity);

      assertNull(result.getTrainingDefinition());
    }

    @Test
    @DisplayName("should preserve content field")
    void shouldPreserveContentField() {
      InfoLevel entity = createInfoLevel();
      entity.setContent(ENTITY_CONTENT);

      InfoLevel result = sut.clone(entity);

      assertEquals(ENTITY_CONTENT, result.getContent());
    }
  }

  @Nested
  @DisplayName("clone(AccessLevel)")
  class CloneAccessLevel {

    @Test
    @DisplayName("should set id to null")
    void shouldSetIdToNull() {
      AccessLevel entity = createAccessLevel();
      entity.setId(ENTITY_ID);

      AccessLevel result = sut.clone(entity);

      assertNull(result.getId());
    }

    @Test
    @DisplayName("should set trainingDefinition to null")
    void shouldSetTrainingDefinitionToNull() {
      AccessLevel entity = createAccessLevel();

      AccessLevel result = sut.clone(entity);

      assertNull(result.getTrainingDefinition());
    }

    @Test
    @DisplayName("should preserve passkey, cloudContent, localContent")
    void shouldPreserveNonIgnoredFields() {
      AccessLevel entity = createAccessLevel();
      entity.setPasskey("secret");
      entity.setCloudContent("cloud");
      entity.setLocalContent("local");

      AccessLevel result = sut.clone(entity);

      assertEquals("secret", result.getPasskey());
      assertEquals("cloud", result.getCloudContent());
      assertEquals("local", result.getLocalContent());
    }
  }

  @Nested
  @DisplayName("clone(TrainingLevel)")
  class CloneTrainingLevel {

    @Test
    @DisplayName("should set id to null")
    void shouldSetIdToNull() {
      TrainingLevel entity = createTrainingLevel();
      entity.setId(ENTITY_ID);

      TrainingLevel result = sut.clone(entity);

      assertNull(result.getId());
    }

    @Test
    @DisplayName("should set trainingDefinition to null")
    void shouldSetTrainingDefinitionToNull() {
      TrainingLevel entity = createTrainingLevel();

      TrainingLevel result = sut.clone(entity);

      assertNull(result.getTrainingDefinition());
    }

    @Test
    @DisplayName("should set hints to empty")
    void shouldSetHintsToEmpty() {
      TrainingLevel entity = createTrainingLevel();

      TrainingLevel result = sut.clone(entity);

      assertNotNull(result.getHints());
      assertTrue(result.getHints().isEmpty());
    }

    @Test
    @DisplayName("should set attachments to empty")
    void shouldSetAttachmentsToEmpty() {
      TrainingLevel entity = createTrainingLevel();

      TrainingLevel result = sut.clone(entity);

      assertNotNull(result.getAttachments());
      assertTrue(result.getAttachments().isEmpty());
    }

    @Test
    @DisplayName("should preserve content, solution, answer fields")
    void shouldPreserveNonIgnoredFields() {
      TrainingLevel entity = createTrainingLevel();
      entity.setContent(ENTITY_CONTENT);
      entity.setSolution("The solution");
      entity.setAnswer("the answer");

      TrainingLevel result = sut.clone(entity);

      assertEquals(ENTITY_CONTENT, result.getContent());
      assertEquals("The solution", result.getSolution());
      assertEquals("the answer", result.getAnswer());
    }
  }

  @Nested
  @DisplayName("clone(Hint)")
  class CloneHint {

    @Test
    @DisplayName("should set id to null")
    void shouldSetIdToNull() {
      Hint entity = createHint();
      entity.setId(ENTITY_ID);

      Hint result = sut.clone(entity);

      assertNull(result.getId());
    }

    @Test
    @DisplayName("should set trainingLevel to null")
    void shouldSetTrainingLevelToNull() {
      Hint entity = createHint();

      Hint result = sut.clone(entity);

      assertNull(result.getTrainingLevel());
    }

    @Test
    @DisplayName("should preserve title, content, hintPenalty, order")
    void shouldPreserveNonIgnoredFields() {
      Hint entity = createHint();
      entity.setTitle(ENTITY_TITLE);
      entity.setContent(ENTITY_CONTENT);
      entity.setHintPenalty(ENTITY_PENALTY);
      entity.setOrder(ENTITY_ORDER);

      Hint result = sut.clone(entity);

      assertEquals(ENTITY_TITLE, result.getTitle());
      assertEquals(ENTITY_CONTENT, result.getContent());
      assertEquals(ENTITY_PENALTY, result.getHintPenalty());
      assertEquals(ENTITY_ORDER, result.getOrder());
    }
  }

  @Nested
  @DisplayName("cloneHints(Set<Hint>)")
  class CloneHints {

    @Test
    @DisplayName("should clone all hints in set")
    void shouldCloneAllHintsInSet() {
      Hint hint1 = createHint();
      hint1.setId(1L);
      hint1.setTitle("Hint1");
      Hint hint2 = createHint();
      hint2.setId(2L);
      hint2.setTitle("Hint2");
      Set<Hint> entities = new HashSet<>(Set.of(hint1, hint2));

      Set<Hint> result = sut.cloneHints(entities);

      assertEquals(2, result.size());
    }

    @Test
    @DisplayName("should return empty set for empty input")
    void shouldReturnEmptySetForEmptyInput() {
      Set<Hint> result = sut.cloneHints(Collections.emptySet());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      Set<Hint> result = sut.cloneHints(null);

      assertNull(result);
    }
  }

  @Nested
  @DisplayName("clone(Attachment)")
  class CloneAttachment {

    @Test
    @DisplayName("should set id to null")
    void shouldSetIdToNull() {
      Attachment entity = createAttachment();
      entity.setId(ENTITY_ID);

      Attachment result = sut.clone(entity);

      assertNull(result.getId());
    }

    @Test
    @DisplayName("should set trainingLevel to null")
    void shouldSetTrainingLevelToNull() {
      Attachment entity = createAttachment();

      Attachment result = sut.clone(entity);

      assertNull(result.getTrainingLevel());
    }

    @Test
    @DisplayName("should preserve content, creationTime")
    void shouldPreserveNonIgnoredFields() {
      LocalDateTime now = LocalDateTime.now();
      Attachment entity = createAttachment();
      entity.setContent(ENTITY_CONTENT);
      entity.setCreationTime(now);

      Attachment result = sut.clone(entity);

      assertEquals(ENTITY_CONTENT, result.getContent());
      assertEquals(now, result.getCreationTime());
    }
  }

  @Nested
  @DisplayName("cloneAttachments(Set<Attachment>)")
  class CloneAttachments {

    @Test
    @DisplayName("should clone all attachments in set")
    void shouldCloneAllAttachmentsInSet() {
      Attachment att1 = createAttachment();
      att1.setId(1L);
      Attachment att2 = createAttachment();
      att2.setId(2L);
      Set<Attachment> entities = new HashSet<>(Set.of(att1, att2));

      Set<Attachment> result = sut.cloneAttachments(entities);

      assertEquals(2, result.size());
    }

    @Test
    @DisplayName("should return empty set for empty input")
    void shouldReturnEmptySetForEmptyInput() {
      Set<Attachment> result = sut.cloneAttachments(Collections.emptySet());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      Set<Attachment> result = sut.cloneAttachments(null);

      assertNull(result);
    }
  }

  @Nested
  @DisplayName("clone(AssessmentLevel)")
  class CloneAssessmentLevel {

    @Test
    @DisplayName("should set id to null")
    void shouldSetIdToNull() {
      AssessmentLevel entity = createAssessmentLevel();
      entity.setId(ENTITY_ID);

      AssessmentLevel result = sut.clone(entity);

      assertNull(result.getId());
    }

    @Test
    @DisplayName("should set trainingDefinition to null")
    void shouldSetTrainingDefinitionToNull() {
      AssessmentLevel entity = createAssessmentLevel();

      AssessmentLevel result = sut.clone(entity);

      assertNull(result.getTrainingDefinition());
    }

    @Test
    @DisplayName("should set questions to empty")
    void shouldSetQuestionsToEmpty() {
      AssessmentLevel entity = createAssessmentLevel();

      AssessmentLevel result = sut.clone(entity);

      assertNotNull(result.getQuestions());
      assertTrue(result.getQuestions().isEmpty());
    }
  }

  @Nested
  @DisplayName("clone(Question)")
  class CloneQuestion {

    @Test
    @DisplayName("should set id to null")
    void shouldSetIdToNull() {
      Question entity = createQuestion();
      entity.setId(ENTITY_ID);

      Question result = sut.clone(entity);

      assertNull(result.getId());
    }

    @Test
    @DisplayName("should set assessmentLevel to null")
    void shouldSetAssessmentLevelToNull() {
      Question entity = createQuestion();

      Question result = sut.clone(entity);

      assertNull(result.getAssessmentLevel());
    }

    @Test
    @DisplayName("should set choices to empty")
    void shouldSetChoicesToEmpty() {
      Question entity = createQuestion();

      Question result = sut.clone(entity);

      assertNotNull(result.getChoices());
      assertTrue(result.getChoices().isEmpty());
    }

    @Test
    @DisplayName("should set extendedMatchingStatements to empty")
    void shouldSetExtendedMatchingStatementsToEmpty() {
      Question entity = createQuestion();

      Question result = sut.clone(entity);

      assertNotNull(result.getExtendedMatchingStatements());
      assertTrue(result.getExtendedMatchingStatements().isEmpty());
    }

    @Test
    @DisplayName("should set extendedMatchingOptions to empty")
    void shouldSetExtendedMatchingOptionsToEmpty() {
      Question entity = createQuestion();

      Question result = sut.clone(entity);

      assertNotNull(result.getExtendedMatchingOptions());
      assertTrue(result.getExtendedMatchingOptions().isEmpty());
    }
  }

  @Nested
  @DisplayName("clone(QuestionChoice)")
  class CloneQuestionChoice {

    @Test
    @DisplayName("should set id to null")
    void shouldSetIdToNull() {
      QuestionChoice entity = createQuestionChoice();
      entity.setId(ENTITY_ID);

      QuestionChoice result = sut.clone(entity);

      assertNull(result.getId());
    }

    @Test
    @DisplayName("should set question to null")
    void shouldSetQuestionToNull() {
      QuestionChoice entity = createQuestionChoice();

      QuestionChoice result = sut.clone(entity);

      assertNull(result.getQuestion());
    }

    @Test
    @DisplayName("should preserve text, correct, order")
    void shouldPreserveNonIgnoredFields() {
      QuestionChoice entity = createQuestionChoice();
      entity.setText("Choice text");
      entity.setCorrect(true);
      entity.setOrder(ENTITY_ORDER);

      QuestionChoice result = sut.clone(entity);

      assertEquals("Choice text", result.getText());
      assertTrue(result.isCorrect());
      assertEquals(ENTITY_ORDER, result.getOrder());
    }
  }

  @Nested
  @DisplayName("cloneChoices(List<QuestionChoice>)")
  class CloneChoices {

    @Test
    @DisplayName("should clone all choices in list")
    void shouldCloneAllChoicesInList() {
      QuestionChoice choice1 = createQuestionChoice();
      choice1.setId(1L);
      choice1.setText("Choice1");
      QuestionChoice choice2 = createQuestionChoice();
      choice2.setId(2L);
      choice2.setText("Choice2");
      List<QuestionChoice> entities = new ArrayList<>(List.of(choice1, choice2));

      List<QuestionChoice> result = sut.cloneChoices(entities);

      assertEquals(2, result.size());
    }

    @Test
    @DisplayName("should return empty list for empty input")
    void shouldReturnEmptyListForEmptyInput() {
      List<QuestionChoice> result = sut.cloneChoices(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      List<QuestionChoice> result = sut.cloneChoices(null);

      assertNull(result);
    }
  }

  @Nested
  @DisplayName("clone(ExtendedMatchingStatement)")
  class CloneExtendedMatchingStatement {

    @Test
    @DisplayName("should set id to null")
    void shouldSetIdToNull() {
      ExtendedMatchingStatement entity = createExtendedMatchingStatement();
      entity.setId(ENTITY_ID);

      ExtendedMatchingStatement result = sut.clone(entity);

      assertNull(result.getId());
    }

    @Test
    @DisplayName("should set question to null")
    void shouldSetQuestionToNull() {
      ExtendedMatchingStatement entity = createExtendedMatchingStatement();

      ExtendedMatchingStatement result = sut.clone(entity);

      assertNull(result.getQuestion());
    }

    @Test
    @DisplayName("should set extendedMatchingOption to null")
    void shouldSetExtendedMatchingOptionToNull() {
      ExtendedMatchingStatement entity = createExtendedMatchingStatement();

      ExtendedMatchingStatement result = sut.clone(entity);

      assertNull(result.getExtendedMatchingOption());
    }

    @Test
    @DisplayName("should preserve text and order")
    void shouldPreserveTextAndOrder() {
      ExtendedMatchingStatement entity = createExtendedMatchingStatement();
      entity.setText(ENTITY_CONTENT);
      entity.setOrder(ENTITY_ORDER);

      ExtendedMatchingStatement result = sut.clone(entity);

      assertEquals(ENTITY_CONTENT, result.getText());
      assertEquals(ENTITY_ORDER, result.getOrder());
    }
  }

  @Nested
  @DisplayName("cloneExtendedMatchingStatements(List<ExtendedMatchingStatement>)")
  class CloneExtendedMatchingStatements {

    @Test
    @DisplayName("should clone all statements in list")
    void shouldCloneAllStatementsInList() {
      ExtendedMatchingStatement stmt1 = createExtendedMatchingStatement();
      stmt1.setId(1L);
      ExtendedMatchingStatement stmt2 = createExtendedMatchingStatement();
      stmt2.setId(2L);
      List<ExtendedMatchingStatement> entities = new ArrayList<>(List.of(stmt1, stmt2));

      List<ExtendedMatchingStatement> result = sut.cloneExtendedMatchingStatements(entities);

      assertEquals(2, result.size());
    }

    @Test
    @DisplayName("should return empty list for empty input")
    void shouldReturnEmptyListForEmptyInput() {
      List<ExtendedMatchingStatement> result =
          sut.cloneExtendedMatchingStatements(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      List<ExtendedMatchingStatement> result = sut.cloneExtendedMatchingStatements(null);

      assertNull(result);
    }
  }

  @Nested
  @DisplayName("clone(ExtendedMatchingOption)")
  class CloneExtendedMatchingOption {

    @Test
    @DisplayName("should set id to null")
    void shouldSetIdToNull() {
      ExtendedMatchingOption entity = createExtendedMatchingOption();
      entity.setId(ENTITY_ID);

      ExtendedMatchingOption result = sut.clone(entity);

      assertNull(result.getId());
    }

    @Test
    @DisplayName("should set question to null")
    void shouldSetQuestionToNull() {
      ExtendedMatchingOption entity = createExtendedMatchingOption();

      ExtendedMatchingOption result = sut.clone(entity);

      assertNull(result.getQuestion());
    }

    @Test
    @DisplayName("should preserve text and order")
    void shouldPreserveTextAndOrder() {
      ExtendedMatchingOption entity = createExtendedMatchingOption();
      entity.setText(ENTITY_CONTENT);
      entity.setOrder(ENTITY_ORDER);

      ExtendedMatchingOption result = sut.clone(entity);

      assertEquals(ENTITY_CONTENT, result.getText());
      assertEquals(ENTITY_ORDER, result.getOrder());
    }
  }

  @Nested
  @DisplayName("cloneExtendedMatchingOptions(List<ExtendedMatchingOption>)")
  class CloneExtendedMatchingOptions {

    @Test
    @DisplayName("should clone all options in list")
    void shouldCloneAllOptionsInList() {
      ExtendedMatchingOption opt1 = createExtendedMatchingOption();
      opt1.setId(1L);
      ExtendedMatchingOption opt2 = createExtendedMatchingOption();
      opt2.setId(2L);
      List<ExtendedMatchingOption> entities = new ArrayList<>(List.of(opt1, opt2));

      List<ExtendedMatchingOption> result = sut.cloneExtendedMatchingOptions(entities);

      assertEquals(2, result.size());
    }

    @Test
    @DisplayName("should return empty list for empty input")
    void shouldReturnEmptyListForEmptyInput() {
      List<ExtendedMatchingOption> result =
          sut.cloneExtendedMatchingOptions(Collections.emptyList());

      assertNotNull(result);
      assertEquals(0, result.size());
    }

    @Test
    @DisplayName("should return null for null input")
    void shouldReturnNullForNullInput() {
      List<ExtendedMatchingOption> result = sut.cloneExtendedMatchingOptions(null);

      assertNull(result);
    }
  }

  // Helper factory methods to create test entities

  private TrainingDefinition createTrainingDefinition() {
    TrainingDefinition entity = new TrainingDefinition();
    entity.setTitle(ENTITY_TITLE);
    entity.setState(TDState.RELEASED);
    entity.setLastEdited(LocalDateTime.now());
    entity.setLastEditedBy("test");
    entity.setCreatedAt(LocalDateTime.now());
    return entity;
  }

  private InfoLevel createInfoLevel() {
    InfoLevel entity = new InfoLevel();
    entity.setTitle(ENTITY_TITLE);
    entity.setContent(ENTITY_CONTENT);
    entity.setOrder(ENTITY_ORDER);
    entity.setMaxScore(100);
    entity.setEstimatedDuration(30);
    return entity;
  }

  private AccessLevel createAccessLevel() {
    AccessLevel entity = new AccessLevel();
    entity.setTitle(ENTITY_TITLE);
    entity.setCloudContent(ENTITY_CONTENT);
    entity.setLocalContent(ENTITY_CONTENT);
    entity.setOrder(ENTITY_ORDER);
    entity.setMaxScore(100);
    entity.setEstimatedDuration(30);
    return entity;
  }

  private TrainingLevel createTrainingLevel() {
    TrainingLevel entity = new TrainingLevel();
    entity.setTitle(ENTITY_TITLE);
    entity.setContent(ENTITY_CONTENT);
    entity.setSolution("solution");
    entity.setOrder(ENTITY_ORDER);
    entity.setMaxScore(100);
    entity.setEstimatedDuration(30);
    return entity;
  }

  private Hint createHint() {
    Hint entity = new Hint();
    entity.setTitle(ENTITY_TITLE);
    entity.setContent(ENTITY_CONTENT);
    entity.setHintPenalty(ENTITY_PENALTY);
    entity.setOrder(ENTITY_ORDER);
    return entity;
  }

  private Attachment createAttachment() {
    Attachment entity = new Attachment();
    entity.setContent(ENTITY_CONTENT);
    entity.setCreationTime(LocalDateTime.now());
    return entity;
  }

  private AssessmentLevel createAssessmentLevel() {
    AssessmentLevel entity = new AssessmentLevel();
    entity.setTitle(ENTITY_TITLE);
    entity.setOrder(ENTITY_ORDER);
    entity.setMaxScore(100);
    entity.setEstimatedDuration(30);
    return entity;
  }

  private Question createQuestion() {
    Question entity = new Question();
    entity.setOrder(ENTITY_ORDER);
    entity.setPenalty(ENTITY_PENALTY);
    return entity;
  }

  private QuestionChoice createQuestionChoice() {
    QuestionChoice entity = new QuestionChoice();
    entity.setOrder(ENTITY_ORDER);
    return entity;
  }

  private ExtendedMatchingStatement createExtendedMatchingStatement() {
    ExtendedMatchingStatement entity = new ExtendedMatchingStatement();
    entity.setOrder(ENTITY_ORDER);
    return entity;
  }

  private ExtendedMatchingOption createExtendedMatchingOption() {
    ExtendedMatchingOption entity = new ExtendedMatchingOption();
    entity.setOrder(ENTITY_ORDER);
    return entity;
  }
}
