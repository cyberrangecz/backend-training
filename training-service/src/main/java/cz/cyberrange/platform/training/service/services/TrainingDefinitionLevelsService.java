package cz.cyberrange.platform.training.service.services;

import cz.cyberrange.platform.training.api.exceptions.BadRequestException;
import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.api.exceptions.UnprocessableEntityException;
import cz.cyberrange.platform.training.persistence.model.AbstractLevel;
import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import cz.cyberrange.platform.training.persistence.model.Attachment;
import cz.cyberrange.platform.training.persistence.model.Hint;
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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Writes the levels of a training definition: brings the stored levels in line with a given ordered
 * list, creating, updating, reordering and deleting levels, and deletes single levels.
 */
@Slf4j
@Service
public class TrainingDefinitionLevelsService {

  private final AbstractLevelRepository abstractLevelRepository;
  private final MitreTechniqueRepository mitreTechniqueRepository;
  private final HintRepository hintRepository;
  private final AttachmentRepository attachmentRepository;

  /**
   * Creates the service over the repositories of the levels, their hints and attachments, and the
   * MITRE techniques they reference.
   *
   * @param abstractLevelRepository the repository of levels of every type
   * @param mitreTechniqueRepository the MITRE technique repository
   * @param hintRepository the hint repository
   * @param attachmentRepository the attachment repository
   */
  @Autowired
  public TrainingDefinitionLevelsService(
      AbstractLevelRepository abstractLevelRepository,
      MitreTechniqueRepository mitreTechniqueRepository,
      HintRepository hintRepository,
      AttachmentRepository attachmentRepository) {
    this.abstractLevelRepository = abstractLevelRepository;
    this.mitreTechniqueRepository = mitreTechniqueRepository;
    this.hintRepository = hintRepository;
    this.attachmentRepository = attachmentRepository;
  }

  /**
   * Makes the given levels the whole level list of the given persisted definition. A level without
   * an id is created, a level carrying the id of one of the definition's levels replaces that
   * level, and every stored level the list leaves out is deleted. Each level takes its position in
   * the list as its order. A hint, question, choice, extended matching statement or option keeps
   * its id only when it belongs to the stored level or question it is sent under, and is created
   * anew otherwise; a MITRE technique is matched by its key against the stored ones. The questions
   * of an assessment and their choices, statements and options take their list positions as order.
   * The attachments of a created training level are stored and linked to it. The definition's
   * estimated duration becomes the sum of the levels' estimated durations.
   *
   * @param trainingDefinition the persisted definition whose levels are written
   * @param levels the levels the definition holds afterwards, in presentation order; each is saved
   *     and completed in place
   * @throws BadRequestException when a level id appears more than once, when a stored hint,
   *     question, choice, statement or option id appears more than once under one parent, when a
   *     level would change its type, when a training level's answer fields contradict its answer
   *     mode, or when a statement of a TEST assessment names none of its question's options as
   *     correct
   * @throws EntityNotFoundException when a level id names no level of the definition
   * @throws UnprocessableEntityException when a training level's hint penalties exceed its maximal
   *     score
   */
  public void replaceLevels(TrainingDefinition trainingDefinition, List<AbstractLevel> levels) {
    Map<Long, AbstractLevel> storedLevelsById =
        abstractLevelRepository
            .findAllLevelsByTrainingDefinitionId(trainingDefinition.getId())
            .stream()
            .collect(Collectors.toMap(AbstractLevel::getId, Function.identity()));
    checkLevelIds(trainingDefinition.getId(), levels, storedLevelsById);
    Set<Long> keptLevelIds =
        levels.stream()
            .map(AbstractLevel::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    storedLevelsById.values().stream()
        .filter(storedLevel -> !keptLevelIds.contains(storedLevel.getId()))
        .forEach(this::deleteLevel);
    for (int position = 0; position < levels.size(); position++) {
      AbstractLevel level = levels.get(position);
      level.setOrder(position);
      level.setTrainingDefinition(trainingDefinition);
      writeLevel(level, storedLevelsById.get(level.getId()));
    }
    trainingDefinition.setEstimatedDuration(
        levels.stream().mapToLong(AbstractLevel::getEstimatedDuration).sum());
  }

  /**
   * Deletes a level of any type. For a training level whose deletion is rejected by a database
   * constraint, deletes its hints first and retries the level deletion.
   *
   * @param level the level to delete
   */
  public void deleteLevel(AbstractLevel level) {
    try {
      abstractLevelRepository.delete(level);
    } catch (ConstraintViolationException ex) {
      if (!(level instanceof TrainingLevel)) {
        throw ex;
      }
      log.warn(
          "Deleting the hints of training level {} first after a constraint violation.",
          level.getId(),
          ex);
      hintRepository.deleteHintsByLevelId(level.getId());
      abstractLevelRepository.delete(level);
    }
  }

  /**
   * Rejects a level list that names a level twice, names a level the definition does not hold, or
   * names a stored level with a level of another type.
   *
   * @param definitionId id of the definition the levels are written to
   * @param levels the levels to write
   * @param storedLevelsById the definition's stored levels by their ids
   * @throws BadRequestException when a level id repeats or a level would change its type
   * @throws EntityNotFoundException when a level id names no level of the definition
   */
  private void checkLevelIds(
      Long definitionId, List<AbstractLevel> levels, Map<Long, AbstractLevel> storedLevelsById) {
    Set<Long> listedLevelIds = new HashSet<>();
    for (AbstractLevel level : levels) {
      Long levelId = level.getId();
      if (levelId == null) {
        continue;
      }
      if (!listedLevelIds.add(levelId)) {
        throw new BadRequestException(
            "Level "
                + levelId
                + " appears more than once in the level list. List each level once.");
      }
      AbstractLevel storedLevel = storedLevelsById.get(levelId);
      if (storedLevel == null) {
        throw new EntityNotFoundException(
            new EntityErrorDetail(
                AbstractLevel.class,
                "id",
                Long.class,
                levelId,
                "Level was not found in definition (id: " + definitionId + ")."));
      }
      if (storedLevel.getClass() != level.getClass()) {
        throw new BadRequestException(
            "Level "
                + levelId
                + " is a "
                + storedLevel.getClass().getSimpleName()
                + " and cannot become a "
                + level.getClass().getSimpleName()
                + ". Leave out its id to replace it with a new level.");
      }
    }
  }

  /**
   * Completes one level of the list from its stored counterpart and the checks of its type, then
   * saves it, together with the attachments of a new training level.
   *
   * @param level the level to save, in place
   * @param storedLevel the stored level it replaces, or null for a new level
   */
  private void writeLevel(AbstractLevel level, AbstractLevel storedLevel) {
    if (level instanceof TrainingLevel trainingLevel) {
      completeTrainingLevel(trainingLevel, (TrainingLevel) storedLevel);
    } else if (level instanceof AssessmentLevel assessmentLevel) {
      completeAssessmentLevel(assessmentLevel, (AssessmentLevel) storedLevel);
    } else if (storedLevel != null) {
      level.setMinimalPossibleSolveTime(storedLevel.getMinimalPossibleSolveTime());
    }
    abstractLevelRepository.save(level);
    if (storedLevel == null && level instanceof TrainingLevel trainingLevel) {
      saveNewAttachments(trainingLevel);
    }
  }

  /**
   * Stores the attachments of a newly saved training level, each linked to the level.
   *
   * @param trainingLevel the saved training level whose attachments are stored
   */
  private void saveNewAttachments(TrainingLevel trainingLevel) {
    Set<Attachment> attachments = trainingLevel.getAttachments();
    for (Attachment attachment : attachments) {
      attachment.setTrainingLevel(trainingLevel);
    }
    attachmentRepository.saveAll(attachments);
  }

  /**
   * Keeps the stored attachments of a replaced training level, keeps only its own hint ids, checks
   * the hint penalties and the answer fields, sorts the hints by their order, links them back to
   * the level and matches its MITRE techniques against the stored ones.
   *
   * @param trainingLevel the training level to complete
   * @param storedLevel the stored training level it replaces, or null for a new level
   * @throws UnprocessableEntityException when the hint penalties exceed the level's maximal score
   * @throws BadRequestException when the answer fields contradict the level's answer mode
   */
  private void completeTrainingLevel(TrainingLevel trainingLevel, TrainingLevel storedLevel) {
    if (storedLevel != null) {
      trainingLevel.setAttachments(new HashSet<>(storedLevel.getAttachments()));
      storedLevel.detachMitreTechniques();
    }
    keepOwnedIds(
        trainingLevel.getHints(),
        storedLevel == null ? Set.of() : storedLevel.getHints(),
        Hint::getId,
        Hint::setId,
        "Hint");
    checkSumOfHintPenalties(trainingLevel);
    checkAnswerAndAnswerVariableName(trainingLevel);
    trainingLevel.setHints(
        trainingLevel.getHints().stream()
            .sorted(Comparator.comparingInt(Hint::getOrder))
            .collect(Collectors.toCollection(LinkedHashSet::new)));
    trainingLevel.getHints().forEach(hint -> hint.setTrainingLevel(trainingLevel));
    matchStoredMitreTechniques(trainingLevel);
  }

  /**
   * Keeps only the assessment's own question ids and their own choice, statement and option ids,
   * gives the questions and their choices, statements and options their list position as order,
   * requires a TEST assessment to name the correct option of every statement, and sets the maximal
   * score to the sum of the questions' points.
   *
   * @param assessmentLevel the assessment level to complete
   * @param storedLevel the stored assessment level it replaces, or null for a new level
   * @throws BadRequestException when an id repeats among the items of one parent, or when a
   *     statement of a TEST assessment names none of its question's options as correct
   */
  private void completeAssessmentLevel(
      AssessmentLevel assessmentLevel, AssessmentLevel storedLevel) {
    List<Question> questions = assessmentLevel.getQuestions();
    keepOwnedIds(
        questions,
        storedLevel == null ? List.of() : storedLevel.getQuestions(),
        Question::getId,
        Question::setId,
        "Question");
    Map<Long, Question> storedQuestionsById =
        storedLevel == null
            ? Map.of()
            : storedLevel.getQuestions().stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));
    numberByPosition(questions, Question::setOrder);
    questions.forEach(
        question ->
            completeQuestionContent(
                question,
                question.getId() == null ? null : storedQuestionsById.get(question.getId())));
    if (assessmentLevel.getAssessmentType() == AssessmentType.TEST) {
      checkCorrectOptionsNamed(assessmentLevel);
    }
    assessmentLevel.setMaxScore(questions.stream().mapToInt(Question::getPoints).sum());
  }

  /**
   * Keeps only the question's own choice, statement and option ids and gives them their list
   * position as order.
   *
   * @param question the question whose content is completed
   * @param storedQuestion the stored question it replaces, or null for a new question
   * @throws BadRequestException when an id repeats among the choices, statements or options
   */
  private void completeQuestionContent(Question question, Question storedQuestion) {
    keepOwnedIds(
        question.getChoices(),
        storedQuestion == null ? List.of() : storedQuestion.getChoices(),
        QuestionChoice::getId,
        QuestionChoice::setId,
        "Choice");
    keepOwnedIds(
        question.getExtendedMatchingStatements(),
        storedQuestion == null ? List.of() : storedQuestion.getExtendedMatchingStatements(),
        ExtendedMatchingStatement::getId,
        ExtendedMatchingStatement::setId,
        "Statement");
    keepOwnedIds(
        question.getExtendedMatchingOptions(),
        storedQuestion == null ? List.of() : storedQuestion.getExtendedMatchingOptions(),
        ExtendedMatchingOption::getId,
        ExtendedMatchingOption::setId,
        "Option");
    numberByPosition(question.getChoices(), QuestionChoice::setOrder);
    numberByPosition(question.getExtendedMatchingStatements(), ExtendedMatchingStatement::setOrder);
    numberByPosition(question.getExtendedMatchingOptions(), ExtendedMatchingOption::setOrder);
  }

  /**
   * Gives each item of the list its position in the list as order.
   *
   * @param items the items to number
   * @param setOrder writes an item's order
   * @param <T> the item type
   */
  private static <T> void numberByPosition(List<T> items, ObjIntConsumer<T> setOrder) {
    for (int position = 0; position < items.size(); position++) {
      setOrder.accept(items.get(position), position);
    }
  }

  /**
   * Clears the id of every item whose id no stored item carries, so the item is created anew, and
   * keeps the id of every item the stored items carry.
   *
   * @param items the items to check
   * @param storedItems the stored items whose ids may be kept
   * @param idOf reads an item's id
   * @param setId writes an item's id
   * @param itemName names the kind of item in the refusal message
   * @param <T> the item type
   * @throws BadRequestException when two items keep the same stored id
   */
  private static <T> void keepOwnedIds(
      Collection<T> items,
      Collection<T> storedItems,
      Function<T, Long> idOf,
      BiConsumer<T, Long> setId,
      String itemName) {
    Set<Long> storedIds = storedItems.stream().map(idOf).collect(Collectors.toSet());
    Set<Long> keptIds = new HashSet<>();
    for (T item : items) {
      Long id = idOf.apply(item);
      if (!storedIds.contains(id)) {
        setId.accept(item, null);
      } else if (!keptIds.add(id)) {
        throw new BadRequestException(
            itemName
                + " "
                + id
                + " appears more than once under the same parent. List each of them once.");
      }
    }
  }

  /**
   * Requires every extended matching statement of the assessment's EMI questions to name one of its
   * question's options as correct.
   *
   * @param assessmentLevel the TEST assessment level to check
   * @throws BadRequestException when a statement names no correct option, or names an option its
   *     question does not have, identifying the statement and its question by position
   */
  private void checkCorrectOptionsNamed(AssessmentLevel assessmentLevel) {
    for (Question question : assessmentLevel.getQuestions()) {
      if (question.getQuestionType() != QuestionType.EMI) {
        continue;
      }
      List<ExtendedMatchingStatement> statements = question.getExtendedMatchingStatements();
      for (int position = 0; position < statements.size(); position++) {
        if (statements.get(position).getExtendedMatchingOption() == null) {
          throw new BadRequestException(
              "Statement "
                  + (position + 1)
                  + " of question "
                  + (question.getOrder() + 1)
                  + " names no correct option among the question's options. In a TEST"
                  + " assessment, every statement names one of them as correct.");
        }
      }
    }
  }

  /**
   * Replaces the level's MITRE techniques with the stored technique of the same key where one
   * exists, and with a new technique otherwise, and registers the level on each of them.
   *
   * @param trainingLevel the level whose techniques are matched in place
   */
  private void matchStoredMitreTechniques(TrainingLevel trainingLevel) {
    Set<String> techniqueKeys =
        trainingLevel.getMitreTechniques().stream()
            .map(MitreTechnique::getTechniqueKey)
            .collect(Collectors.toSet());
    Set<MitreTechnique> techniques =
        mitreTechniqueRepository.findAllByTechniqueKeyInOrderById(techniqueKeys);
    trainingLevel.getMitreTechniques().stream()
        .filter(technique -> !techniques.contains(technique))
        .forEach(
            technique -> {
              technique.setId(null);
              techniques.add(technique);
            });
    trainingLevel.setMitreTechniques(new HashSet<>());
    techniques.forEach(trainingLevel::addMitreTechnique);
  }

  /**
   * Requires the sum of a training level's hint penalties to stay within its maximal score.
   *
   * @param trainingLevel the level whose hints are checked
   * @throws UnprocessableEntityException when the sum of hint penalties exceeds the level's maximal
   *     score, naming the level by id, or by title for a new level
   */
  private void checkSumOfHintPenalties(TrainingLevel trainingLevel) {
    int sumOfHintPenalties = trainingLevel.getHints().stream().mapToInt(Hint::getHintPenalty).sum();
    if (sumOfHintPenalties <= trainingLevel.getMaxScore()) {
      return;
    }
    String reason =
        "Sum of hint penalties cannot be greater than maximal score of the training level.";
    throw new UnprocessableEntityException(
        trainingLevel.getId() == null
            ? new EntityErrorDetail(
                TrainingLevel.class, "title", String.class, trainingLevel.getTitle(), reason)
            : new EntityErrorDetail(
                TrainingLevel.class, "id", Long.class, trainingLevel.getId(), reason));
  }

  /**
   * Validates a training level's correct-answer fields, checking the static answer when the level
   * does not use variant answers or the answer variable name when it does.
   *
   * @param trainingLevel the level whose answer fields are checked
   * @throws BadRequestException when the fields required by the level's answer mode are not set
   *     consistently
   */
  private void checkAnswerAndAnswerVariableName(TrainingLevel trainingLevel) {
    if (trainingLevel.isVariantAnswers()) {
      checkAnswerVariableName(trainingLevel);
    } else {
      checkAnswer(trainingLevel);
    }
  }

  /**
   * Requires a training level with a static answer to carry a non-blank static answer and no answer
   * variable name.
   *
   * @param trainingLevel the level to check
   * @throws BadRequestException when the answer variable name is set, or the static answer is blank
   */
  private void checkAnswer(TrainingLevel trainingLevel) {
    if (trainingLevel.getAnswerVariableName() != null) {
      throw new BadRequestException("Field Correct Answer - Variable Name must be null.");
    }
    if (StringUtils.isBlank(trainingLevel.getAnswer())) {
      throw new BadRequestException("Field Correct Answer - Static cannot be empty.");
    }
  }

  /**
   * Requires a training level with variant answers to carry a non-blank answer variable name and no
   * static answer.
   *
   * @param trainingLevel the level to check
   * @throws BadRequestException when the static answer is set, or the answer variable name is blank
   */
  private void checkAnswerVariableName(TrainingLevel trainingLevel) {
    if (trainingLevel.getAnswer() != null) {
      throw new BadRequestException("Field Correct Answer - Static must be null.");
    }
    if (StringUtils.isBlank(trainingLevel.getAnswerVariableName())) {
      throw new BadRequestException("Field Correct Answer - Variable name cannot be empty.");
    }
  }
}
