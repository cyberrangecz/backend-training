package cz.cyberrange.platform.training.service.unit.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import cz.cyberrange.platform.training.api.exceptions.BadRequestException;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.api.exceptions.UnprocessableEntityException;
import cz.cyberrange.platform.training.persistence.model.AbstractLevel;
import cz.cyberrange.platform.training.persistence.model.AccessLevel;
import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import cz.cyberrange.platform.training.persistence.model.Attachment;
import cz.cyberrange.platform.training.persistence.model.Hint;
import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import cz.cyberrange.platform.training.persistence.model.MitreTechnique;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.enums.AssessmentType;
import cz.cyberrange.platform.training.persistence.model.enums.QuestionType;
import cz.cyberrange.platform.training.persistence.model.question.ExtendedMatchingOption;
import cz.cyberrange.platform.training.persistence.model.question.ExtendedMatchingStatement;
import cz.cyberrange.platform.training.persistence.model.question.Question;
import cz.cyberrange.platform.training.persistence.model.question.QuestionChoice;
import cz.cyberrange.platform.training.persistence.repository.AbstractLevelRepository;
import cz.cyberrange.platform.training.persistence.repository.AttachmentRepository;
import cz.cyberrange.platform.training.persistence.repository.HintRepository;
import cz.cyberrange.platform.training.persistence.repository.MitreTechniqueRepository;
import cz.cyberrange.platform.training.service.services.TrainingDefinitionLevelsService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TrainingDefinitionLevelsServiceTest {

  private static final long DEFINITION_ID = 1L;

  @Mock private AbstractLevelRepository abstractLevelRepository;
  @Mock private MitreTechniqueRepository mitreTechniqueRepository;
  @Mock private HintRepository hintRepository;
  @Mock private AttachmentRepository attachmentRepository;
  @Captor private ArgumentCaptor<Iterable<Attachment>> attachmentsCaptor;

  private TrainingDefinitionLevelsService levelsService;
  private TrainingDefinition definition;

  @BeforeEach
  void init() {
    levelsService =
        new TrainingDefinitionLevelsService(
            abstractLevelRepository,
            mitreTechniqueRepository,
            hintRepository,
            attachmentRepository);
    definition = new TrainingDefinition();
    definition.setId(DEFINITION_ID);
  }

  private void givenStoredLevels(AbstractLevel... storedLevels) {
    given(abstractLevelRepository.findAllLevelsByTrainingDefinitionId(DEFINITION_ID))
        .willReturn(Arrays.asList(storedLevels));
  }

  /**
   * Sends the levels as the definition's whole level list, expects the refusal and checks that no
   * level was saved.
   */
  private String refusalMessageOf(AbstractLevel... sentLevels) {
    String message =
        assertThrows(
                BadRequestException.class,
                () -> levelsService.replaceLevels(definition, List.of(sentLevels)))
            .getMessage();
    then(abstractLevelRepository).should(never()).save(any(AbstractLevel.class));
    return message;
  }

  private static InfoLevel infoLevel(Long id, long estimatedDuration) {
    InfoLevel level = new InfoLevel();
    level.setId(id);
    level.setTitle("Info");
    level.setEstimatedDuration(estimatedDuration);
    return level;
  }

  private static TrainingLevel trainingLevel(Long id, int maxScore, Hint... hints) {
    TrainingLevel level = new TrainingLevel();
    level.setId(id);
    level.setTitle("Training");
    level.setMaxScore(maxScore);
    level.setAnswer("flag");
    level.setEstimatedDuration(10);
    for (Hint hint : hints) {
      level.addHint(hint);
    }
    return level;
  }

  private static Hint hint(Long id, String title, int penalty, int order) {
    Hint hint = new Hint();
    hint.setId(id);
    hint.setTitle(title);
    hint.setContent(title + " content");
    hint.setHintPenalty(penalty);
    hint.setOrder(order);
    return hint;
  }

  private static Attachment attachment(String content) {
    Attachment attachment = new Attachment();
    attachment.setContent(content);
    return attachment;
  }

  private static MitreTechnique technique(Long id, String techniqueKey) {
    MitreTechnique technique = new MitreTechnique();
    technique.setId(id);
    technique.setTechniqueKey(techniqueKey);
    return technique;
  }

  private static Question question(Long id, QuestionType type, int points, int order) {
    Question question = new Question();
    question.setId(id);
    question.setQuestionType(type);
    question.setText("Question " + order);
    question.setPoints(points);
    question.setOrder(order);
    return question;
  }

  private static QuestionChoice choice(Long id) {
    QuestionChoice choice = new QuestionChoice();
    choice.setId(id);
    return choice;
  }

  private static ExtendedMatchingStatement statement(Long id) {
    ExtendedMatchingStatement statement = new ExtendedMatchingStatement();
    statement.setId(id);
    return statement;
  }

  private static ExtendedMatchingOption option(Long id) {
    ExtendedMatchingOption option = new ExtendedMatchingOption();
    option.setId(id);
    return option;
  }

  private static AssessmentLevel assessmentLevel(
      Long id, AssessmentType type, Question... questions) {
    AssessmentLevel level = new AssessmentLevel();
    level.setId(id);
    level.setTitle("Assessment");
    level.setAssessmentType(type);
    level.setQuestions(new ArrayList<>(Arrays.asList(questions)));
    return level;
  }

  /**
   * Builds a new TEST assessment holding one EMI question with one option and one statement, the
   * statement naming that option as correct only when asked to.
   */
  private static AssessmentLevel testAssessmentWithOneStatement(boolean statementLinkedToOption) {
    Question emiQuestion = question(null, QuestionType.EMI, 2, 0);
    ExtendedMatchingOption option = option(null);
    ExtendedMatchingStatement statement = statement(null);
    if (statementLinkedToOption) {
      statement.setExtendedMatchingOption(option);
    }
    emiQuestion.setExtendedMatchingOptions(new ArrayList<>(List.of(option)));
    emiQuestion.setExtendedMatchingStatements(new ArrayList<>(List.of(statement)));
    return assessmentLevel(null, AssessmentType.TEST, emiQuestion);
  }

  @Test
  void replaceLevels_newLevels_savedInListOrderWithSummedDuration() {
    givenStoredLevels();
    InfoLevel first = infoLevel(null, 5);
    TrainingLevel second = trainingLevel(null, 20);
    AccessLevel third = new AccessLevel();
    third.setEstimatedDuration(3);

    levelsService.replaceLevels(definition, List.of(first, second, third));

    assertEquals(0, first.getOrder());
    assertEquals(1, second.getOrder());
    assertEquals(2, third.getOrder());
    assertSame(definition, first.getTrainingDefinition());
    assertSame(definition, third.getTrainingDefinition());
    assertEquals(18, definition.getEstimatedDuration());
    InOrder saveOrder = inOrder(abstractLevelRepository);
    saveOrder.verify(abstractLevelRepository).save(first);
    saveOrder.verify(abstractLevelRepository).save(second);
    saveOrder.verify(abstractLevelRepository).save(third);
  }

  @Test
  void replaceLevels_emptyList_deletesEveryStoredLevel() {
    InfoLevel storedInfo = infoLevel(5L, 5);
    AssessmentLevel storedAssessment = assessmentLevel(6L, AssessmentType.QUESTIONNAIRE);
    givenStoredLevels(storedInfo, storedAssessment);

    levelsService.replaceLevels(definition, List.of());

    then(abstractLevelRepository).should().delete(storedInfo);
    then(abstractLevelRepository).should().delete(storedAssessment);
    then(abstractLevelRepository).should(never()).save(any(AbstractLevel.class));
    assertEquals(0, definition.getEstimatedDuration());
  }

  @Test
  void replaceLevels_storedLevelLeftOut_isDeletedAndOthersKept() {
    InfoLevel storedInfo = infoLevel(5L, 5);
    AccessLevel storedAccess = new AccessLevel();
    storedAccess.setId(6L);
    givenStoredLevels(storedInfo, storedAccess);
    InfoLevel sentInfo = infoLevel(5L, 5);

    levelsService.replaceLevels(definition, List.of(sentInfo));

    then(abstractLevelRepository).should().delete(storedAccess);
    then(abstractLevelRepository).should(never()).delete(storedInfo);
    then(abstractLevelRepository).should().save(sentInfo);
  }

  @Test
  void replaceLevels_storedLevelsInOtherOrder_takeTheirNewPositions() {
    givenStoredLevels(infoLevel(5L, 5), trainingLevel(6L, 20));
    TrainingLevel sentTraining = trainingLevel(6L, 20);
    InfoLevel sentInfo = infoLevel(5L, 5);

    levelsService.replaceLevels(definition, List.of(sentTraining, sentInfo));

    assertEquals(0, sentTraining.getOrder());
    assertEquals(1, sentInfo.getOrder());
  }

  @Test
  void replaceLevels_levelIdListedTwice_isRefused() {
    givenStoredLevels(infoLevel(5L, 5));

    String message = refusalMessageOf(infoLevel(5L, 5), infoLevel(5L, 5));

    assertTrue(message.contains("Level 5 appears more than once"));
    then(abstractLevelRepository).should(never()).delete(any(AbstractLevel.class));
  }

  @Test
  void replaceLevels_levelIdOfAnotherDefinition_isNotFound() {
    givenStoredLevels(infoLevel(5L, 5));

    EntityNotFoundException exception =
        assertThrows(
            EntityNotFoundException.class,
            () -> levelsService.replaceLevels(definition, List.of(infoLevel(77L, 5))));

    assertEquals(77L, exception.getEntityErrorDetail().getIdentifierValue());
    then(abstractLevelRepository).should(never()).save(any(AbstractLevel.class));
    then(abstractLevelRepository).should(never()).delete(any(AbstractLevel.class));
  }

  @Test
  void replaceLevels_levelChangingItsType_isRefused() {
    givenStoredLevels(infoLevel(5L, 5));

    assertTrue(
        refusalMessageOf(trainingLevel(5L, 20))
            .contains("InfoLevel and cannot become a TrainingLevel"));
  }

  @Test
  void replaceLevels_infoLevelReplaced_keepsStoredMinimalSolveTime() {
    InfoLevel storedInfo = infoLevel(5L, 5);
    storedInfo.setMinimalPossibleSolveTime(42L);
    givenStoredLevels(storedInfo);
    InfoLevel sentInfo = infoLevel(5L, 5);

    levelsService.replaceLevels(definition, List.of(sentInfo));

    assertEquals(42L, sentInfo.getMinimalPossibleSolveTime());
  }

  @Test
  void replaceLevels_trainingLevelReplaced_keepsStoredAttachments() {
    TrainingLevel storedTraining = trainingLevel(6L, 20);
    Attachment attachment = new Attachment();
    attachment.setId(9L);
    attachment.setContent("https://attachment");
    storedTraining.addAttachment(attachment);
    givenStoredLevels(storedTraining);
    TrainingLevel sentTraining = trainingLevel(6L, 20);

    levelsService.replaceLevels(definition, List.of(sentTraining));

    assertEquals(Set.of(attachment), sentTraining.getAttachments());
    then(attachmentRepository).shouldHaveNoInteractions();
  }

  @Test
  void replaceLevels_newTrainingLevelWithAttachments_savesThemLinkedToLevel() {
    givenStoredLevels();
    TrainingLevel sentTraining = trainingLevel(null, 20);
    sentTraining.addAttachment(attachment("https://first"));
    sentTraining.addAttachment(attachment("https://second"));

    levelsService.replaceLevels(definition, List.of(sentTraining));

    InOrder saveOrder = inOrder(abstractLevelRepository, attachmentRepository);
    saveOrder.verify(abstractLevelRepository).save(sentTraining);
    saveOrder.verify(attachmentRepository).saveAll(attachmentsCaptor.capture());
    List<Attachment> savedAttachments = new ArrayList<>();
    attachmentsCaptor.getValue().forEach(savedAttachments::add);
    assertEquals(
        Set.of("https://first", "https://second"),
        savedAttachments.stream().map(Attachment::getContent).collect(Collectors.toSet()));
    for (Attachment savedAttachment : savedAttachments) {
      assertSame(sentTraining, savedAttachment.getTrainingLevel());
    }
    assertEquals(new HashSet<>(savedAttachments), sentTraining.getAttachments());
  }

  @Test
  void replaceLevels_trainingLevelReplaced_detachesStoredMitreTechniques() {
    MitreTechnique storedTechnique = technique(31L, "TA0002.T1059");
    TrainingLevel storedTraining = trainingLevel(6L, 20);
    storedTraining.addMitreTechnique(storedTechnique);
    givenStoredLevels(storedTraining);
    TrainingLevel sentTraining = trainingLevel(6L, 20);

    levelsService.replaceLevels(definition, List.of(sentTraining));

    assertTrue(storedTraining.getMitreTechniques().isEmpty());
    assertTrue(storedTechnique.getTrainingLevels().isEmpty());
  }

  @Test
  void replaceLevels_hintIds_keptOnlyWhenOwnedByStoredLevel() {
    TrainingLevel storedTraining = trainingLevel(6L, 20, hint(61L, "Own hint", 1, 0));
    givenStoredLevels(storedTraining);
    Hint ownHint = hint(61L, "Own hint", 1, 0);
    Hint foreignHint = hint(99L, "Hint of another level", 1, 1);
    TrainingLevel sentTraining = trainingLevel(6L, 20, ownHint, foreignHint);
    TrainingLevel newTraining = trainingLevel(null, 20, hint(61L, "Copied hint", 1, 0));

    levelsService.replaceLevels(definition, List.of(sentTraining, newTraining));

    assertEquals(61L, ownHint.getId());
    assertNull(foreignHint.getId());
    assertNull(newTraining.getHints().iterator().next().getId());
    assertTrue(
        sentTraining.getHints().stream()
            .allMatch(sentHint -> sentHint.getTrainingLevel() == sentTraining));
  }

  @Test
  void replaceLevels_hints_sortedByTheirOrder() {
    givenStoredLevels();
    TrainingLevel sentTraining =
        trainingLevel(
            null,
            20,
            hint(null, "Third", 1, 2),
            hint(null, "First", 1, 0),
            hint(null, "Second", 1, 1));

    levelsService.replaceLevels(definition, List.of(sentTraining));

    assertEquals(
        List.of("First", "Second", "Third"),
        sentTraining.getHints().stream().map(Hint::getTitle).toList());
  }

  @Test
  void replaceLevels_hintPenaltiesAboveMaxScore_isUnprocessable() {
    givenStoredLevels();
    TrainingLevel sentTraining =
        trainingLevel(null, 5, hint(null, "First", 3, 0), hint(null, "Second", 3, 1));

    UnprocessableEntityException exception =
        assertThrows(
            UnprocessableEntityException.class,
            () -> levelsService.replaceLevels(definition, List.of(sentTraining)));

    assertEquals("Training", exception.getEntityErrorDetail().getIdentifierValue());
  }

  @Test
  void replaceLevels_staticAnswerBlank_isRefused() {
    givenStoredLevels();
    TrainingLevel sentTraining = trainingLevel(null, 20);
    sentTraining.setAnswer(" ");

    refusalMessageOf(sentTraining);
  }

  @Test
  void replaceLevels_variantAnswerWithStaticAnswer_isRefused() {
    givenStoredLevels();
    TrainingLevel sentTraining = trainingLevel(null, 20);
    sentTraining.setVariantAnswers(true);
    sentTraining.setAnswerVariableName("flag_variable");

    refusalMessageOf(sentTraining);
  }

  @Test
  void replaceLevels_mitreTechniques_matchedByKeyAgainstStoredOnes() {
    givenStoredLevels();
    MitreTechnique storedTechnique = technique(31L, "TA0002.T1059");
    given(mitreTechniqueRepository.findAllByTechniqueKeyInOrderById(anySet()))
        .willAnswer(invocation -> new HashSet<>(Set.of(storedTechnique)));
    MitreTechnique sentStoredKey = technique(500L, "TA0002.T1059");
    MitreTechnique sentNewKey = technique(501L, "TA0003.T1098");
    TrainingLevel sentTraining = trainingLevel(null, 20);
    sentTraining.setMitreTechniques(new HashSet<>(Set.of(sentStoredKey, sentNewKey)));

    levelsService.replaceLevels(definition, List.of(sentTraining));

    assertTrue(
        sentTraining.getMitreTechniques().stream()
            .anyMatch(technique -> technique == storedTechnique));
    assertNull(sentNewKey.getId());
    assertTrue(storedTechnique.getTrainingLevels().contains(sentTraining));
  }

  @Test
  void replaceLevels_assessmentContentIds_keptOnlyWhenOwned() {
    Question storedQuestion = question(71L, QuestionType.MCQ, 2, 0);
    storedQuestion.setChoices(new ArrayList<>(List.of(choice(81L))));
    givenStoredLevels(assessmentLevel(7L, AssessmentType.QUESTIONNAIRE, storedQuestion));

    Question ownQuestion = question(71L, QuestionType.MCQ, 2, 0);
    QuestionChoice ownChoice = choice(81L);
    QuestionChoice foreignChoice = choice(999L);
    ownQuestion.setChoices(new ArrayList<>(List.of(ownChoice, foreignChoice)));
    Question foreignQuestion = question(72L, QuestionType.FFQ, 3, 1);
    QuestionChoice choiceOfForeignQuestion = choice(81L);
    foreignQuestion.setChoices(new ArrayList<>(List.of(choiceOfForeignQuestion)));
    AssessmentLevel sentAssessment =
        assessmentLevel(7L, AssessmentType.QUESTIONNAIRE, ownQuestion, foreignQuestion);

    levelsService.replaceLevels(definition, List.of(sentAssessment));

    assertEquals(71L, ownQuestion.getId());
    assertEquals(81L, ownChoice.getId());
    assertNull(foreignChoice.getId());
    assertNull(foreignQuestion.getId());
    assertNull(choiceOfForeignQuestion.getId());
    assertEquals(5, sentAssessment.getMaxScore());
  }

  @Test
  void replaceLevels_testStatementWithoutCorrectOption_isRefused() {
    givenStoredLevels();

    String message = refusalMessageOf(testAssessmentWithOneStatement(false));

    assertTrue(message.contains("Statement 1 of question 1 names no correct option"));
  }

  @Test
  void replaceLevels_testStatementsWithCorrectOptions_areSaved() {
    givenStoredLevels();
    AssessmentLevel sentAssessment = testAssessmentWithOneStatement(true);

    levelsService.replaceLevels(definition, List.of(sentAssessment));

    then(abstractLevelRepository).should().save(sentAssessment);
    assertEquals(2, sentAssessment.getMaxScore());
  }

  @Test
  void replaceLevels_hintIdOfStoredHintListedTwice_isRefused() {
    givenStoredLevels(trainingLevel(6L, 20, hint(61L, "Own hint", 1, 0)));
    TrainingLevel sentTraining =
        trainingLevel(6L, 20, hint(61L, "Own hint", 1, 0), hint(61L, "Copy of own hint", 1, 1));

    assertTrue(refusalMessageOf(sentTraining).contains("Hint 61 appears more than once"));
  }

  @Test
  void replaceLevels_questionIdOfStoredQuestionListedTwice_isRefused() {
    givenStoredLevels(
        assessmentLevel(7L, AssessmentType.QUESTIONNAIRE, question(71L, QuestionType.MCQ, 2, 0)));
    AssessmentLevel sentAssessment =
        assessmentLevel(
            7L,
            AssessmentType.QUESTIONNAIRE,
            question(71L, QuestionType.MCQ, 2, 0),
            question(71L, QuestionType.FFQ, 2, 1));

    assertTrue(refusalMessageOf(sentAssessment).contains("Question 71 appears more than once"));
  }

  @Test
  void replaceLevels_choiceStatementAndOptionIdsListedTwice_areRefused() {
    Question storedQuestion = question(71L, QuestionType.EMI, 2, 0);
    storedQuestion.setChoices(new ArrayList<>(List.of(choice(81L))));
    storedQuestion.setExtendedMatchingStatements(new ArrayList<>(List.of(statement(82L))));
    storedQuestion.setExtendedMatchingOptions(new ArrayList<>(List.of(option(83L))));
    givenStoredLevels(assessmentLevel(7L, AssessmentType.QUESTIONNAIRE, storedQuestion));

    Question twiceChoice = question(71L, QuestionType.EMI, 2, 0);
    twiceChoice.setChoices(new ArrayList<>(List.of(choice(81L), choice(81L))));
    Question twiceStatement = question(71L, QuestionType.EMI, 2, 0);
    twiceStatement.setExtendedMatchingStatements(
        new ArrayList<>(List.of(statement(82L), statement(82L))));
    Question twiceOption = question(71L, QuestionType.EMI, 2, 0);
    twiceOption.setExtendedMatchingOptions(new ArrayList<>(List.of(option(83L), option(83L))));

    assertTrue(
        refusalMessageOf(assessmentLevel(7L, AssessmentType.QUESTIONNAIRE, twiceChoice))
            .contains("Choice 81 appears more than once"));
    assertTrue(
        refusalMessageOf(assessmentLevel(7L, AssessmentType.QUESTIONNAIRE, twiceStatement))
            .contains("Statement 82 appears more than once"));
    assertTrue(
        refusalMessageOf(assessmentLevel(7L, AssessmentType.QUESTIONNAIRE, twiceOption))
            .contains("Option 83 appears more than once"));
  }

  @Test
  void replaceLevels_assessmentContent_takesListPositionsAsOrder() {
    givenStoredLevels();
    Question first = question(null, QuestionType.EMI, 1, 0);
    first.setChoices(new ArrayList<>(List.of(choice(null), choice(null))));
    first.setExtendedMatchingStatements(new ArrayList<>(List.of(statement(null), statement(null))));
    first.setExtendedMatchingOptions(new ArrayList<>(List.of(option(null), option(null))));
    Question second = question(null, QuestionType.MCQ, 1, 1);
    AssessmentLevel sentAssessment =
        assessmentLevel(null, AssessmentType.QUESTIONNAIRE, first, second);
    first.setOrder(7);
    second.setOrder(3);
    first.getChoices().forEach(sentChoice -> sentChoice.setOrder(9));
    first.getExtendedMatchingStatements().forEach(sentStatement -> sentStatement.setOrder(9));
    first.getExtendedMatchingOptions().forEach(sentOption -> sentOption.setOrder(9));

    levelsService.replaceLevels(definition, List.of(sentAssessment));

    assertEquals(0, first.getOrder());
    assertEquals(1, second.getOrder());
    assertEquals(List.of(0, 1), first.getChoices().stream().map(QuestionChoice::getOrder).toList());
    assertEquals(
        List.of(0, 1),
        first.getExtendedMatchingStatements().stream()
            .map(ExtendedMatchingStatement::getOrder)
            .toList());
    assertEquals(
        List.of(0, 1),
        first.getExtendedMatchingOptions().stream().map(ExtendedMatchingOption::getOrder).toList());
  }

  @Test
  void deleteLevel_trainingLevelRejectedByConstraint_deletesHintsAndRetries() {
    TrainingLevel level = trainingLevel(6L, 20);
    willThrow(ConstraintViolationException.class)
        .willDoNothing()
        .given(abstractLevelRepository)
        .delete(level);

    levelsService.deleteLevel(level);

    then(hintRepository).should().deleteHintsByLevelId(6L);
    then(abstractLevelRepository).should(times(2)).delete(level);
  }

  @Test
  void deleteLevel_nonTrainingLevelRejectedByConstraint_rethrowsWithoutTouchingHints() {
    InfoLevel level = infoLevel(5L, 5);
    willThrow(ConstraintViolationException.class).given(abstractLevelRepository).delete(level);

    assertThrows(ConstraintViolationException.class, () -> levelsService.deleteLevel(level));

    then(hintRepository).shouldHaveNoInteractions();
    then(abstractLevelRepository).should(times(1)).delete(level);
  }
}
