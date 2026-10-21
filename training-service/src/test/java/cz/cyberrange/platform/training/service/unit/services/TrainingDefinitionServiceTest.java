package cz.cyberrange.platform.training.service.unit.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.BDDMockito.*;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.cyberrange.platform.training.api.exceptions.EntityConflictException;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.persistence.model.AbstractLevel;
import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import cz.cyberrange.platform.training.persistence.model.Attachment;
import cz.cyberrange.platform.training.persistence.model.Hint;
import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.model.enums.AssessmentType;
import cz.cyberrange.platform.training.persistence.model.enums.QuestionType;
import cz.cyberrange.platform.training.persistence.model.enums.TDState;
import cz.cyberrange.platform.training.persistence.model.question.ExtendedMatchingOption;
import cz.cyberrange.platform.training.persistence.model.question.ExtendedMatchingStatement;
import cz.cyberrange.platform.training.persistence.model.question.Question;
import cz.cyberrange.platform.training.persistence.repository.AbstractLevelRepository;
import cz.cyberrange.platform.training.persistence.repository.AttachmentRepository;
import cz.cyberrange.platform.training.persistence.repository.HintRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingDefinitionRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingInstanceRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingInstanceRepository.DefinitionInstanceCount;
import cz.cyberrange.platform.training.persistence.repository.UserRefRepository;
import cz.cyberrange.platform.training.persistence.util.TestDataFactory;
import cz.cyberrange.platform.training.service.mapping.mapstruct.CloneMapperImpl;
import cz.cyberrange.platform.training.service.services.SecurityService;
import cz.cyberrange.platform.training.service.services.TrainingDefinitionLevelsService;
import cz.cyberrange.platform.training.service.services.TrainingDefinitionService;
import cz.cyberrange.platform.training.service.services.UserService;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class TrainingDefinitionServiceTest {

  private static final LocalDateTime SOURCE_CREATED_AT = LocalDateTime.of(2020, 1, 1, 8, 0);
  private static final long STORED_VERSION = 4L;

  private final TestDataFactory testDataFactory = new TestDataFactory();

  @Mock private TrainingDefinitionRepository trainingDefinitionRepository;
  @Mock private AbstractLevelRepository abstractLevelRepository;
  @Mock private TrainingInstanceRepository trainingInstanceRepository;
  @Mock private HintRepository hintRepository;
  @Mock private AttachmentRepository attachmentRepository;
  @Mock private UserRefRepository userRefRepository;
  @Mock private SecurityService securityService;
  @Mock private UserService userService;
  @Mock private TrainingDefinitionLevelsService trainingDefinitionLevelsService;
  @Captor private ArgumentCaptor<TrainingDefinition> definitionCaptor;
  @Captor private ArgumentCaptor<AbstractLevel> levelCaptor;
  @Captor private ArgumentCaptor<Iterable<Attachment>> attachmentsCaptor;

  private TrainingDefinitionService trainingDefinitionService;
  private TrainingDefinition unreleasedDefinition;
  private TrainingDefinition releasedDefinition;
  private AssessmentLevel assessmentLevel;
  private TrainingLevel trainingLevel;
  private InfoLevel infoLevel;
  private UserRefDTO userRefDTO;

  @BeforeEach
  public void init() {
    trainingDefinitionService =
        new TrainingDefinitionService(
            trainingDefinitionRepository,
            abstractLevelRepository,
            trainingInstanceRepository,
            hintRepository,
            attachmentRepository,
            userRefRepository,
            securityService,
            userService,
            trainingDefinitionLevelsService,
            new CloneMapperImpl());

    infoLevel = testDataFactory.getInfoLevel1();
    infoLevel.setId(1L);
    infoLevel.setOrder(0);

    trainingLevel = testDataFactory.getPenalizedLevel();
    trainingLevel.setId(2L);
    trainingLevel.setOrder(1);

    assessmentLevel = testDataFactory.getTest();
    assessmentLevel.setId(3L);
    assessmentLevel.setOrder(2);

    unreleasedDefinition = testDataFactory.getUnreleasedDefinition();
    unreleasedDefinition.setId(1L);
    ReflectionTestUtils.setField(unreleasedDefinition, "version", STORED_VERSION);

    releasedDefinition = testDataFactory.getReleasedDefinition();
    releasedDefinition.setId(2L);

    userRefDTO = testDataFactory.getUserRefDTO1();
  }

  @Test
  public void getTrainingDefinitionById() {
    given(trainingDefinitionRepository.findById(unreleasedDefinition.getId()))
        .willReturn(Optional.of(unreleasedDefinition));
    TrainingDefinition tD = trainingDefinitionService.findById(unreleasedDefinition.getId());
    assertEquals(tD, unreleasedDefinition);
    then(trainingDefinitionRepository).should().findById(unreleasedDefinition.getId());
  }

  @Test
  public void getNonexistentTrainingDefinitionById() {
    Long id = 6L;
    assertThrows(EntityNotFoundException.class, () -> trainingDefinitionService.findById(id));
  }

  @Test
  public void findAll() {
    List<TrainingDefinition> expected = new ArrayList<>();
    expected.add(unreleasedDefinition);
    expected.add(releasedDefinition);

    Page<TrainingDefinition> page = new PageImpl<>(expected);
    PathBuilder<TrainingDefinition> tD =
        new PathBuilder<TrainingDefinition>(TrainingDefinition.class, "trainingRun");
    Predicate predicate = tD.isNotNull();

    given(trainingDefinitionRepository.findAll(any(Predicate.class), any(Pageable.class)))
        .willReturn(page);

    Page<TrainingDefinition> result =
        trainingDefinitionService.findAll(predicate, PageRequest.of(0, 2));
    assertEquals(2, result.getTotalElements());
  }

  @Test
  public void cloneTrainingDefinitionCreatesUnreleasedCopyWithCopiedLevels() {
    givenLoggedInAuthor();
    given(trainingDefinitionRepository.findById(releasedDefinition.getId()))
        .willReturn(Optional.of(releasedDefinition));
    given(abstractLevelRepository.findAllLevelsByTrainingDefinitionId(releasedDefinition.getId()))
        .willReturn(List.of(infoLevel, trainingLevel));
    given(trainingDefinitionRepository.save(any(TrainingDefinition.class)))
        .willAnswer(returnsFirstArg());

    TrainingDefinition clone = trainingDefinitionService.clone(releasedDefinition.getId(), "Copy");

    assertNotSame(releasedDefinition, clone);
    assertNull(clone.getId());
    assertEquals("Copy", clone.getTitle());
    assertEquals(TDState.UNRELEASED, clone.getState());
    assertEquals(releasedDefinition.getDescription(), clone.getDescription());
    assertEquals(1, clone.getAuthors().size());
    then(trainingDefinitionLevelsService).shouldHaveNoInteractions();
    then(abstractLevelRepository).should(times(2)).save(levelCaptor.capture());
    List<AbstractLevel> clonedLevels = levelCaptor.getAllValues();
    InfoLevel clonedInfoLevel = assertInstanceOf(InfoLevel.class, clonedLevels.get(0));
    assertNull(clonedInfoLevel.getId());
    assertSame(clone, clonedInfoLevel.getTrainingDefinition());
    assertEquals(infoLevel.getContent(), clonedInfoLevel.getContent());
    TrainingLevel clonedTrainingLevel = assertInstanceOf(TrainingLevel.class, clonedLevels.get(1));
    assertNull(clonedTrainingLevel.getId());
    assertSame(clone, clonedTrainingLevel.getTrainingDefinition());
    assertEquals(trainingLevel.getContent(), clonedTrainingLevel.getContent());
    assertEquals(trainingLevel.getSolution(), clonedTrainingLevel.getSolution());
    assertEquals(trainingLevel.getMaxScore(), clonedTrainingLevel.getMaxScore());
    then(trainingDefinitionRepository).should().flush();
  }

  @Test
  public void cloneTrainingDefinitionCopiesLevelsBreakingCurrentRulesAsStored() {
    givenLoggedInAuthor();
    releasedDefinition.setEstimatedDuration(99L);
    TrainingLevel legacyTrainingLevel = legacyTrainingLevel();
    AssessmentLevel legacyTest = legacyTestWithStatementLackingCorrectOption();
    given(trainingDefinitionRepository.findById(releasedDefinition.getId()))
        .willReturn(Optional.of(releasedDefinition));
    given(abstractLevelRepository.findAllLevelsByTrainingDefinitionId(releasedDefinition.getId()))
        .willReturn(List.of(legacyTrainingLevel, legacyTest));
    given(trainingDefinitionRepository.save(any(TrainingDefinition.class)))
        .willAnswer(returnsFirstArg());

    TrainingDefinition clone = trainingDefinitionService.clone(releasedDefinition.getId(), "Copy");

    assertEquals(99L, clone.getEstimatedDuration());
    then(trainingDefinitionLevelsService).shouldHaveNoInteractions();
    then(abstractLevelRepository).should(times(2)).save(levelCaptor.capture());
    TrainingLevel clonedTrainingLevel =
        assertInstanceOf(TrainingLevel.class, levelCaptor.getAllValues().get(0));
    assertEquals(5, clonedTrainingLevel.getOrder());
    assertEquals(1, clonedTrainingLevel.getMaxScore());
    assertEquals("", clonedTrainingLevel.getAnswer());
    Hint clonedHint = clonedTrainingLevel.getHints().iterator().next();
    assertEquals(10, clonedHint.getHintPenalty());
    assertSame(clonedTrainingLevel, clonedHint.getTrainingLevel());
    AssessmentLevel clonedTest =
        assertInstanceOf(AssessmentLevel.class, levelCaptor.getAllValues().get(1));
    assertEquals(8, clonedTest.getOrder());
    assertEquals(50, clonedTest.getMaxScore());
    Question clonedQuestion = clonedTest.getQuestions().getFirst();
    assertEquals(4, clonedQuestion.getOrder());
    ExtendedMatchingStatement clonedStatement =
        clonedQuestion.getExtendedMatchingStatements().getFirst();
    assertNull(clonedStatement.getExtendedMatchingOption());
  }

  @Test
  public void cloneTrainingDefinitionSavesCopiedAttachmentsLinkedToCopiedLevel() {
    givenLoggedInAuthor();
    Attachment attachment = new Attachment();
    attachment.setId(21L);
    attachment.setContent("Manual");
    attachment.setTrainingLevel(trainingLevel);
    trainingLevel.addAttachment(attachment);
    given(trainingDefinitionRepository.findById(releasedDefinition.getId()))
        .willReturn(Optional.of(releasedDefinition));
    given(abstractLevelRepository.findAllLevelsByTrainingDefinitionId(releasedDefinition.getId()))
        .willReturn(List.of(infoLevel, trainingLevel));
    given(trainingDefinitionRepository.save(any(TrainingDefinition.class)))
        .willAnswer(returnsFirstArg());

    trainingDefinitionService.clone(releasedDefinition.getId(), "Copy");

    then(abstractLevelRepository).should(times(2)).save(levelCaptor.capture());
    TrainingLevel clonedTrainingLevel =
        assertInstanceOf(TrainingLevel.class, levelCaptor.getAllValues().get(1));
    then(attachmentRepository).should().saveAll(attachmentsCaptor.capture());
    List<Attachment> savedAttachments = new ArrayList<>();
    attachmentsCaptor.getValue().forEach(savedAttachments::add);
    assertEquals(1, savedAttachments.size());
    Attachment savedAttachment = savedAttachments.getFirst();
    assertNotSame(attachment, savedAttachment);
    assertNull(savedAttachment.getId());
    assertEquals("Manual", savedAttachment.getContent());
    assertSame(clonedTrainingLevel, savedAttachment.getTrainingLevel());
    assertSame(trainingLevel, attachment.getTrainingLevel());
  }

  @Test
  public void cloneTrainingDefinitionSetsCurrentCreationTime() {
    givenLoggedInAuthor();
    releasedDefinition.setCreatedAt(SOURCE_CREATED_AT);
    given(trainingDefinitionRepository.findById(releasedDefinition.getId()))
        .willReturn(Optional.of(releasedDefinition));
    given(trainingDefinitionRepository.save(any(TrainingDefinition.class)))
        .willAnswer(returnsFirstArg());
    LocalDateTime beforeClone = LocalDateTime.now(Clock.systemUTC());

    trainingDefinitionService.clone(releasedDefinition.getId(), "new Title");

    then(trainingDefinitionRepository).should().save(definitionCaptor.capture());
    LocalDateTime cloneCreatedAt = definitionCaptor.getValue().getCreatedAt();
    assertNotNull(cloneCreatedAt);
    assertFalse(cloneCreatedAt.isBefore(beforeClone));
    assertTrue(Duration.between(beforeClone, cloneCreatedAt).toMinutes() < 1);
    assertEquals(SOURCE_CREATED_AT, releasedDefinition.getCreatedAt());
  }

  @Test
  public void createTrainingDefinitionWritesLevels() {
    givenLoggedInAuthor();
    TrainingDefinition newDefinition = testDataFactory.getUnreleasedDefinition();
    newDefinition.setCreatedAt(null);
    List<AbstractLevel> levels = List.of(new InfoLevel(), new TrainingLevel());
    given(trainingDefinitionRepository.save(newDefinition)).willReturn(newDefinition);

    TrainingDefinition created = trainingDefinitionService.create(newDefinition, levels);

    assertSame(newDefinition, created);
    assertNotNull(created.getCreatedAt());
    assertEquals(userRefDTO.getUserRefFullName(), created.getLastEditedBy());
    then(userRefRepository).should().createOrGet(anyLong());
    then(trainingDefinitionRepository).should(times(1)).save(newDefinition);
    then(trainingDefinitionLevelsService).should().replaceLevels(newDefinition, levels);
    then(trainingDefinitionRepository).should().flush();
  }

  @Test
  public void createTrainingDefinitionWithoutLevels() {
    givenLoggedInAuthor();
    TrainingDefinition newDefinition = testDataFactory.getUnreleasedDefinition();
    given(trainingDefinitionRepository.save(newDefinition)).willReturn(newDefinition);

    trainingDefinitionService.create(newDefinition, List.of());

    then(trainingDefinitionLevelsService).should().replaceLevels(newDefinition, List.of());
  }

  @Test
  public void completeInstanceCountsSetsRealCountAndZeroForDefinitionWithoutInstances() {
    TrainingDefinitionDTO definitionWithInstances = definitionResponse(1L);
    TrainingDefinitionDTO definitionWithoutInstances = definitionResponse(2L);
    TrainingDefinitionDTO sameDefinitionAgain = definitionResponse(1L);
    given(trainingInstanceRepository.countGroupedByTrainingDefinition(Set.of(1L, 2L)))
        .willReturn(List.of(instanceCount(1L, 3L)));

    trainingDefinitionService.completeInstanceCounts(
        List.of(definitionWithInstances, definitionWithoutInstances, sameDefinitionAgain));

    assertEquals(3L, definitionWithInstances.getInstanceCount());
    assertEquals(0L, definitionWithoutInstances.getInstanceCount());
    assertEquals(3L, sameDefinitionAgain.getInstanceCount());
    then(trainingInstanceRepository).should(times(1)).countGroupedByTrainingDefinition(any());
  }

  @Test
  public void completeInstanceCountsWithoutHoldersQueriesNothing() {
    trainingDefinitionService.completeInstanceCounts(List.of());

    then(trainingInstanceRepository).shouldHaveNoInteractions();
  }

  @Test
  public void updateReplacesContentAndLevelsAndSaves() {
    givenLoggedInAuthor();
    givenStoredDefinition(unreleasedDefinition);
    given(trainingDefinitionRepository.save(unreleasedDefinition)).willReturn(unreleasedDefinition);
    TrainingDefinition content = updateContent();
    List<AbstractLevel> levels = List.of(infoLevel, new TrainingLevel());

    TrainingDefinition updated =
        trainingDefinitionService.update(
            unreleasedDefinition.getId(), STORED_VERSION, content, levels);

    assertSame(unreleasedDefinition, updated);
    assertEquals(content.getTitle(), updated.getTitle());
    assertEquals(content.getDescription(), updated.getDescription());
    assertArrayEquals(content.getPrerequisites(), updated.getPrerequisites());
    assertArrayEquals(content.getOutcomes(), updated.getOutcomes());
    assertEquals(TDState.UNRELEASED, updated.getState());
    assertEquals(userRefDTO.getUserRefFullName(), updated.getLastEditedBy());
    assertNotNull(updated.getLastEdited());
    then(userRefRepository).should().createOrGet(anyLong());
    then(trainingDefinitionLevelsService).should().replaceLevels(unreleasedDefinition, levels);
    then(trainingDefinitionRepository).should().flush();
  }

  @Test
  public void updateWithStaleVersionIsRefused() {
    givenStoredDefinition(unreleasedDefinition);

    EntityConflictException exception =
        assertThrows(
            EntityConflictException.class,
            () ->
                trainingDefinitionService.update(
                    unreleasedDefinition.getId(), STORED_VERSION - 1, updateContent(), List.of()));

    assertTrue(exception.getEntityErrorDetail().getReason().contains("version " + STORED_VERSION));
    then(trainingDefinitionLevelsService).shouldHaveNoInteractions();
    then(trainingDefinitionRepository).should(never()).save(any());
  }

  @Test
  public void updateReleasedDefinitionIsRefused() {
    givenStoredDefinition(releasedDefinition);

    assertThrows(
        EntityConflictException.class,
        () ->
            trainingDefinitionService.update(
                releasedDefinition.getId(),
                releasedDefinition.getVersion(),
                updateContent(),
                List.of()));
    then(trainingDefinitionLevelsService).shouldHaveNoInteractions();
  }

  @Test
  public void updateDefinitionWithCreatedInstancesIsRefused() {
    givenStoredDefinition(unreleasedDefinition);
    given(trainingInstanceRepository.existsAnyForTrainingDefinition(unreleasedDefinition.getId()))
        .willReturn(true);

    assertThrows(
        EntityConflictException.class,
        () ->
            trainingDefinitionService.update(
                unreleasedDefinition.getId(), STORED_VERSION, updateContent(), List.of()));
    then(trainingDefinitionLevelsService).shouldHaveNoInteractions();
  }

  @Test
  public void updateUnknownDefinitionIsNotFound() {
    assertThrows(
        EntityNotFoundException.class,
        () -> trainingDefinitionService.update(99L, 0L, updateContent(), List.of()));
  }

  @Test
  public void delete() {
    givenStoredDefinition(unreleasedDefinition);
    given(abstractLevelRepository.findAllLevelsByTrainingDefinitionId(unreleasedDefinition.getId()))
        .willReturn(List.of(trainingLevel, assessmentLevel, infoLevel));

    trainingDefinitionService.delete(unreleasedDefinition.getId());

    then(trainingDefinitionRepository).should().findById(unreleasedDefinition.getId());
    then(trainingDefinitionRepository).should().delete(unreleasedDefinition);
    then(trainingDefinitionLevelsService).should().deleteLevel(assessmentLevel);
    then(trainingDefinitionLevelsService).should().deleteLevel(trainingLevel);
    then(trainingDefinitionLevelsService).should().deleteLevel(infoLevel);
  }

  @Test
  public void deleteWithCannotBeDeletedException() {
    givenStoredDefinition(releasedDefinition);
    assertThrows(
        EntityConflictException.class,
        () -> trainingDefinitionService.delete(releasedDefinition.getId()));
  }

  @Test
  public void deleteWithCreatedInstances() {
    givenStoredDefinition(unreleasedDefinition);
    given(trainingInstanceRepository.existsAnyForTrainingDefinition(unreleasedDefinition.getId()))
        .willReturn(true);
    assertThrows(
        EntityConflictException.class,
        () -> trainingDefinitionService.delete(unreleasedDefinition.getId()));
  }

  @Test
  public void findLevelById() {
    given(abstractLevelRepository.findByIdIncludingDefinition(infoLevel.getId()))
        .willReturn(Optional.of(infoLevel));
    AbstractLevel abstractLevel = trainingDefinitionService.findLevelById(infoLevel.getId());
    assertTrue(abstractLevel instanceof InfoLevel);
    assertEquals(infoLevel.getId(), abstractLevel.getId());
    then(abstractLevelRepository).should().findByIdIncludingDefinition(infoLevel.getId());
  }

  @Test
  public void findLevelByIdNotExisting() {
    assertThrows(
        EntityNotFoundException.class, () -> trainingDefinitionService.findLevelById(555L));
  }

  @Test
  public void switchState_UNRELEASEDtoRELEASED() {
    givenLoggedInEditor();
    givenStoredDefinition(unreleasedDefinition);
    trainingDefinitionService.switchState(
        unreleasedDefinition.getId(), cz.cyberrange.platform.training.api.enums.TDState.RELEASED);
    assertEquals(TDState.RELEASED, unreleasedDefinition.getState());
  }

  @Test
  public void switchState_RELEASEDtoARCHIVED() {
    givenLoggedInEditor();
    givenStoredDefinition(releasedDefinition);
    trainingDefinitionService.switchState(
        releasedDefinition.getId(), cz.cyberrange.platform.training.api.enums.TDState.ARCHIVED);
    assertEquals(TDState.ARCHIVED, releasedDefinition.getState());
  }

  @Test
  public void switchState_RELEASEDtoUNRELEASED() {
    givenLoggedInEditor();
    givenStoredDefinition(releasedDefinition);
    given(trainingInstanceRepository.existsAnyForTrainingDefinition(releasedDefinition.getId()))
        .willReturn(false);
    trainingDefinitionService.switchState(
        releasedDefinition.getId(), cz.cyberrange.platform.training.api.enums.TDState.UNRELEASED);
    assertEquals(TDState.UNRELEASED, releasedDefinition.getState());
  }

  @Test
  public void switchState_RELEASEDtoUNRELEASED_withCreatedInstances() {
    givenStoredDefinition(releasedDefinition);
    given(trainingInstanceRepository.existsAnyForTrainingDefinition(releasedDefinition.getId()))
        .willReturn(true);
    assertThrows(
        EntityConflictException.class,
        () ->
            trainingDefinitionService.switchState(
                releasedDefinition.getId(),
                cz.cyberrange.platform.training.api.enums.TDState.UNRELEASED));
  }

  private void givenStoredDefinition(TrainingDefinition definition) {
    given(trainingDefinitionRepository.findById(definition.getId()))
        .willReturn(Optional.of(definition));
  }

  private void givenLoggedInEditor() {
    given(userService.getUserRefFromUserAndGroup()).willReturn(userRefDTO);
  }

  private void givenLoggedInAuthor() {
    givenLoggedInEditor();
    given(userRefRepository.createOrGet(anyLong())).willReturn(new UserRef());
  }

  private static TrainingLevel legacyTrainingLevel() {
    Hint hint = new Hint();
    hint.setTitle("Hint");
    hint.setContent("Hint content");
    hint.setHintPenalty(10);
    hint.setOrder(0);
    TrainingLevel level = new TrainingLevel();
    level.setId(11L);
    level.setTitle("Legacy training level");
    level.setOrder(5);
    level.setMaxScore(1);
    level.setAnswer("");
    level.setHints(new LinkedHashSet<>(List.of(hint)));
    return level;
  }

  private static AssessmentLevel legacyTestWithStatementLackingCorrectOption() {
    ExtendedMatchingOption option = new ExtendedMatchingOption();
    option.setText("Option");
    option.setOrder(0);
    ExtendedMatchingStatement statement = new ExtendedMatchingStatement();
    statement.setText("Statement");
    statement.setOrder(0);
    Question question = new Question();
    question.setQuestionType(QuestionType.EMI);
    question.setText("Match the statement");
    question.setOrder(4);
    question.setPoints(2);
    question.setExtendedMatchingOptions(new ArrayList<>(List.of(option)));
    question.setExtendedMatchingStatements(new ArrayList<>(List.of(statement)));
    AssessmentLevel level = new AssessmentLevel();
    level.setId(12L);
    level.setTitle("Legacy test");
    level.setOrder(8);
    level.setMaxScore(50);
    level.setAssessmentType(AssessmentType.TEST);
    level.setQuestions(new ArrayList<>(List.of(question)));
    return level;
  }

  private static TrainingDefinition updateContent() {
    TrainingDefinition content = new TrainingDefinition();
    content.setTitle("Updated title");
    content.setDescription("Updated description");
    content.setPrerequisites(new String[] {"Updated prerequisite"});
    content.setOutcomes(new String[] {"Updated outcome"});
    return content;
  }

  private static TrainingDefinitionDTO definitionResponse(Long definitionId) {
    TrainingDefinitionDTO definition = new TrainingDefinitionDTO();
    definition.setId(definitionId);
    return definition;
  }

  private static DefinitionInstanceCount instanceCount(Long definitionId, long count) {
    return new DefinitionInstanceCount() {
      @Override
      public Long getDefinitionId() {
        return definitionId;
      }

      @Override
      public long getInstanceCount() {
        return count;
      }
    };
  }
}
