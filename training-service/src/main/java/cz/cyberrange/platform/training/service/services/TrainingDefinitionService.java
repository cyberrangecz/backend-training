package cz.cyberrange.platform.training.service.services;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.training.api.exceptions.BadRequestException;
import cz.cyberrange.platform.training.api.exceptions.EntityConflictException;
import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.api.exceptions.UnprocessableEntityException;
import cz.cyberrange.platform.training.persistence.model.*;
import cz.cyberrange.platform.training.persistence.model.enums.AssessmentType;
import cz.cyberrange.platform.training.persistence.model.enums.QuestionType;
import cz.cyberrange.platform.training.persistence.model.enums.TDState;
import cz.cyberrange.platform.training.persistence.model.question.ExtendedMatchingOption;
import cz.cyberrange.platform.training.persistence.model.question.ExtendedMatchingStatement;
import cz.cyberrange.platform.training.persistence.model.question.Question;
import cz.cyberrange.platform.training.persistence.repository.*;
import cz.cyberrange.platform.training.service.mapping.mapstruct.CloneMapper;
import cz.cyberrange.platform.training.service.startup.DefaultLevelsLoader;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/** The type Training definition service. */
@Service
public class TrainingDefinitionService {

  private static final Logger LOG = LoggerFactory.getLogger(TrainingDefinitionService.class);
  private static final String ARCHIVED_OR_RELEASED =
      "Cannot edit released or archived training definition.";
  private static final String LEVEL_NOT_FOUND = "Level not found.";
  private CloneMapper cloneMapper;
  private TrainingDefinitionRepository trainingDefinitionRepository;
  private TrainingInstanceRepository trainingInstanceRepository;
  private AbstractLevelRepository abstractLevelRepository;
  private TrainingLevelRepository trainingLevelRepository;
  private InfoLevelRepository infoLevelRepository;
  private AssessmentLevelRepository assessmentLevelRepository;
  private AccessLevelRepository accessLevelRepository;
  private MitreTechniqueRepository mitreTechniqueRepository;
  private HintRepository hintRepository;
  private UserRefRepository userRefRepository;
  private SecurityService securityService;
  private UserService userService;
  private DefaultLevelsLoader defaultLevelsLoader;

  /**
   * Instantiates a new Training definition service.
   *
   * @param trainingDefinitionRepository the training definition repository
   * @param abstractLevelRepository the abstract level repository
   * @param infoLevelRepository the info level repository
   * @param trainingLevelRepository the training level repository
   * @param assessmentLevelRepository the assessment level repository
   * @param trainingInstanceRepository the training instance repository
   * @param userRefRepository the user ref repository
   */
  @Autowired
  public TrainingDefinitionService(
      TrainingDefinitionRepository trainingDefinitionRepository,
      AbstractLevelRepository abstractLevelRepository,
      InfoLevelRepository infoLevelRepository,
      TrainingLevelRepository trainingLevelRepository,
      AssessmentLevelRepository assessmentLevelRepository,
      AccessLevelRepository accessLevelRepository,
      TrainingInstanceRepository trainingInstanceRepository,
      MitreTechniqueRepository mitreTechniqueRepository,
      HintRepository hintRepository,
      UserRefRepository userRefRepository,
      SecurityService securityService,
      UserService userService,
      DefaultLevelsLoader defaultLevelsLoader,
      CloneMapper cloneMapper) {
    this.trainingDefinitionRepository = trainingDefinitionRepository;
    this.abstractLevelRepository = abstractLevelRepository;
    this.trainingLevelRepository = trainingLevelRepository;
    this.infoLevelRepository = infoLevelRepository;
    this.assessmentLevelRepository = assessmentLevelRepository;
    this.accessLevelRepository = accessLevelRepository;
    this.trainingInstanceRepository = trainingInstanceRepository;
    this.mitreTechniqueRepository = mitreTechniqueRepository;
    this.hintRepository = hintRepository;
    this.userRefRepository = userRefRepository;
    this.securityService = securityService;
    this.userService = userService;
    this.defaultLevelsLoader = defaultLevelsLoader;
    this.cloneMapper = cloneMapper;
  }

  /**
   * Finds specific Training Definition by id.
   *
   * @param id of a Training Definition that would be returned
   * @return specific {@link TrainingDefinition} by id
   * @throws EntityNotFoundException training definition cannot be found
   */
  public TrainingDefinition findById(Long id) {
    return trainingDefinitionRepository
        .findById(id)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    new EntityErrorDetail(TrainingDefinition.class, "id", Long.class, id)));
  }

  /**
   * Finds every Training Definition matching the given predicate, restricted by nothing else, its
   * authors and its beta testing group's organizers loaded along with each one.
   *
   * @param predicate represents a predicate (boolean-valued function) of one argument.
   * @param pageable pageable parameter with information about pagination.
   * @return the requested page of {@link TrainingDefinition}s
   */
  public Page<TrainingDefinition> findAll(Predicate predicate, Pageable pageable) {
    return trainingDefinitionRepository.findAll(predicate, pageable);
  }

  /**
   * Find all page.
   *
   * @param predicate the predicate
   * @param pageable the pageable
   * @param loggedInUserId the logged in user id
   * @return the page
   */
  public Page<TrainingDefinition> findAll(
      Predicate predicate, Pageable pageable, Long loggedInUserId) {
    return trainingDefinitionRepository.findAll(predicate, pageable, loggedInUserId);
  }

  /**
   * Finds all Training Definitions by state.
   *
   * @param state represents a state of training definition if it is released or unreleased.
   * @param pageable pageable parameter with information about pagination.
   * @return all Training Definitions for organizers
   */
  public Page<TrainingDefinition> findAllByState(TDState state, Pageable pageable) {
    return trainingDefinitionRepository.findAllByState(state, pageable);
  }

  /**
   * Find all for designers and organizers unreleased page.
   *
   * @param loggedInUserId the logged in user id
   * @param pageable the pageable
   * @return the page
   */
  public Page<TrainingDefinition> findAllForDesignersAndOrganizersUnreleased(
      Long loggedInUserId, Pageable pageable) {
    return trainingDefinitionRepository.findAllForDesignersAndOrganizersUnreleased(
        loggedInUserId, pageable);
  }

  /**
   * Find all for organizers unreleased page.
   *
   * @param loggedInUserId the logged in user id
   * @param pageable the pageable
   * @return the page
   */
  public Page<TrainingDefinition> findAllForOrganizersUnreleased(
      Long loggedInUserId, Pageable pageable) {
    return trainingDefinitionRepository.findAllForOrganizersUnreleased(loggedInUserId, pageable);
  }

  /**
   * Finds ids of all Training Definitions played by a user.
   *
   * @param userId a user id
   * @return the ids of played definitions
   */
  public Set<Long> findPlayedDefinitionIdsByUser(Long userId) {
    return trainingDefinitionRepository.findPlayedDefinitionIdsByUser(userId);
  }

  /**
   * Finds every MITRE technique key used by a training level of a released Training Definition.
   *
   * @return the MITRE technique usages, ordered by definition title, definition id and technique
   *     key
   */
  public List<TrainingDefinitionRepository.MitreTechniqueUsage>
      findMitreTechniqueUsagesOfReleasedDefinitions() {
    return trainingDefinitionRepository.findMitreTechniqueUsagesByState(TDState.RELEASED);
  }

  /**
   * Persists the given training definition, stamping it with the current time and the current
   * user's full name as its last editor, setting its creation time, adding the currently logged-in
   * user as an author, and, when requested, seeding it with a default intro info level and access
   * level.
   *
   * @param trainingDefinition the definition to persist
   * @param createDefaultContent whether to create the default intro info level and access level for
   *     the new definition
   * @return the persisted {@link TrainingDefinition}
   */
  public TrainingDefinition create(
      TrainingDefinition trainingDefinition, boolean createDefaultContent) {
    addLoggedInUserToTrainingDefinitionAsAuthor(trainingDefinition);
    trainingDefinition.setCreatedAt(getCurrentTimeInUTC());
    if (createDefaultContent) {
      this.createDefaultLevels(trainingDefinition);
    }
    LOG.info("Training definition with id: {} created.", trainingDefinition.getId());
    return auditAndSave(trainingDefinition);
  }

  /**
   * Persists the given training definition in place of the stored one with matching id, carrying
   * over the stored estimated duration regardless of the value given, adds the currently logged-in
   * user as one of its authors, and stamps it with the current time and the current user's full
   * name as its last editor.
   *
   * @param trainingDefinitionToUpdate the replacement definition, carrying the id of the definition
   *     to replace
   * @throws EntityNotFoundException when no training definition with the given id exists
   * @throws EntityConflictException when the stored definition is released or archived, or already
   *     has a training instance created
   */
  public void update(TrainingDefinition trainingDefinitionToUpdate) {
    TrainingDefinition trainingDefinition = findById(trainingDefinitionToUpdate.getId());
    checkIfCanBeUpdated(trainingDefinition);
    addLoggedInUserToTrainingDefinitionAsAuthor(trainingDefinitionToUpdate);
    trainingDefinitionToUpdate.setEstimatedDuration(trainingDefinition.getEstimatedDuration());
    auditAndSave(trainingDefinitionToUpdate);
    LOG.info("Training definition with id: {} updated.", trainingDefinitionToUpdate.getId());
  }

  /**
   * Creates a copy of the given training definition under a new title, together with a copy of
   * every one of its levels, adds the currently logged-in user as the clone's sole author, and
   * stamps the clone with the current time and the current user's full name as its last editor.
   *
   * @param id id of the definition to clone
   * @param title the title of the new, cloned definition
   * @return the persisted clone, {@link TrainingDefinition}
   * @throws EntityNotFoundException when no training definition with the given id exists
   */
  public TrainingDefinition clone(Long id, String title) {
    TrainingDefinition trainingDefinition = findById(id);
    TrainingDefinition clonedTrainingDefinition = cloneMapper.clone(trainingDefinition);
    clonedTrainingDefinition.setTitle(title);
    addLoggedInUserToTrainingDefinitionAsAuthor(clonedTrainingDefinition);
    clonedTrainingDefinition = auditAndSave(clonedTrainingDefinition);
    cloneLevelsFromTrainingDefinition(trainingDefinition.getId(), clonedTrainingDefinition);

    LOG.info("Training definition with id: {} cloned.", trainingDefinition.getId());
    return clonedTrainingDefinition;
  }

  /**
   * Swaps the order of two levels within a training definition, so each takes the other's position,
   * and updates the definition's last-edited time.
   *
   * @param definitionId id of the definition containing the levels
   * @param swapLevelFrom id of the first level to swap
   * @param swapLevelTo id of the second level to swap
   * @throws EntityNotFoundException when the training definition or either level does not exist
   * @throws EntityConflictException when the definition is released or archived, or already has a
   *     training instance created
   */
  public void swapLevels(Long definitionId, Long swapLevelFrom, Long swapLevelTo) {
    TrainingDefinition trainingDefinition = findById(definitionId);
    checkIfCanBeUpdated(trainingDefinition);
    AbstractLevel swapAbstractLevelFrom = this.findLevelByIdWithoutDefinition(swapLevelFrom);
    AbstractLevel swapAbstractLevelTo = this.findLevelByIdWithoutDefinition(swapLevelTo);
    int orderFromLevel = swapAbstractLevelFrom.getOrder();
    int orderToLevel = swapAbstractLevelTo.getOrder();
    swapAbstractLevelFrom.setOrder(orderToLevel);
    swapAbstractLevelTo.setOrder(orderFromLevel);
    auditAndSave(trainingDefinition);
  }

  /**
   * Moves a level to a new position among the levels of its training definition, shifting the order
   * of every level between the old and new position by one to keep the ordering contiguous, and
   * updates the definition's last-edited time. A requested position outside the current range is
   * silently clamped to the nearest valid position.
   *
   * @param definitionId id of the definition containing the levels
   * @param levelIdToBeMoved id of the level to move
   * @param newPosition the position to move the level to
   * @throws EntityNotFoundException when the training definition or the level does not exist
   * @throws EntityConflictException when the definition is released or archived, or already has a
   *     training instance created
   */
  public void moveLevel(Long definitionId, Long levelIdToBeMoved, Integer newPosition) {
    Integer maxOrderOfLevel = abstractLevelRepository.getCurrentMaxOrder(definitionId);
    if (newPosition < 0) {
      newPosition = 0;
    } else if (newPosition > maxOrderOfLevel) {
      newPosition = maxOrderOfLevel;
    }
    TrainingDefinition trainingDefinition = findById(definitionId);
    checkIfCanBeUpdated(trainingDefinition);
    AbstractLevel levelToBeMoved = this.findLevelByIdWithoutDefinition(levelIdToBeMoved);
    if (levelToBeMoved.getOrder() == newPosition) {
      return;
    } else if (levelToBeMoved.getOrder() < newPosition) {
      abstractLevelRepository.decreaseOrderOfLevels(
          definitionId, levelToBeMoved.getOrder() + 1, newPosition);
    } else {
      abstractLevelRepository.increaseOrderOfLevels(
          definitionId, newPosition, levelToBeMoved.getOrder() - 1);
    }
    levelToBeMoved.setOrder(newPosition);
    auditAndSave(trainingDefinition);
  }

  /**
   * Deletes the given training definition together with every one of its levels.
   *
   * @param definitionId id of the definition to delete
   * @throws EntityNotFoundException when no training definition with the given id exists
   * @throws EntityConflictException when the definition is released, or already has a training
   *     instance created
   */
  public void delete(Long definitionId) {
    TrainingDefinition definition = findById(definitionId);
    if (definition.getState().equals(TDState.RELEASED))
      throw new EntityConflictException(
          new EntityErrorDetail(
              TrainingDefinition.class,
              "id",
              definitionId.getClass(),
              definitionId,
              "Cannot delete released training definition."));
    if (trainingInstanceRepository.existsAnyForTrainingDefinition(definitionId)) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              TrainingDefinition.class,
              "id",
              definitionId.getClass(),
              definitionId,
              "Cannot delete training definition with already created training instance. "
                  + "Remove training instance/s before deleting training definition."));
    }
    List<AbstractLevel> abstractLevels =
        abstractLevelRepository.findAllLevelsByTrainingDefinitionId(definitionId);
    abstractLevels.forEach(this::deleteLevel);
    trainingDefinitionRepository.delete(definition);
  }

  /**
   * Deletes a level from a training definition, reduces the definition's estimated duration by the
   * deleted level's own duration, shifts down by one the order of every level that came after it to
   * close the gap it leaves, and updates the definition's last-edited time.
   *
   * @param definitionId id of the definition containing the level to delete
   * @param levelId id of the level to delete
   * @throws EntityNotFoundException when the training definition or the level does not exist
   * @throws EntityConflictException when the definition is not unreleased
   */
  public void deleteOneLevel(Long definitionId, Long levelId) {
    TrainingDefinition trainingDefinition = findById(definitionId);
    if (!trainingDefinition.getState().equals(TDState.UNRELEASED))
      throw new EntityConflictException(
          new EntityErrorDetail(
              AbstractLevel.class, "id", levelId.getClass(), levelId, ARCHIVED_OR_RELEASED));
    AbstractLevel abstractLevelToDelete = findLevelById(levelId);
    trainingDefinition.setEstimatedDuration(
        trainingDefinition.getEstimatedDuration() - abstractLevelToDelete.getEstimatedDuration());
    int orderOfDeleted = abstractLevelToDelete.getOrder();
    deleteLevel(abstractLevelToDelete);
    List<AbstractLevel> levels =
        abstractLevelRepository.findAllLevelsByTrainingDefinitionId(definitionId);
    for (AbstractLevel level : levels) {
      if (level.getOrder() > orderOfDeleted) {
        level.setOrder(level.getOrder() - 1);
      }
    }
    auditAndSave(trainingDefinition);
  }

  /**
   * Updates the training level identified by the id carried in the given level, replacing its
   * stored data within the given training definition.
   *
   * @param definitionId id of the definition the level must belong to
   * @param updatedTrainingLevel the level's new data, carrying the id of the level to update
   * @return the persisted {@link TrainingLevel}
   * @throws EntityNotFoundException when the training definition does not exist, the level does not
   *     exist, or the level does not belong to the given definition
   * @throws EntityConflictException when the definition is released or archived, or already has a
   *     training instance created
   */
  public TrainingLevel updateTrainingLevel(Long definitionId, TrainingLevel updatedTrainingLevel) {
    TrainingDefinition trainingDefinition = findById(definitionId);
    this.checkIfCanBeUpdated(trainingDefinition);
    TrainingLevel persistedTrainingLevel = findTrainingLevelById(updatedTrainingLevel.getId());
    this.checkIfLevelPresentInDefinition(definitionId, persistedTrainingLevel);
    return this.updateTrainingLevel(updatedTrainingLevel, persistedTrainingLevel);
  }

  /**
   * Replaces the stored data of a training level with the given one, keeping its attachments, order
   * and owning training definition, re-attaching its MITRE techniques by matching technique keys
   * against the database rather than reusing the given associations, and links each of its hints
   * back to it. Adjusts the owning training definition's estimated duration by the difference
   * between the old and new level duration and stamps its last-edited time, without invoking a save
   * for that definition.
   *
   * @param updatedTrainingLevel the level's new data
   * @param persistedTrainingLevel the level as currently stored, supplying the attachments, order
   *     and training definition the new data is completed with
   * @return the persisted {@link TrainingLevel}
   * @throws UnprocessableEntityException when the sum of hint penalties exceeds the level's maximal
   *     score
   * @throws BadRequestException when the free-text answer and its variable-name counterpart are not
   *     set consistently with whether the level uses variant answers
   */
  public TrainingLevel updateTrainingLevel(
      TrainingLevel updatedTrainingLevel, TrainingLevel persistedTrainingLevel) {
    updatedTrainingLevel.setAttachments(new HashSet<>(persistedTrainingLevel.getAttachments()));
    this.updateCommonLevelData(updatedTrainingLevel, persistedTrainingLevel);
    this.updateMitreTechniques(updatedTrainingLevel, persistedTrainingLevel);
    this.checkSumOfHintPenalties(updatedTrainingLevel);
    this.checkAnswerAndAnswerVariableName(updatedTrainingLevel);
    for (Hint hint : (updatedTrainingLevel).getHints()) {
      hint.setTrainingLevel(updatedTrainingLevel);
    }
    return trainingLevelRepository.save(updatedTrainingLevel);
  }

  /**
   * Replaces the stored data of an access level with the given one, keeping its estimated duration,
   * minimal solve time, order and owning training definition, and adjusts that definition's
   * estimated duration and last-edited time to reflect the change.
   *
   * @param updatedAccessLevel the level's new data
   * @param persistedAccessLevel the level as currently stored, supplying the values the new data is
   *     completed with
   * @return the persisted {@link AccessLevel}
   */
  public AccessLevel updateAccessLevel(
      AccessLevel updatedAccessLevel, AccessLevel persistedAccessLevel) {
    this.retainTimingDataAbsentFromUpdate(updatedAccessLevel, persistedAccessLevel);
    this.updateCommonLevelData(updatedAccessLevel, persistedAccessLevel);
    return accessLevelRepository.save(updatedAccessLevel);
  }

  /**
   * Updates the info level identified by the id carried in the given level, replacing its stored
   * data within the given training definition.
   *
   * @param definitionId id of the definition the level must belong to
   * @param updatedInfoLevel the level's new data, carrying the id of the level to update
   * @return the persisted {@link InfoLevel}
   * @throws EntityNotFoundException when the training definition does not exist, the level does not
   *     exist, or the level does not belong to the given definition
   * @throws EntityConflictException when the definition is released or archived, or already has a
   *     training instance created
   */
  public InfoLevel updateInfoLevel(Long definitionId, InfoLevel updatedInfoLevel) {
    TrainingDefinition trainingDefinition = findById(definitionId);
    this.checkIfCanBeUpdated(trainingDefinition);
    InfoLevel persistedInfoLevel = findInfoLevelById(updatedInfoLevel.getId());
    this.checkIfLevelPresentInDefinition(definitionId, persistedInfoLevel);
    return this.updateInfoLevel(updatedInfoLevel, persistedInfoLevel);
  }

  /**
   * Replaces the stored data of an info level with the given one, keeping its estimated duration,
   * minimal solve time, order and owning training definition, and adjusts that definition's
   * estimated duration and last-edited time to reflect the change.
   *
   * @param updatedInfoLevel the level's new data
   * @param persistedInfoLevel the level as currently stored, supplying the values the new data is
   *     completed with
   * @return the persisted {@link InfoLevel}
   */
  public InfoLevel updateInfoLevel(InfoLevel updatedInfoLevel, InfoLevel persistedInfoLevel) {
    this.retainTimingDataAbsentFromUpdate(updatedInfoLevel, persistedInfoLevel);
    this.updateCommonLevelData(updatedInfoLevel, persistedInfoLevel);
    return infoLevelRepository.save(updatedInfoLevel);
  }

  /**
   * Updates the assessment level identified by the id carried in the given level, replacing its
   * stored data within the given training definition.
   *
   * @param definitionId id of the definition the level must belong to
   * @param updatedAssessmentLevel the level's new data, carrying the id of the level to update
   * @return the persisted {@link AssessmentLevel}
   * @throws EntityNotFoundException when the training definition does not exist, the level does not
   *     exist, or the level does not belong to the given definition
   * @throws EntityConflictException when the definition is released or archived, or already has a
   *     training instance created
   */
  public AssessmentLevel updateAssessmentLevel(
      Long definitionId, AssessmentLevel updatedAssessmentLevel) {
    TrainingDefinition trainingDefinition = findById(definitionId);
    this.checkIfCanBeUpdated(trainingDefinition);
    AssessmentLevel persistedAssessmentLevel =
        findAssessmentLevelById(updatedAssessmentLevel.getId());
    this.checkIfLevelPresentInDefinition(definitionId, persistedAssessmentLevel);
    return this.updateAssessmentLevel(updatedAssessmentLevel, persistedAssessmentLevel);
  }

  /**
   * Replaces the stored data of an assessment level with the given one, keeping its order and
   * owning training definition, recomputing its maximal score as the sum of its questions' point
   * values regardless of the value given, and adjusts the owning definition's estimated duration
   * and last-edited time to reflect the change.
   *
   * @param updatedAssessmentLevel the level's new data
   * @param persistedAssessmentLevel the level as currently stored, supplying the values the new
   *     data is completed with
   * @return the persisted {@link AssessmentLevel}
   */
  public AssessmentLevel updateAssessmentLevel(
      AssessmentLevel updatedAssessmentLevel, AssessmentLevel persistedAssessmentLevel) {
    this.updateCommonLevelData(updatedAssessmentLevel, persistedAssessmentLevel);
    updatedAssessmentLevel.setMaxScore(
        updatedAssessmentLevel.getQuestions().stream().mapToInt(Question::getPoints).sum());
    return assessmentLevelRepository.save(updatedAssessmentLevel);
  }

  /**
   * Carries the stored duration and minimal solve time over to a level whose update request cannot
   * express them, so that saving the level leaves both values untouched.
   *
   * @param updatedLevel the level built from the update request
   * @param persistedLevel the level as currently stored
   */
  private void retainTimingDataAbsentFromUpdate(
      AbstractLevel updatedLevel, AbstractLevel persistedLevel) {
    updatedLevel.setEstimatedDuration(persistedLevel.getEstimatedDuration());
    updatedLevel.setMinimalPossibleSolveTime(persistedLevel.getMinimalPossibleSolveTime());
  }

  /**
   * Carries the order and owning training definition over from a persisted level to its updated
   * replacement, and adjusts that training definition's estimated duration by the difference
   * between the two levels' durations and stamps its last-edited time.
   *
   * @param updatedLevel the level built from the update request, completed in place
   * @param persistedLevel the level as currently stored
   */
  private void updateCommonLevelData(AbstractLevel updatedLevel, AbstractLevel persistedLevel) {
    TrainingDefinition trainingDefinition = persistedLevel.getTrainingDefinition();
    updatedLevel.setOrder(persistedLevel.getOrder());
    updatedLevel.setTrainingDefinition(trainingDefinition);
    trainingDefinition.setLastEdited(getCurrentTimeInUTC());
    trainingDefinition.setEstimatedDuration(
        trainingDefinition.getEstimatedDuration()
            - persistedLevel.getEstimatedDuration()
            + updatedLevel.getEstimatedDuration());
  }

  /**
   * Detaches a training level from its currently associated MITRE techniques and re-associates the
   * level built from the update request with the existing technique row for each requested
   * technique key, keeping the given association unchanged for a key with no matching row.
   *
   * @param updatedLevel the level built from the update request, whose MITRE technique associations
   *     are replaced
   * @param persistedLevel the level as currently stored, detached from its MITRE techniques
   */
  private void updateMitreTechniques(TrainingLevel updatedLevel, TrainingLevel persistedLevel) {
    // Removing training level from persisted MITRE techniques
    persistedLevel
        .getMitreTechniques()
        .forEach(
            t -> t.getTrainingLevels().removeIf(tl -> tl.getId().equals(persistedLevel.getId())));

    Set<String> techniqueKeys =
        updatedLevel.getMitreTechniques().stream()
            .map(MitreTechnique::getTechniqueKey)
            .collect(Collectors.toSet());
    Set<MitreTechnique> resultTechniques =
        mitreTechniqueRepository.findAllByTechniqueKeyIn(techniqueKeys);
    resultTechniques.addAll(updatedLevel.getMitreTechniques());

    updatedLevel.setMitreTechniques(new HashSet<>());
    resultTechniques.forEach(updatedLevel::addMitreTechnique);
  }

  /**
   * Creates a new training level with placeholder content, appending it after every existing level
   * of the given training definition, increases that definition's estimated duration by the new
   * level's own duration, and updates its last-edited time.
   *
   * @param definitionId id of the definition to add the level to
   * @return the persisted {@link TrainingLevel}
   * @throws EntityNotFoundException when no training definition with the given id exists
   * @throws EntityConflictException when the definition is released or archived, or already has a
   *     training instance created
   */
  public TrainingLevel createTrainingLevel(Long definitionId) {
    TrainingDefinition trainingDefinition = findById(definitionId);
    checkIfCanBeUpdated(trainingDefinition);
    TrainingLevel newTrainingLevel = initializeNewTrainingLevel();
    newTrainingLevel.setOrder(getNextOrder(definitionId));
    newTrainingLevel.setTrainingDefinition(trainingDefinition);
    TrainingLevel trainingLevel = trainingLevelRepository.save(newTrainingLevel);
    trainingDefinition.setEstimatedDuration(
        trainingDefinition.getEstimatedDuration() + newTrainingLevel.getEstimatedDuration());
    auditAndSave(trainingDefinition);
    LOG.info("Training level with id: {} created", trainingLevel.getId());
    return trainingLevel;
  }

  /**
   * Creates a new access level from the configured default content, appending it after every
   * existing level of the given training definition, and updates that definition's last-edited
   * time.
   *
   * @param definitionId id of the definition to add the level to
   * @return the persisted {@link AccessLevel}
   * @throws EntityNotFoundException when no training definition with the given id exists
   * @throws EntityConflictException when the definition is released or archived, or already has a
   *     training instance created
   */
  public AccessLevel createAccessLevel(Long definitionId) {
    TrainingDefinition trainingDefinition = findById(definitionId);
    checkIfCanBeUpdated(trainingDefinition);
    AccessLevel newAccessLevel = new AccessLevel();
    newAccessLevel.setTitle(defaultLevelsLoader.getDefaultAccessLevel().getTitle());
    newAccessLevel.setCloudContent(defaultLevelsLoader.getDefaultAccessLevel().getCloudContent());
    newAccessLevel.setLocalContent(defaultLevelsLoader.getDefaultAccessLevel().getLocalContent());
    newAccessLevel.setPasskey(defaultLevelsLoader.getDefaultAccessLevel().getPasskey());
    newAccessLevel.setOrder(getNextOrder(definitionId));
    newAccessLevel.setTrainingDefinition(trainingDefinition);
    AccessLevel accessLevel = accessLevelRepository.save(newAccessLevel);
    auditAndSave(trainingDefinition);
    LOG.info("Training level with id: {} created", accessLevel.getId());
    return accessLevel;
  }

  private TrainingLevel initializeNewTrainingLevel() {
    TrainingLevel newTrainingLevel = new TrainingLevel();
    newTrainingLevel.setMaxScore(100);
    newTrainingLevel.setTitle("Title of training level");
    newTrainingLevel.setIncorrectAnswerLimit(100);
    newTrainingLevel.setAnswer("Secret answer");
    newTrainingLevel.setSolutionPenalized(true);
    newTrainingLevel.setSolution("Solution of the training should be here");
    newTrainingLevel.setContent("The test entry should be here");
    newTrainingLevel.setEstimatedDuration(1);
    return newTrainingLevel;
  }

  private int getNextOrder(Long definitionId) {
    return abstractLevelRepository.getCurrentMaxOrder(definitionId) + 1;
  }

  /**
   * Creates a new info level from the configured default content, appending it after every existing
   * level of the given training definition, and updates that definition's last-edited time.
   *
   * @param definitionId id of the definition to add the level to
   * @return the persisted {@link InfoLevel}
   * @throws EntityNotFoundException when no training definition with the given id exists
   * @throws EntityConflictException when the definition is released or archived, or already has a
   *     training instance created
   */
  public InfoLevel createInfoLevel(Long definitionId) {
    TrainingDefinition trainingDefinition = findById(definitionId);
    checkIfCanBeUpdated(trainingDefinition);

    InfoLevel newInfoLevel = new InfoLevel();
    newInfoLevel.setTitle(defaultLevelsLoader.getDefaultInfoLevel().getTitle());
    newInfoLevel.setContent(defaultLevelsLoader.getDefaultInfoLevel().getContent());
    newInfoLevel.setOrder(getNextOrder(definitionId));
    newInfoLevel.setTrainingDefinition(trainingDefinition);
    InfoLevel infoLevel = infoLevelRepository.save(newInfoLevel);
    auditAndSave(trainingDefinition);
    LOG.info("Info level with id: {} created.", infoLevel.getId());
    return infoLevel;
  }

  /**
   * Creates a new assessment level with placeholder content, appending it after every existing
   * level of the given training definition, increases that definition's estimated duration by the
   * new level's own duration, and updates its last-edited time.
   *
   * @param definitionId id of the definition to add the level to
   * @return the persisted {@link AssessmentLevel}
   * @throws EntityNotFoundException when no training definition with the given id exists
   * @throws EntityConflictException when the definition is released or archived, or already has a
   *     training instance created
   */
  public AssessmentLevel createAssessmentLevel(Long definitionId) {
    TrainingDefinition trainingDefinition = findById(definitionId);
    checkIfCanBeUpdated(trainingDefinition);
    AssessmentLevel newAssessmentLevel = initializeNewAssessmentLevel();
    newAssessmentLevel.setOrder(getNextOrder(definitionId));
    newAssessmentLevel.setTrainingDefinition(trainingDefinition);
    AssessmentLevel assessmentLevel = assessmentLevelRepository.save(newAssessmentLevel);
    trainingDefinition.setEstimatedDuration(
        trainingDefinition.getEstimatedDuration() + newAssessmentLevel.getEstimatedDuration());
    auditAndSave(trainingDefinition);
    LOG.info("Assessment level with id: {} created.", assessmentLevel.getId());
    return assessmentLevel;
  }

  private AssessmentLevel initializeNewAssessmentLevel() {
    AssessmentLevel newAssessmentLevel = new AssessmentLevel();
    newAssessmentLevel.setTitle("Title of assessment level");
    newAssessmentLevel.setMaxScore(0);
    newAssessmentLevel.setAssessmentType(AssessmentType.QUESTIONNAIRE);
    newAssessmentLevel.setInstructions("Instructions should be here");
    newAssessmentLevel.setEstimatedDuration(1);
    return newAssessmentLevel;
  }

  /**
   * Finds all levels from single definition.
   *
   * @param definitionId of definition
   * @return list of {@link AbstractLevel} associated with training definition, ordered by level
   *     order
   */
  public List<AbstractLevel> findAllLevelsFromDefinition(Long definitionId) {
    return abstractLevelRepository.findAllLevelsByTrainingDefinitionId(definitionId);
  }

  /**
   * Finds all levels belonging to any of the given definitions.
   *
   * @param definitionIds ids of the definitions
   * @return list of {@link AbstractLevel} associated with any of the given training definitions,
   *     ordered by level order
   */
  public List<AbstractLevel> findAllLevelsFromDefinitions(Collection<Long> definitionIds) {
    return definitionIds.isEmpty()
        ? List.of()
        : abstractLevelRepository.findAllLevelsByTrainingDefinitionIdIn(definitionIds);
  }

  /**
   * Finds which of the given definitions still have a training instance running at or after the
   * given moment.
   *
   * @param definitionIds ids of the definitions to restrict the lookup to
   * @param time the moment an instance has to end after
   * @return ids of the definitions with at least one such instance
   */
  public Set<Long> findDefinitionIdsWithInstanceEndingAfter(
      Collection<Long> definitionIds, LocalDateTime time) {
    return definitionIds.isEmpty()
        ? Set.of()
        : new HashSet<>(
            trainingInstanceRepository.findTrainingDefinitionIdsWithInstanceEndingAfter(
                definitionIds, time));
  }

  /**
   * Decides whether the given training definition can be archived, which is the case exactly when
   * none of its training instances ends in the future.
   *
   * @param definitionId id of the definition to decide for
   * @return true when the definition has no instance ending after the current moment
   */
  public boolean canBeArchived(Long definitionId) {
    return findDefinitionIdsWithInstanceEndingAfter(
            Set.of(definitionId), LocalDateTime.now(Clock.systemUTC()))
        .isEmpty();
  }

  /**
   * Finds specific level by id with associated training definition.
   *
   * @param levelId - id of wanted level
   * @return wanted {@link AbstractLevel}
   * @throws EntityNotFoundException level is not found.
   */
  public AbstractLevel findLevelById(Long levelId) {
    return abstractLevelRepository
        .findByIdIncludingDefinition(levelId)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    new EntityErrorDetail(
                        AbstractLevel.class, "id", levelId.getClass(), levelId, LEVEL_NOT_FOUND)));
  }

  /**
   * Finds specific level by id.
   *
   * @param levelId - id of wanted level
   * @return wanted {@link AbstractLevel}
   * @throws EntityNotFoundException level is not found.
   */
  private AbstractLevel findLevelByIdWithoutDefinition(Long levelId) {
    return abstractLevelRepository
        .findById(levelId)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    new EntityErrorDetail(
                        AbstractLevel.class, "id", levelId.getClass(), levelId, LEVEL_NOT_FOUND)));
  }

  /**
   * Switches a training definition from unreleased to released, from released to archived, or from
   * released back to unreleased when it has no training instance created, doing nothing when the
   * requested state already matches the current one, and otherwise stamping the definition with the
   * current time and the current user's full name as its last editor. Every other transition,
   * including any transition away from an archived definition, is rejected.
   *
   * @param definitionId id of the definition to switch
   * @param state the requested new state
   * @throws EntityNotFoundException when no training definition with the given id exists
   * @throws EntityConflictException when the requested transition is not one of the allowed ones,
   *     or when switching a released definition back to unreleased while it still has a training
   *     instance created
   */
  public void switchState(
      Long definitionId, cz.cyberrange.platform.training.api.enums.TDState state) {
    TrainingDefinition trainingDefinition = findById(definitionId);
    if (trainingDefinition.getState().name().equals(state.name())) {
      return;
    }
    switch (trainingDefinition.getState()) {
      case UNRELEASED:
        if (state.equals(cz.cyberrange.platform.training.api.enums.TDState.RELEASED))
          trainingDefinition.setState(TDState.RELEASED);
        else
          throw new EntityConflictException(
              new EntityErrorDetail(
                  TrainingDefinition.class,
                  "id",
                  definitionId.getClass(),
                  definitionId,
                  "Cannot switch from" + trainingDefinition.getState() + " to " + state));
        break;
      case RELEASED:
        if (state.equals(cz.cyberrange.platform.training.api.enums.TDState.ARCHIVED))
          trainingDefinition.setState(TDState.ARCHIVED);
        else if (state.equals(cz.cyberrange.platform.training.api.enums.TDState.UNRELEASED)) {
          if (trainingInstanceRepository.existsAnyForTrainingDefinition(definitionId)) {
            throw new EntityConflictException(
                new EntityErrorDetail(
                    TrainingDefinition.class,
                    "id",
                    definitionId.getClass(),
                    definitionId,
                    "Cannot update training definition with already created training instance(s). "
                        + "Remove training instance(s) before changing the state from released to unreleased training definition."));
          }
          trainingDefinition.setState((TDState.UNRELEASED));
        }
        break;
      default:
        throw new EntityConflictException(
            new EntityErrorDetail(
                TrainingDefinition.class,
                "id",
                definitionId.getClass(),
                definitionId,
                "Cannot switch from " + trainingDefinition.getState() + " to " + state));
    }
    auditAndSave(trainingDefinition);
  }

  /**
   * Collects the levels of the given training definition that are training levels, leaving out
   * every level of another kind.
   *
   * @param trainingDefinitionId id of the training definition.
   * @return its {@link TrainingLevel}s in presentation order, empty when it holds none
   */
  public List<TrainingLevel> getAllTrainingLevels(Long trainingDefinitionId) {
    return findAllLevelsFromDefinition(trainingDefinitionId).stream()
        .filter(abstractLevel -> abstractLevel.getClass() == TrainingLevel.class)
        .map(abstractLevel -> (TrainingLevel) abstractLevel)
        .collect(Collectors.toList());
  }

  /**
   * Stamps the given training definition with the current time and the full name of the currently
   * logged-in user as its last editor, then persists it.
   *
   * @param trainingDefinition the training definition to persist
   * @return the persisted {@link TrainingDefinition}
   * @throws cz.cyberrange.platform.training.api.exceptions.MicroserviceApiException when the call
   *     to the user-and-group service to resolve the current user's full name fails
   */
  public TrainingDefinition auditAndSave(TrainingDefinition trainingDefinition) {
    trainingDefinition.setLastEdited(getCurrentTimeInUTC());
    trainingDefinition.setLastEditedBy(
        userService.getUserRefFromUserAndGroup().getUserRefFullName());
    return trainingDefinitionRepository.save(trainingDefinition);
  }

  /**
   * Creates and persists a default intro info level at order 0 and a default access level at order
   * 1 for the given training definition, both seeded from the configured default content.
   *
   * @param trainingDefinition the definition to seed with its default levels
   */
  private void createDefaultLevels(TrainingDefinition trainingDefinition) {
    InfoLevel introInfoLevel = new InfoLevel();
    introInfoLevel.setTrainingDefinition(trainingDefinition);
    introInfoLevel.setTitle(defaultLevelsLoader.getDefaultInfoLevel().getTitle());
    introInfoLevel.setContent(defaultLevelsLoader.getDefaultInfoLevel().getContent());
    introInfoLevel.setOrder(0);
    infoLevelRepository.save(introInfoLevel);

    AccessLevel getAccessLevel = new AccessLevel();
    getAccessLevel.setOrder(1);
    getAccessLevel.setTrainingDefinition(trainingDefinition);
    getAccessLevel.setCloudContent(defaultLevelsLoader.getDefaultAccessLevel().getCloudContent());
    getAccessLevel.setLocalContent(defaultLevelsLoader.getDefaultAccessLevel().getLocalContent());
    getAccessLevel.setTitle(defaultLevelsLoader.getDefaultAccessLevel().getTitle());
    getAccessLevel.setPasskey(defaultLevelsLoader.getDefaultAccessLevel().getPasskey());
    getAccessLevel.setTrainingDefinition(trainingDefinition);
    accessLevelRepository.save(getAccessLevel);
  }

  /**
   * Confirms that a level belongs to the given training definition.
   *
   * @param trainingDefinitionId id of the definition the level is expected to belong to
   * @param level the level to check
   * @throws EntityNotFoundException when the level's own training definition id does not match the
   *     given one
   */
  private void checkIfLevelPresentInDefinition(Long trainingDefinitionId, AbstractLevel level) {
    if (!level.getTrainingDefinition().getId().equals(trainingDefinitionId)) {
      throw new EntityNotFoundException(
          new EntityErrorDetail(
              AbstractLevel.class,
              "id",
              level.getId().getClass(),
              level.getId(),
              "Level was not found in definition (id: " + trainingDefinitionId + ")."));
    }
  }

  /**
   * Finds an assessment level by id.
   *
   * @param id id of the wanted level
   * @return the {@link AssessmentLevel}
   * @throws EntityNotFoundException when no assessment level with the given id exists
   */
  private AssessmentLevel findAssessmentLevelById(Long id) {
    return assessmentLevelRepository
        .findById(id)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    new EntityErrorDetail(
                        AbstractLevel.class, "id", id.getClass(), id, LEVEL_NOT_FOUND)));
  }

  /**
   * Finds a training level by id.
   *
   * @param id id of the wanted level
   * @return the {@link TrainingLevel}
   * @throws EntityNotFoundException when no training level with the given id exists
   */
  private TrainingLevel findTrainingLevelById(Long id) {
    return trainingLevelRepository
        .findById(id)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    new EntityErrorDetail(
                        AbstractLevel.class, "id", id.getClass(), id, LEVEL_NOT_FOUND)));
  }

  /**
   * Finds an info level by id.
   *
   * @param id id of the wanted level
   * @return the {@link InfoLevel}
   * @throws EntityNotFoundException when no info level with the given id exists
   */
  private InfoLevel findInfoLevelById(Long id) {
    return infoLevelRepository
        .findById(id)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    new EntityErrorDetail(
                        AbstractLevel.class, "id", id.getClass(), id, LEVEL_NOT_FOUND)));
  }

  private void cloneLevelsFromTrainingDefinition(
      Long trainingDefinitionId, TrainingDefinition clonedTrainingDefinition) {
    List<AbstractLevel> levels =
        abstractLevelRepository.findAllLevelsByTrainingDefinitionId(trainingDefinitionId);
    if (levels == null || levels.isEmpty()) {
      return;
    }
    levels.forEach(
        level -> {
          if (level instanceof AssessmentLevel) {
            cloneAssessmentLevel((AssessmentLevel) level, clonedTrainingDefinition);
          }
          if (level instanceof InfoLevel) {
            cloneInfoLevel((InfoLevel) level, clonedTrainingDefinition);
          }
          if (level instanceof TrainingLevel) {
            cloneTrainingLevel((TrainingLevel) level, clonedTrainingDefinition);
          }
          if (level instanceof AccessLevel) {
            cloneAccessLevel((AccessLevel) level, clonedTrainingDefinition);
          }
        });
  }

  private void cloneInfoLevel(InfoLevel level, TrainingDefinition trainingDefinition) {
    InfoLevel clonedInfoLevel = cloneMapper.clone(level);
    clonedInfoLevel.setTrainingDefinition(trainingDefinition);
    infoLevelRepository.save(clonedInfoLevel);
  }

  private void cloneAccessLevel(AccessLevel level, TrainingDefinition trainingDefinition) {
    AccessLevel clonedAccessLevel = cloneMapper.clone(level);
    clonedAccessLevel.setTrainingDefinition(trainingDefinition);
    accessLevelRepository.save(clonedAccessLevel);
  }

  /**
   * Clones an assessment level together with its questions into the given training definition, and
   * re-links each cloned extended-matching question's marked correct option to its counterpart
   * among the cloned options.
   *
   * @param level the assessment level to clone
   * @param trainingDefinition the definition the clone is attached to
   */
  private void cloneAssessmentLevel(AssessmentLevel level, TrainingDefinition trainingDefinition) {
    AssessmentLevel newAssessmentLevel = cloneMapper.clone(level);
    newAssessmentLevel.setTrainingDefinition(trainingDefinition);
    newAssessmentLevel.setQuestions(cloneQuestions(level.getQuestions()));
    assessmentLevelRepository.save(newAssessmentLevel);

    for (Question question :
        level.getQuestions().stream()
            .filter(question -> question.getQuestionType() == QuestionType.EMI)
            .collect(Collectors.toList())) {
      List<ExtendedMatchingStatement> extendedMatchingStatements =
          question.getExtendedMatchingStatements();
      Question clonedQuestion = newAssessmentLevel.getQuestions().get(question.getOrder());
      for (ExtendedMatchingStatement extendedMatchingStatement : extendedMatchingStatements) {
        if (extendedMatchingStatement.getExtendedMatchingOption() != null) {
          ExtendedMatchingStatement clonedExtendedMatchingStatement =
              clonedQuestion
                  .getExtendedMatchingStatements()
                  .get(extendedMatchingStatement.getOrder());
          int ordersOfExtendedMatchingOption =
              extendedMatchingStatement.getExtendedMatchingOption().getOrder();
          clonedExtendedMatchingStatement.setExtendedMatchingOption(
              clonedQuestion.getExtendedMatchingOptions().stream()
                  .filter(emo -> ordersOfExtendedMatchingOption == emo.getOrder())
                  .findFirst()
                  .get());
        }
      }
    }
  }

  private List<Question> cloneQuestions(List<Question> questions) {
    List<Question> clonedQuestions = new ArrayList<>();
    for (Question question : questions) {
      Question clonedQuestion = cloneMapper.clone(question);
      clonedQuestion.setChoices(cloneMapper.cloneChoices(question.getChoices()));
      clonedQuestion.setExtendedMatchingStatements(
          cloneMapper.cloneExtendedMatchingStatements(
              question.getExtendedMatchingStatements().stream()
                  .sorted(Comparator.comparing(ExtendedMatchingStatement::getOrder))
                  .collect(Collectors.toList())));
      clonedQuestion.setExtendedMatchingOptions(
          cloneMapper.cloneExtendedMatchingOptions(
              question.getExtendedMatchingOptions().stream()
                  .sorted(Comparator.comparing(ExtendedMatchingOption::getOrder))
                  .collect(Collectors.toList())));
      clonedQuestions.add(clonedQuestion);
    }
    return clonedQuestions;
  }

  private void cloneTrainingLevel(TrainingLevel level, TrainingDefinition trainingDefinition) {
    TrainingLevel newTrainingLevel = cloneMapper.clone(level);
    newTrainingLevel.setHints(cloneMapper.cloneHints(level.getHints()));
    newTrainingLevel.setAttachments(cloneMapper.cloneAttachments(level.getAttachments()));
    newTrainingLevel.setTrainingDefinition(trainingDefinition);
    trainingLevelRepository.save(newTrainingLevel);
  }

  /**
   * Rejects a training definition that cannot be updated: one whose state is not {@code
   * UNRELEASED}, or one already carrying a created training instance.
   *
   * @param trainingDefinition the definition to check
   * @throws EntityConflictException when either condition holds
   */
  public void checkIfCanBeUpdated(TrainingDefinition trainingDefinition) {
    if (!trainingDefinition.getState().equals(TDState.UNRELEASED)) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              TrainingDefinition.class,
              "id",
              trainingDefinition.getId().getClass(),
              trainingDefinition.getId(),
              ARCHIVED_OR_RELEASED));
    }
    if (trainingInstanceRepository.existsAnyForTrainingDefinition(trainingDefinition.getId())) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              TrainingDefinition.class,
              "id",
              trainingDefinition.getId().getClass(),
              trainingDefinition.getId(),
              "Cannot update training definition with already created training instance. "
                  + "Remove training instance/s before updating training definition."));
    }
  }

  /**
   * Deletes a level, dispatching to the repository matching its concrete type. For a training level
   * whose deletion is rejected by a database constraint, deletes its hints first and retries the
   * level deletion.
   *
   * @param level the level to delete
   */
  private void deleteLevel(AbstractLevel level) {
    if (level instanceof AssessmentLevel assessmentLevel) {
      assessmentLevelRepository.delete(assessmentLevel);
    } else if (level instanceof InfoLevel infoLevel) {
      infoLevelRepository.delete(infoLevel);
    } else if (level instanceof AccessLevel accessLevel) {
      accessLevelRepository.delete(accessLevel);
    } else {
      try {
        trainingLevelRepository.delete((TrainingLevel) level);
      } catch (ConstraintViolationException ex) {
        hintRepository.deleteHintsByLevelId(level.getId());
        trainingLevelRepository.delete((TrainingLevel) level);
      }
    }
  }

  private LocalDateTime getCurrentTimeInUTC() {
    return LocalDateTime.now(Clock.systemUTC());
  }

  /**
   * Adds the currently logged-in user as an author of the given training definition, inserting a
   * new {@link UserRef} row keyed on that user's cross-service {@code userRefId} when none exists
   * yet.
   *
   * @param trainingDefinition the definition to add the author to
   * @throws cz.cyberrange.platform.training.api.exceptions.MicroserviceApiException when the call
   *     to the user-and-group service to resolve the current user fails
   */
  private void addLoggedInUserToTrainingDefinitionAsAuthor(TrainingDefinition trainingDefinition) {
    UserRef user = userRefRepository.createOrGet(securityService.getUserRefIdFromUserAndGroup());
    trainingDefinition.addAuthor(user);
  }

  /**
   * Confirms that the sum of a training level's hint penalties does not exceed its maximal score.
   *
   * @param trainingLevel the level whose hints are checked
   * @throws UnprocessableEntityException when the sum of hint penalties exceeds the level's maximal
   *     score
   */
  private void checkSumOfHintPenalties(TrainingLevel trainingLevel) {
    int sumHintPenalty = 0;
    for (Hint hint : trainingLevel.getHints()) {
      sumHintPenalty += hint.getHintPenalty();
    }
    if (sumHintPenalty > trainingLevel.getMaxScore()) {
      throw new UnprocessableEntityException(
          new EntityErrorDetail(
              TrainingLevel.class,
              "id",
              Long.class,
              trainingLevel.getId(),
              "Sum of hint penalties cannot be greater than maximal score of the training level."));
    }
  }

  /**
   * Validates a training level's correct-answer fields, checking the static answer when the level
   * does not use variant answers or the answer variable name when it does.
   *
   * @param trainingLevel the level whose answer fields are checked
   * @throws BadRequestException propagated from the specific check performed, when the fields
   *     required by the level's answer mode are not set consistently
   */
  private void checkAnswerAndAnswerVariableName(TrainingLevel trainingLevel) {
    if (trainingLevel.isVariantAnswers()) {
      this.checkAnswerVariableName(trainingLevel);
    } else {
      this.checkAnswer(trainingLevel);
    }
  }

  /**
   * Confirms that a training level with a static answer has no answer variable name set and a
   * non-blank static answer.
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
   * Confirms that a training level with variant answers has no static answer set and a non-blank
   * answer variable name.
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

  /**
   * Finds the training definitions matching the given ids.
   *
   * @param ids ids of the definitions to look up
   * @return the matching {@link TrainingDefinition} entities; an id with no matching row is
   *     silently omitted, and the order of the returned entities is unspecified
   */
  public List<TrainingDefinition> findAllByIds(List<Long> ids) {
    return trainingDefinitionRepository.findAllByIdIn(ids);
  }

  /**
   * Finds the levels matching the given ids.
   *
   * @param ids ids of the levels to look up
   * @return the matching {@link AbstractLevel} entities; an id with no matching row is silently
   *     omitted, and the order of the returned entities is unspecified
   */
  public List<AbstractLevel> findAllLevelsByIds(List<Long> ids) {
    return abstractLevelRepository.findAllByIdIn(ids);
  }

  /**
   * Finds the hints matching the given ids.
   *
   * @param ids ids of the hints to look up
   * @return the matching {@link Hint} entities; an id with no matching row is silently omitted, and
   *     the order of the returned entities is unspecified
   */
  public List<Hint> findAllHintsByIds(List<Long> ids) {
    return hintRepository.findAllByIdIn(ids);
  }
}
