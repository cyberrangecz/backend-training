package cz.cyberrange.platform.training.service.unit.mapstruct;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cz.cyberrange.platform.training.persistence.model.AbstractLevel;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

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
    @DisplayName("should leave version at zero and creation time unset")
    void shouldLeaveVersionAndCreationTimeUnset() {
      TrainingDefinition entity = createTrainingDefinition();
      ReflectionTestUtils.setField(entity, "version", 7L);

      TrainingDefinition result = sut.clone(entity);

      assertEquals(0L, result.getVersion());
      assertNull(result.getCreatedAt());
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
  @DisplayName("cloneLevel(AbstractLevel)")
  class CloneLevel {

    @ParameterizedTest
    @MethodSource(
        "cz.cyberrange.platform.training.service.unit.mapstruct.CloneMapperTest#levelsOfEveryType")
    @DisplayName("should copy a level into a new level of the same type without its id")
    void shouldCopyIntoSameType(AbstractLevel level) {
      level.setId(ENTITY_ID);

      AbstractLevel result = sut.cloneLevel(level);

      assertNotSame(level, result);
      assertEquals(level.getClass(), result.getClass());
      assertEquals(ENTITY_TITLE, result.getTitle());
      assertNull(result.getId());
    }
  }

  static Stream<AbstractLevel> levelsOfEveryType() {
    return Stream.of(
        createInfoLevel(), createAccessLevel(), createTrainingLevel(), createAssessmentLevel());
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
    @DisplayName("should link copied hints to the copy")
    void shouldLinkCopiedHintsToCopy() {
      TrainingLevel entity = createTrainingLevel();
      Hint hint = createHint();
      hint.setTrainingLevel(entity);
      entity.setHints(new LinkedHashSet<>(Set.of(hint)));

      TrainingLevel result = sut.clone(entity);

      assertEquals(1, result.getHints().size());
      Hint clonedHint = result.getHints().iterator().next();
      assertNotSame(hint, clonedHint);
      assertSame(result, clonedHint.getTrainingLevel());
    }

    @Test
    @DisplayName("should copy attachments and link them to the copy")
    void shouldLinkCopiedAttachmentsToCopy() {
      TrainingLevel entity = createTrainingLevel();
      Attachment attachment = createAttachment();
      attachment.setId(ENTITY_ID);
      attachment.setTrainingLevel(entity);
      entity.addAttachment(attachment);

      TrainingLevel result = sut.clone(entity);

      assertEquals(1, result.getAttachments().size());
      Attachment clonedAttachment = result.getAttachments().iterator().next();
      assertNotSame(attachment, clonedAttachment);
      assertNull(clonedAttachment.getId());
      assertEquals(attachment.getContent(), clonedAttachment.getContent());
      assertSame(result, clonedAttachment.getTrainingLevel());
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
    @DisplayName("should preserve content")
    void shouldPreserveNonIgnoredFields() {
      Attachment entity = createAttachment();

      Attachment result = sut.clone(entity);

      assertEquals(ENTITY_CONTENT, result.getContent());
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
      att2.setContent("https://example.com/second.pdf");
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

    @Test
    @DisplayName("should link each copied statement to the copied option its source names")
    void shouldLinkCopiedStatementsToCopiedOptions() {
      ExtendedMatchingOption firstOption = createExtendedMatchingOption(0);
      ExtendedMatchingOption secondOption = createExtendedMatchingOption(1);
      ExtendedMatchingStatement linkedStatement = createExtendedMatchingStatement();
      linkedStatement.setExtendedMatchingOption(secondOption);
      ExtendedMatchingStatement unlinkedStatement = createExtendedMatchingStatement();
      Question entity = createQuestion();
      entity.setExtendedMatchingOptions(new ArrayList<>(List.of(firstOption, secondOption)));
      entity.setExtendedMatchingStatements(
          new ArrayList<>(List.of(linkedStatement, unlinkedStatement)));

      Question result = sut.clone(entity);

      ExtendedMatchingOption copiedSecondOption = result.getExtendedMatchingOptions().get(1);
      assertNotSame(secondOption, copiedSecondOption);
      assertSame(
          copiedSecondOption,
          result.getExtendedMatchingStatements().getFirst().getExtendedMatchingOption());
      assertNull(result.getExtendedMatchingStatements().get(1).getExtendedMatchingOption());
    }

    @Test
    @DisplayName("should link the copied choices, statements and options to the copy")
    void shouldLinkCopiedItemsToCopy() {
      Question entity = createQuestion();
      entity.setChoices(new ArrayList<>(List.of(createQuestionChoice())));
      entity.setExtendedMatchingStatements(
          new ArrayList<>(List.of(createExtendedMatchingStatement())));
      entity.setExtendedMatchingOptions(new ArrayList<>(List.of(createExtendedMatchingOption(0))));

      Question result = sut.clone(entity);

      assertSame(result, result.getChoices().getFirst().getQuestion());
      assertSame(result, result.getExtendedMatchingStatements().getFirst().getQuestion());
      assertSame(result, result.getExtendedMatchingOptions().getFirst().getQuestion());
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
      ExtendedMatchingOption entity = createExtendedMatchingOption(ENTITY_ORDER);
      entity.setId(ENTITY_ID);

      ExtendedMatchingOption result = sut.clone(entity);

      assertNull(result.getId());
    }

    @Test
    @DisplayName("should set question to null")
    void shouldSetQuestionToNull() {
      ExtendedMatchingOption entity = createExtendedMatchingOption(ENTITY_ORDER);

      ExtendedMatchingOption result = sut.clone(entity);

      assertNull(result.getQuestion());
    }

    @Test
    @DisplayName("should preserve text and order")
    void shouldPreserveTextAndOrder() {
      ExtendedMatchingOption entity = createExtendedMatchingOption(ENTITY_ORDER);
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
      ExtendedMatchingOption opt1 = createExtendedMatchingOption(ENTITY_ORDER);
      opt1.setId(1L);
      ExtendedMatchingOption opt2 = createExtendedMatchingOption(ENTITY_ORDER);
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

  private static TrainingDefinition createTrainingDefinition() {
    TrainingDefinition entity = new TrainingDefinition();
    entity.setTitle(ENTITY_TITLE);
    entity.setState(TDState.RELEASED);
    entity.setLastEdited(LocalDateTime.now());
    entity.setLastEditedBy("test");
    entity.setCreatedAt(LocalDateTime.now());
    return entity;
  }

  private static InfoLevel createInfoLevel() {
    InfoLevel entity = new InfoLevel();
    entity.setTitle(ENTITY_TITLE);
    entity.setContent(ENTITY_CONTENT);
    entity.setOrder(ENTITY_ORDER);
    entity.setMaxScore(100);
    entity.setEstimatedDuration(30);
    return entity;
  }

  private static AccessLevel createAccessLevel() {
    AccessLevel entity = new AccessLevel();
    entity.setTitle(ENTITY_TITLE);
    entity.setCloudContent(ENTITY_CONTENT);
    entity.setLocalContent(ENTITY_CONTENT);
    entity.setOrder(ENTITY_ORDER);
    entity.setMaxScore(100);
    entity.setEstimatedDuration(30);
    return entity;
  }

  private static TrainingLevel createTrainingLevel() {
    TrainingLevel entity = new TrainingLevel();
    entity.setTitle(ENTITY_TITLE);
    entity.setContent(ENTITY_CONTENT);
    entity.setSolution("solution");
    entity.setOrder(ENTITY_ORDER);
    entity.setMaxScore(100);
    entity.setEstimatedDuration(30);
    return entity;
  }

  private static Hint createHint() {
    Hint entity = new Hint();
    entity.setTitle(ENTITY_TITLE);
    entity.setContent(ENTITY_CONTENT);
    entity.setHintPenalty(ENTITY_PENALTY);
    entity.setOrder(ENTITY_ORDER);
    return entity;
  }

  private static Attachment createAttachment() {
    Attachment entity = new Attachment();
    entity.setContent(ENTITY_CONTENT);
    return entity;
  }

  private static AssessmentLevel createAssessmentLevel() {
    AssessmentLevel entity = new AssessmentLevel();
    entity.setTitle(ENTITY_TITLE);
    entity.setOrder(ENTITY_ORDER);
    entity.setMaxScore(100);
    entity.setEstimatedDuration(30);
    return entity;
  }

  private static Question createQuestion() {
    Question entity = new Question();
    entity.setOrder(ENTITY_ORDER);
    entity.setPenalty(ENTITY_PENALTY);
    return entity;
  }

  private static QuestionChoice createQuestionChoice() {
    QuestionChoice entity = new QuestionChoice();
    entity.setOrder(ENTITY_ORDER);
    return entity;
  }

  private static ExtendedMatchingStatement createExtendedMatchingStatement() {
    ExtendedMatchingStatement entity = new ExtendedMatchingStatement();
    entity.setOrder(ENTITY_ORDER);
    return entity;
  }

  private static ExtendedMatchingOption createExtendedMatchingOption(int order) {
    ExtendedMatchingOption entity = new ExtendedMatchingOption();
    entity.setOrder(order);
    return entity;
  }
}
