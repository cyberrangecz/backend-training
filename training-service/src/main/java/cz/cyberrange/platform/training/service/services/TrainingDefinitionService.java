package cz.cyberrange.platform.training.service.services;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.InstanceCountHolder;
import cz.cyberrange.platform.training.api.exceptions.BadRequestException;
import cz.cyberrange.platform.training.api.exceptions.EntityConflictException;
import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.persistence.model.AbstractLevel;
import cz.cyberrange.platform.training.persistence.model.Hint;
import cz.cyberrange.platform.training.persistence.model.QTrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.model.enums.TDState;
import cz.cyberrange.platform.training.persistence.repository.AbstractLevelRepository;
import cz.cyberrange.platform.training.persistence.repository.AttachmentRepository;
import cz.cyberrange.platform.training.persistence.repository.HintRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingDefinitionRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingInstanceRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingInstanceRepository.DefinitionInstanceCount;
import cz.cyberrange.platform.training.persistence.repository.UserRefRepository;
import cz.cyberrange.platform.training.service.mapping.mapstruct.CloneMapper;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/** The type Training definition service */
@Slf4j
@Service
public class TrainingDefinitionService {

  private static final String ARCHIVED_OR_RELEASED =
      "Cannot edit released or archived training definition.";
  private static final String LEVEL_NOT_FOUND = "Level not found.";
  private final CloneMapper cloneMapper;
  private final TrainingDefinitionRepository trainingDefinitionRepository;
  private final TrainingInstanceRepository trainingInstanceRepository;
  private final AbstractLevelRepository abstractLevelRepository;
  private final HintRepository hintRepository;
  private final AttachmentRepository attachmentRepository;
  private final UserRefRepository userRefRepository;
  private final SecurityService securityService;
  private final UserService userService;
  private final TrainingDefinitionLevelsService trainingDefinitionLevelsService;

  /**
   * Instantiates a new Training definition service.
   *
   * @param trainingDefinitionRepository the training definition repository
   * @param abstractLevelRepository the abstract level repository
   * @param trainingInstanceRepository the training instance repository
   * @param hintRepository the hint repository
   * @param attachmentRepository the attachment repository
   * @param userRefRepository the user ref repository
   * @param securityService the security service
   * @param userService the user service
   * @param trainingDefinitionLevelsService the service writing the levels of a definition
   * @param cloneMapper the clone mapper
   */
  @Autowired
  public TrainingDefinitionService(
      TrainingDefinitionRepository trainingDefinitionRepository,
      AbstractLevelRepository abstractLevelRepository,
      TrainingInstanceRepository trainingInstanceRepository,
      HintRepository hintRepository,
      AttachmentRepository attachmentRepository,
      UserRefRepository userRefRepository,
      SecurityService securityService,
      UserService userService,
      TrainingDefinitionLevelsService trainingDefinitionLevelsService,
      CloneMapper cloneMapper) {
    this.trainingDefinitionRepository = trainingDefinitionRepository;
    this.abstractLevelRepository = abstractLevelRepository;
    this.trainingInstanceRepository = trainingInstanceRepository;
    this.hintRepository = hintRepository;
    this.attachmentRepository = attachmentRepository;
    this.userRefRepository = userRefRepository;
    this.securityService = securityService;
    this.userService = userService;
    this.trainingDefinitionLevelsService = trainingDefinitionLevelsService;
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
   * authors loaded along with each one.
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
   * Finds the unreleased Training Definitions the given user authored.
   *
   * @param loggedInUserId the logged in user id
   * @param pageable the pageable
   * @return the page
   */
  public Page<TrainingDefinition> findAllForDesignersUnreleased(
      Long loggedInUserId, Pageable pageable) {
    return trainingDefinitionRepository.findAll(
        QTrainingDefinition.trainingDefinition.state.eq(TDState.UNRELEASED),
        pageable,
        loggedInUserId);
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
   * Persists the given training definition together with its levels, stamping it with the current
   * time and the current user's full name as its last editor, setting its creation time and adding
   * the currently logged-in user as an author. Each level takes its position in the list as its
   * order, and the definition's estimated duration becomes the sum of the levels' estimated
   * durations.
   *
   * @param trainingDefinition the definition to persist
   * @param levels the levels of the new definition in presentation order, none carrying an id
   * @return the persisted {@link TrainingDefinition}, written to the database
   * @throws BadRequestException when a level fails a content check of {@link
   *     TrainingDefinitionLevelsService#replaceLevels}
   * @throws cz.cyberrange.platform.training.api.exceptions.UnprocessableEntityException when a
   *     training level's hint penalties exceed its maximal score
   */
  public TrainingDefinition create(
      TrainingDefinition trainingDefinition, List<AbstractLevel> levels) {
    TrainingDefinition createdTrainingDefinition = saveNewDefinition(trainingDefinition);
    trainingDefinitionLevelsService.replaceLevels(createdTrainingDefinition, levels);
    trainingDefinitionRepository.flush();
    log.info("Training definition with id: {} created.", createdTrainingDefinition.getId());
    return createdTrainingDefinition;
  }

  /**
   * Replaces the title, description, prerequisites, outcomes and whole level list of a stored
   * training definition, adds the currently logged-in user as one of its authors, and stamps it
   * with the current time and the current user's full name as its last editor. The definition must
   * be unreleased, have no training instance, and still carry the version the change was made on.
   * Each write raises the definition's version.
   *
   * @param id id of the definition to update
   * @param expectedVersion the version the caller last read
   * @param content carries the new title, description, prerequisites and outcomes
   * @param levels the levels the definition holds afterwards, in presentation order
   * @return the updated {@link TrainingDefinition}, written to the database
   * @throws EntityNotFoundException when no training definition with the given id exists, or a
   *     level id names no level of the definition
   * @throws EntityConflictException when the definition is released or archived, already has a
   *     training instance created, or carries another version
   * @throws BadRequestException when a level id repeats, a level would change its type, or a level
   *     fails a content check of {@link TrainingDefinitionLevelsService#replaceLevels}
   * @throws cz.cyberrange.platform.training.api.exceptions.UnprocessableEntityException when a
   *     training level's hint penalties exceed its maximal score
   */
  public TrainingDefinition update(
      Long id, long expectedVersion, TrainingDefinition content, List<AbstractLevel> levels) {
    TrainingDefinition trainingDefinition = findById(id);
    checkIfCanBeUpdated(trainingDefinition);
    if (trainingDefinition.getVersion() != expectedVersion) {
      throw definitionConflict(
          id,
          "Training definition "
              + id
              + " is at version "
              + trainingDefinition.getVersion()
              + ", the change was made on version "
              + expectedVersion
              + ". Reload the definition and repeat the change.");
    }
    trainingDefinition.setTitle(content.getTitle());
    trainingDefinition.setDescription(content.getDescription());
    trainingDefinition.setPrerequisites(content.getPrerequisites());
    trainingDefinition.setOutcomes(content.getOutcomes());
    addLoggedInUserToTrainingDefinitionAsAuthor(trainingDefinition);
    trainingDefinitionLevelsService.replaceLevels(trainingDefinition, levels);
    TrainingDefinition updatedTrainingDefinition = auditAndSave(trainingDefinition);
    trainingDefinitionRepository.flush();
    log.info("Training definition with id: {} updated.", id);
    return updatedTrainingDefinition;
  }

  /**
   * Creates a copy of the given training definition under a new title, together with a copy of
   * every one of its levels as stored: the clone is unreleased, the currently logged-in user is its
   * sole author, its creation time is the current time, and its levels keep their stored order and
   * content, training level attachments included, without any level content check.
   *
   * @param id id of the definition to clone
   * @param title the title of the new, cloned definition
   * @return the persisted clone, {@link TrainingDefinition}
   * @throws EntityNotFoundException when no training definition with the given id exists
   */
  public TrainingDefinition clone(Long id, String title) {
    TrainingDefinition clonedTrainingDefinition = cloneMapper.clone(findById(id));
    clonedTrainingDefinition.setTitle(title);
    TrainingDefinition createdTrainingDefinition = saveNewDefinition(clonedTrainingDefinition);
    for (AbstractLevel level : findAllLevelsFromDefinition(id)) {
      AbstractLevel clonedLevel = cloneMapper.cloneLevel(level);
      clonedLevel.setTrainingDefinition(createdTrainingDefinition);
      abstractLevelRepository.save(clonedLevel);
      if (clonedLevel instanceof TrainingLevel clonedTrainingLevel) {
        attachmentRepository.saveAll(clonedTrainingLevel.getAttachments());
      }
    }
    trainingDefinitionRepository.flush();
    log.info(
        "Training definition with id: {} cloned into {}.", id, createdTrainingDefinition.getId());
    return createdTrainingDefinition;
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
    if (definition.getState() == TDState.RELEASED) {
      throw definitionConflict(definitionId, "Cannot delete released training definition.");
    }
    if (trainingInstanceRepository.existsAnyForTrainingDefinition(definitionId)) {
      throw definitionConflict(
          definitionId,
          "Cannot delete training definition with already created training instance. "
              + "Remove training instance/s before deleting training definition.");
    }
    List<AbstractLevel> abstractLevels =
        abstractLevelRepository.findAllLevelsByTrainingDefinitionId(definitionId);
    abstractLevels.forEach(trainingDefinitionLevelsService::deleteLevel);
    trainingDefinitionRepository.delete(definition);
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
   * Sets on each holder the number of training instances of the definition it describes, regardless
   * of the instances' start and end time, using one grouped query for all holders. A definition
   * without instances counts zero.
   *
   * @param holders the definition responses to complete
   */
  public void completeInstanceCounts(Collection<? extends InstanceCountHolder> holders) {
    Set<Long> definitionIds =
        holders.stream().map(InstanceCountHolder::getId).collect(Collectors.toSet());
    if (definitionIds.isEmpty()) {
      return;
    }
    Map<Long, Long> countsByDefinitionId =
        trainingInstanceRepository.countGroupedByTrainingDefinition(definitionIds).stream()
            .collect(
                Collectors.toMap(
                    DefinitionInstanceCount::getDefinitionId,
                    DefinitionInstanceCount::getInstanceCount));
    holders.forEach(
        holder -> holder.setInstanceCount(countsByDefinitionId.getOrDefault(holder.getId(), 0L)));
  }

  /**
   * Decides whether the given training definition can be archived, which is the case exactly when
   * none of its training instances ends in the future.
   *
   * @param definitionId id of the definition to decide for
   * @return true when the definition has no instance ending after the current moment
   */
  public boolean canBeArchived(Long definitionId) {
    return findDefinitionIdsWithInstanceEndingAfter(Set.of(definitionId), getCurrentTimeInUTC())
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
                        AbstractLevel.class, "id", Long.class, levelId, LEVEL_NOT_FOUND)));
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
    TDState currentState = trainingDefinition.getState();
    TDState requestedState = TDState.valueOf(state.name());
    if (currentState == requestedState) {
      return;
    }
    if (currentState == TDState.UNRELEASED && requestedState == TDState.RELEASED) {
      trainingDefinition.setState(TDState.RELEASED);
    } else if (currentState == TDState.RELEASED && requestedState == TDState.ARCHIVED) {
      trainingDefinition.setState(TDState.ARCHIVED);
    } else if (currentState == TDState.RELEASED && requestedState == TDState.UNRELEASED) {
      if (trainingInstanceRepository.existsAnyForTrainingDefinition(definitionId)) {
        throw definitionConflict(
            definitionId,
            "Cannot update training definition with already created training instance(s). "
                + "Remove training instance(s) before changing the state from released to"
                + " unreleased training definition.");
      }
      trainingDefinition.setState(TDState.UNRELEASED);
    } else {
      throw definitionConflict(
          definitionId, "Cannot switch from " + currentState + " to " + requestedState);
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
   * Adds the currently logged-in user as an author of the given new training definition, sets its
   * creation time to the current time, then stamps and persists it as {@link #auditAndSave} does.
   *
   * @param trainingDefinition the new training definition to persist
   * @return the persisted {@link TrainingDefinition}
   */
  private TrainingDefinition saveNewDefinition(TrainingDefinition trainingDefinition) {
    addLoggedInUserToTrainingDefinitionAsAuthor(trainingDefinition);
    trainingDefinition.setCreatedAt(getCurrentTimeInUTC());
    return auditAndSave(trainingDefinition);
  }

  /**
   * Rejects a training definition that cannot be updated: one whose state is not {@code
   * UNRELEASED}, or one already carrying a created training instance.
   *
   * @param trainingDefinition the definition to check
   * @throws EntityConflictException when either condition holds
   */
  private void checkIfCanBeUpdated(TrainingDefinition trainingDefinition) {
    if (trainingDefinition.getState() != TDState.UNRELEASED) {
      throw definitionConflict(trainingDefinition.getId(), ARCHIVED_OR_RELEASED);
    }
    if (trainingInstanceRepository.existsAnyForTrainingDefinition(trainingDefinition.getId())) {
      throw definitionConflict(
          trainingDefinition.getId(),
          "Cannot update training definition with already created training instance. "
              + "Remove training instance/s before updating training definition.");
    }
  }

  /**
   * Builds the conflict refusing a write to a training definition.
   *
   * @param definitionId id of the definition the write was requested for
   * @param reason why the write is refused
   * @return the conflict naming the definition and the reason
   */
  private static EntityConflictException definitionConflict(Long definitionId, String reason) {
    return new EntityConflictException(
        new EntityErrorDetail(TrainingDefinition.class, "id", Long.class, definitionId, reason));
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
   * Finds the training definitions matching the given ids.
   *
   * @param ids ids of the definitions to look up
   * @return the matching {@link TrainingDefinition} entities; an id with no matching row is
   *     silently omitted, and the order of the returned entities is unspecified
   */
  public List<TrainingDefinition> findAllByIds(List<Long> ids) {
    return trainingDefinitionRepository.findAllByIdInOrderById(ids);
  }

  /**
   * Finds the levels matching the given ids.
   *
   * @param ids ids of the levels to look up
   * @return the matching {@link AbstractLevel} entities; an id with no matching row is silently
   *     omitted, and the order of the returned entities is unspecified
   */
  public List<AbstractLevel> findAllLevelsByIds(List<Long> ids) {
    return abstractLevelRepository.findAllByIdInOrderById(ids);
  }

  /**
   * Finds the hints matching the given ids.
   *
   * @param ids ids of the hints to look up
   * @return the matching {@link Hint} entities; an id with no matching row is silently omitted, and
   *     the order of the returned entities is unspecified
   */
  public List<Hint> findAllHintsByIds(List<Long> ids) {
    return hintRepository.findAllByIdInOrderById(ids);
  }
}
