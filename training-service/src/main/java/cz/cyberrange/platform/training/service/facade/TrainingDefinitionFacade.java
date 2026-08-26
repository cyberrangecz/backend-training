package cz.cyberrange.platform.training.service.facade;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.training.api.dto.AbstractLevelBasicDTO;
import cz.cyberrange.platform.training.api.dto.AbstractLevelDTO;
import cz.cyberrange.platform.training.api.dto.AbstractLevelUpdateDTO;
import cz.cyberrange.platform.training.api.dto.BasicLevelInfoDTO;
import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.dto.accesslevel.AccessLevelUpdateDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.cyberrange.platform.training.api.dto.hint.HintBasicDTO;
import cz.cyberrange.platform.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.AbstractTrainingDefinitionDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionBasicDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionCreateDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionInfoDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionMitreTechniquesDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionUpdateDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionWithLevelsDTO;
import cz.cyberrange.platform.training.api.dto.traininglevel.TrainingLevelUpdateDTO;
import cz.cyberrange.platform.training.api.enums.QuestionType;
import cz.cyberrange.platform.training.api.enums.RoleType;
import cz.cyberrange.platform.training.api.enums.TDState;
import cz.cyberrange.platform.training.api.exceptions.BadRequestException;
import cz.cyberrange.platform.training.api.exceptions.EntityConflictException;
import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.api.exceptions.InternalServerErrorException;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.AbstractLevel;
import cz.cyberrange.platform.training.persistence.model.AccessLevel;
import cz.cyberrange.platform.training.persistence.model.AssessmentLevel;
import cz.cyberrange.platform.training.persistence.model.BetaTestingGroup;
import cz.cyberrange.platform.training.persistence.model.InfoLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingLevel;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.model.enums.AssessmentType;
import cz.cyberrange.platform.training.persistence.model.question.ExtendedMatchingOption;
import cz.cyberrange.platform.training.persistence.model.question.ExtendedMatchingStatement;
import cz.cyberrange.platform.training.persistence.model.question.Question;
import cz.cyberrange.platform.training.persistence.repository.TrainingDefinitionRepository;
import cz.cyberrange.platform.training.service.annotations.security.IsDesignerOrAdmin;
import cz.cyberrange.platform.training.service.annotations.security.IsDesignerOrOrganizerOrAdmin;
import cz.cyberrange.platform.training.service.annotations.security.IsOrganizerOrAdmin;
import cz.cyberrange.platform.training.service.annotations.security.IsTraineeOrAdmin;
import cz.cyberrange.platform.training.service.annotations.transactions.TransactionalRO;
import cz.cyberrange.platform.training.service.annotations.transactions.TransactionalWO;
import cz.cyberrange.platform.training.service.enums.RoleTypeSecurity;
import cz.cyberrange.platform.training.service.mapping.mapstruct.HintMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.LevelMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.TrainingDefinitionMapper;
import cz.cyberrange.platform.training.service.services.SecurityService;
import cz.cyberrange.platform.training.service.services.TrainingDefinitionService;
import cz.cyberrange.platform.training.service.services.UserService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The type Training definition facade. */
@Service
@Transactional
public class TrainingDefinitionFacade {

  private final TrainingDefinitionService trainingDefinitionService;
  private final UserService userService;
  private final SecurityService securityService;
  private final TrainingDefinitionMapper trainingDefinitionMapper;
  private final LevelMapper levelMapper;
  private final HintMapper hintMapper;

  /**
   * Instantiates a new Training definition facade.
   *
   * @param trainingDefinitionService the training definition service
   * @param trainingDefMapper the training def mapper
   * @param levelMapper the level mapper
   * @param userService the user service
   * @param securityService the security service
   */
  @Autowired
  public TrainingDefinitionFacade(
      TrainingDefinitionService trainingDefinitionService,
      UserService userService,
      SecurityService securityService,
      TrainingDefinitionMapper trainingDefMapper,
      LevelMapper levelMapper,
      HintMapper hintMapper) {
    this.trainingDefinitionService = trainingDefinitionService;
    this.userService = userService;
    this.securityService = securityService;
    this.trainingDefinitionMapper = trainingDefMapper;
    this.levelMapper = levelMapper;
    this.hintMapper = hintMapper;
  }

  /**
   * Finds one training definition together with the full detail of every level it holds and with
   * the flag telling whether it can be archived.
   *
   * @param id id of the training definition to return
   * @return the {@link TrainingDefinitionWithLevelsDTO} of that definition, its levels in
   *     presentation order
   * @throws EntityNotFoundException when no training definition with the given id exists
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isDesignerOfGivenTrainingDefinition(#id)"
          + "or @securityService.isOrganizerForGivenTrainingDefinition(#id)")
  @TransactionalRO
  public TrainingDefinitionWithLevelsDTO findById(Long id) {
    TrainingDefinition trainingDefinition = trainingDefinitionService.findById(id);
    return addArchivingInfo(
        trainingDefinitionMapper.mapToDTOWithLevels(trainingDefinition, gatherLevels(id)));
  }

  /**
   * Sets the archiving flag on the given definition DTO according to whether the definition it
   * describes can currently be archived.
   *
   * @param trainingDefinitionDTO the DTO to complete, carrying the id of the definition to decide
   *     for
   * @return the same instance, with the flag set
   */
  private <T extends TrainingDefinitionDTO> T addArchivingInfo(T trainingDefinitionDTO) {
    trainingDefinitionDTO.setCanBeArchived(
        trainingDefinitionService.canBeArchived(trainingDefinitionDTO.getId()));
    return trainingDefinitionDTO;
  }

  /**
   * Collects the basic level information of every level of the given definition, in presentation
   * order.
   *
   * @param definitionId id of the definition whose levels are collected
   * @return the levels' basic information, empty when the definition holds none
   */
  private List<BasicLevelInfoDTO> gatherBasicLevelInfo(Long definitionId) {
    return trainingDefinitionService.findAllLevelsFromDefinition(definitionId).stream()
        .map(this.levelMapper::mapToBasicLevelInfoDTO)
        .collect(Collectors.toList());
  }

  /**
   * Collects the full detail of every level of the given definition, each mapped to the DTO subtype
   * matching its concrete level type.
   *
   * @param definitionId id of the definition whose levels are collected
   * @return the levels in presentation order, empty when the definition holds none
   */
  private List<AbstractLevelDTO> gatherLevels(Long definitionId) {
    List<AbstractLevel> levels =
        trainingDefinitionService.findAllLevelsFromDefinition(definitionId);
    return levels.stream().map(this.levelMapper::mapToDTO).collect(Collectors.toList());
  }

  /**
   * Collects the basic level information of every level belonging to any of the given definitions,
   * grouped by the id of the definition each level belongs to.
   *
   * @param definitionIds ids of the definitions whose levels are collected
   * @return the levels grouped by definition id; a definition with no levels has no entry
   */
  private Map<Long, List<AbstractLevelBasicDTO>> gatherBasicLevelsByDefinitionId(
      Collection<Long> definitionIds) {
    return trainingDefinitionService.findAllLevelsFromDefinitions(definitionIds).stream()
        .collect(
            Collectors.groupingBy(
                level -> level.getTrainingDefinition().getId(),
                Collectors.mapping(this.levelMapper::mapToBasicDTO, Collectors.toList())));
  }

  /**
   * Finds Training Definitions matching the given predicate. An administrator sees every matching
   * definition; any other caller sees only definitions where they are an author or a beta testing
   * organizer.
   *
   * @param predicate represents a predicate (boolean-valued function) of one argument.
   * @param pageable pageable parameter with information about pagination.
   * @return page of matching {@link TrainingDefinitionDTO}, each flagged with whether it can be
   *     archived
   */
  @IsDesignerOrAdmin
  @TransactionalRO
  public PageResultResource<TrainingDefinitionDTO> findAll(Predicate predicate, Pageable pageable) {
    if (securityService.hasRole(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)) {
      return mapToDtoAndAddArchivingInfo(trainingDefinitionService.findAll(predicate, pageable));
    } else {
      Long loggedInUserId = securityService.getUserRefIdFromUserAndGroup();
      return mapToDtoAndAddArchivingInfo(
          trainingDefinitionService.findAll(predicate, pageable, loggedInUserId));
    }
  }

  /**
   * Maps a page of training definition entities to their DTOs and sets each one's archiving flag
   * according to whether any of its training instances ends in the future.
   *
   * @param trainingDefinitionPage the page of entities to map
   * @return the mapped page, with the archiving flag set on every DTO
   */
  private PageResultResource<TrainingDefinitionDTO> mapToDtoAndAddArchivingInfo(
      Page<TrainingDefinition> trainingDefinitionPage) {
    PageResultResource<TrainingDefinitionDTO> resource =
        trainingDefinitionMapper.mapToPageResultResource(trainingDefinitionPage);
    List<Long> definitionIds =
        resource.getContent().stream()
            .map(AbstractTrainingDefinitionDTO::getId)
            .collect(Collectors.toList());
    Set<Long> definitionIdsWithRunningInstance =
        trainingDefinitionService.findDefinitionIdsWithInstanceEndingAfter(
            definitionIds, LocalDateTime.now(Clock.systemUTC()));
    for (TrainingDefinitionDTO trainingDefinitionDTO : resource.getContent()) {
      trainingDefinitionDTO.setCanBeArchived(
          !definitionIdsWithRunningInstance.contains(trainingDefinitionDTO.getId()));
    }
    return resource;
  }

  /**
   * Finds released Training Definitions using MITRE techniques, each flagged with whether the
   * requesting user has played it. Definitions without any MITRE technique are not included.
   *
   * @return the {@link TrainingDefinitionMitreTechniquesDTO} of definitions using MITRE techniques
   */
  @IsTraineeOrAdmin
  @TransactionalRO
  public List<TrainingDefinitionMitreTechniquesDTO> findPlayedMitreTechniques() {
    Set<Long> playedDefinitionIds =
        trainingDefinitionService.findPlayedDefinitionIdsByUser(
            securityService.getUserRefIdFromUserAndGroup());
    Map<Long, TrainingDefinitionMitreTechniquesDTO> techniquesByDefinition = new LinkedHashMap<>();
    for (TrainingDefinitionRepository.MitreTechniqueUsage usage :
        trainingDefinitionService.findMitreTechniqueUsagesOfReleasedDefinitions()) {
      techniquesByDefinition
          .computeIfAbsent(
              usage.getDefinitionId(),
              definitionId ->
                  new TrainingDefinitionMitreTechniquesDTO(
                      definitionId,
                      usage.getTitle(),
                      playedDefinitionIds.contains(definitionId),
                      new ArrayList<>()))
          .getMitreTechniques()
          .add(usage.getTechniqueKey());
    }
    return List.copyOf(techniquesByDefinition.values());
  }

  /**
   * Finds Training Definitions in the given state, restricted to those the caller may see. For
   * {@link TDState#RELEASED} every released definition is returned. For {@link TDState#UNRELEASED},
   * an administrator sees every unreleased definition; a caller holding both the designer and
   * organizer roles sees unreleased definitions where they are a designer or organizer; any other
   * caller sees only unreleased definitions where they are an organizer.
   *
   * @param state whether released or unreleased definitions are wanted
   * @param pageable pageable parameter with information about pagination.
   * @return page of matching {@link TrainingDefinitionInfoDTO}
   * @throws InternalServerErrorException when {@code state} is neither released nor unreleased
   */
  @IsOrganizerOrAdmin
  @TransactionalRO
  public PageResultResource<TrainingDefinitionInfoDTO> findAllForOrganizers(
      TDState state, Pageable pageable) {
    Long loggedInUserId = securityService.getUserRefIdFromUserAndGroup();
    if (state == TDState.RELEASED) {
      return trainingDefinitionMapper.mapToPageResultResourceInfoDTO(
          trainingDefinitionService.findAllByState(
              cz.cyberrange.platform.training.persistence.model.enums.TDState.RELEASED, pageable));
    } else if (state == TDState.UNRELEASED) {
      if (securityService.hasRole(RoleTypeSecurity.ROLE_TRAINING_ADMINISTRATOR)) {
        return trainingDefinitionMapper.mapToPageResultResourceInfoDTO(
            trainingDefinitionService.findAllByState(
                cz.cyberrange.platform.training.persistence.model.enums.TDState.UNRELEASED,
                pageable));
      } else if (securityService.hasRole(RoleTypeSecurity.ROLE_TRAINING_DESIGNER)
          && securityService.hasRole(RoleTypeSecurity.ROLE_TRAINING_ORGANIZER)) {
        return trainingDefinitionMapper.mapToPageResultResourceInfoDTO(
            trainingDefinitionService.findAllForDesignersAndOrganizersUnreleased(
                loggedInUserId, pageable));
      } else {
        return trainingDefinitionMapper.mapToPageResultResourceInfoDTO(
            trainingDefinitionService.findAllForOrganizersUnreleased(loggedInUserId, pageable));
      }
    }
    throw new InternalServerErrorException(
        "It is required to provide training definition state that is RELEASED or UNRELEASED");
  }

  /**
   * Creates a new training definition, assigning the requesting user as an author and, when a beta
   * testing group is given, creating that group with its organizers. When the request asks for
   * default content, a first info level and a first access level with default content are created
   * alongside it.
   *
   * @param trainingDefinition the definition to create
   * @return the created definition together with its levels, {@link
   *     TrainingDefinitionWithLevelsDTO}
   */
  @IsDesignerOrAdmin
  @TransactionalWO
  public TrainingDefinitionWithLevelsDTO create(TrainingDefinitionCreateDTO trainingDefinition) {
    TrainingDefinition newTrainingDefinition =
        trainingDefinitionMapper.mapCreateToEntity(trainingDefinition);
    if (trainingDefinition.getBetaTestingGroup() != null) {
      addOrganizersToTrainingDefinition(
          newTrainingDefinition, trainingDefinition.getBetaTestingGroup().getOrganizersRefIds());
    }
    TrainingDefinition createdTrainingDefinition =
        trainingDefinitionService.create(
            newTrainingDefinition, trainingDefinition.isDefaultContent());
    return addArchivingInfo(
        trainingDefinitionMapper.mapToDTOWithLevels(
            createdTrainingDefinition, gatherLevels(createdTrainingDefinition.getId())));
  }

  /**
   * Updates a training definition, carrying its creation timestamp and estimated duration over
   * from the stored definition, and its existing authors joined by the requesting user.
   * Reachable only by an administrator or by a designer of the definition being updated. The beta
   * testing group cannot be removed by this call: if the request carries none while the stored
   * definition has one, the update is refused.
   *
   * @param trainingDefinitionUpdateDTO the update, carrying the id of the definition to update
   * @throws EntityNotFoundException when no training definition with the given id exists
   * @throws EntityConflictException when the stored definition's beta testing group would be
   *     removed, or when the definition is not {@link TDState#UNRELEASED} or already has a
   *     training instance
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isDesignerOfGivenTrainingDefinition(#trainingDefinitionUpdateDTO.getId())")
  @TransactionalWO
  public void update(TrainingDefinitionUpdateDTO trainingDefinitionUpdateDTO) {
    TrainingDefinition mappedTrainingDefinition =
        trainingDefinitionMapper.mapUpdateToEntity(trainingDefinitionUpdateDTO);
    TrainingDefinition trainingDefinition =
        trainingDefinitionService.findById(trainingDefinitionUpdateDTO.getId());
    mappedTrainingDefinition.setCreatedAt(trainingDefinition.getCreatedAt());
    mappedTrainingDefinition.setAuthors(new HashSet<>(trainingDefinition.getAuthors()));
    if (trainingDefinitionUpdateDTO.getBetaTestingGroup() != null) {
      addOrganizersToTrainingDefinition(
          mappedTrainingDefinition,
          trainingDefinitionUpdateDTO.getBetaTestingGroup().getOrganizersRefIds());
      if (trainingDefinition.getBetaTestingGroup() != null) {
        trainingDefinition
            .getBetaTestingGroup()
            .setId(trainingDefinition.getBetaTestingGroup().getId());
      }
    } else if (trainingDefinition.getBetaTestingGroup() != null) {
      throw new EntityConflictException(
          new EntityErrorDetail(
              BetaTestingGroup.class,
              "id",
              Long.class,
              trainingDefinition.getBetaTestingGroup().getId(),
              "Cannot delete beta testing group. You only can remove organizers from group."));
    }
    trainingDefinitionService.update(mappedTrainingDefinition);
  }

  /**
   * Replaces the organizers of the given training definition's beta testing group with the users
   * carrying the given ids, creating a local user reference for any of them that does not have one
   * yet.
   *
   * @param trainingDefinition the definition whose beta testing group's organizers are replaced
   * @param userRefIds cross-service ids of the users to set as organizers
   */
  private void addOrganizersToTrainingDefinition(
      TrainingDefinition trainingDefinition, Set<Long> userRefIds) {
    trainingDefinition.getBetaTestingGroup().setOrganizers(new HashSet<>());
    PageResultResource<UserRefDTO> organizers =
        userService.getUsersRefDTOByGivenUserIds(
            new ArrayList<>(userRefIds), PageRequest.of(0, 999), null, null);
    for (UserRefDTO organizer : organizers.getContent()) {
      UserRef userRef = userService.createOrGetUserRef(organizer.getUserRefId());
      trainingDefinition.getBetaTestingGroup().addOrganizer(userRef);
    }
  }

  /**
   * Clones a training definition together with its levels, assigning the requesting user as an
   * author of the clone. Reachable only by an administrator or by a designer of the definition
   * being cloned.
   *
   * @param id id of the definition to clone
   * @param title the title to give the clone
   * @return the cloned definition together with its levels, {@link
   *     TrainingDefinitionWithLevelsDTO}
   * @throws EntityNotFoundException when no training definition with the given id exists
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isDesignerOfGivenTrainingDefinition(#id)")
  @TransactionalWO
  public TrainingDefinitionWithLevelsDTO clone(Long id, String title) {
    TrainingDefinition clonedDefinition = trainingDefinitionService.clone(id, title);
    return addArchivingInfo(
        trainingDefinitionMapper.mapToDTOWithLevels(
            clonedDefinition, gatherLevels(clonedDefinition.getId())));
  }

  /**
   * Swaps the order of two levels within a training definition, so each takes the other's
   * position, and records the definition as edited.
   *
   * @param definitionId id of the definition containing the levels
   * @param swapLevelFrom id of the first level to swap
   * @param swapLevelTo id of the second level to swap
   * @return the basic information of every level of the definition, in presentation order
   * @throws EntityNotFoundException when the definition or either level does not exist
   * @throws EntityConflictException when the definition is not {@link TDState#UNRELEASED} or
   *     already has a training instance
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
  @TransactionalWO
  public List<BasicLevelInfoDTO> swapLevels(
      Long definitionId, Long swapLevelFrom, Long swapLevelTo) {
    trainingDefinitionService.swapLevels(definitionId, swapLevelFrom, swapLevelTo);
    return gatherBasicLevelInfo(definitionId);
  }

  /**
   * Moves a level to a new position within its definition, shifting the levels between the old
   * and new position to close the gap, and records the definition as edited. A requested
   * position outside the definition's range is clamped to the nearest valid position rather than
   * rejected.
   *
   * @param definitionId id of the definition containing the level
   * @param levelIdToBeMoved id of the level to move
   * @param newPosition position to move the level to
   * @return the basic information of every level of the definition, in presentation order
   * @throws EntityNotFoundException when the definition or the level does not exist
   * @throws EntityConflictException when the definition is not {@link TDState#UNRELEASED} or
   *     already has a training instance
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
  @TransactionalWO
  public List<BasicLevelInfoDTO> moveLevel(
      Long definitionId, Long levelIdToBeMoved, Integer newPosition) {
    trainingDefinitionService.moveLevel(definitionId, levelIdToBeMoved, newPosition);
    return gatherBasicLevelInfo(definitionId);
  }

  /**
   * Deletes a training definition together with all of its levels.
   *
   * @param id id of the definition to delete
   * @throws EntityNotFoundException when no training definition with the given id exists
   * @throws EntityConflictException when the definition is {@link TDState#RELEASED} or already
   *     has a training instance
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isDesignerOfGivenTrainingDefinition(#id)")
  @TransactionalWO
  public void delete(Long id) {
    trainingDefinitionService.delete(id);
  }

  /**
   * Deletes a level, shifting the order of every level after it back by one and reducing the
   * definition's estimated duration by the deleted level's own estimated duration.
   *
   * @param definitionId - id of definition containing level to be deleted
   * @param levelId - id of level to be deleted
   * @return the list of {@link BasicLevelInfoDTO} about all levels from given definition
   * @throws EntityNotFoundException when the definition or the level does not exist
   * @throws EntityConflictException when the definition is not {@link TDState#UNRELEASED}
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
  @TransactionalWO
  public List<BasicLevelInfoDTO> deleteOneLevel(Long definitionId, Long levelId) {
    trainingDefinitionService.deleteOneLevel(definitionId, levelId);
    return gatherBasicLevelInfo(definitionId);
  }

  /**
   * Updates several levels of one training definition in a single call, dispatching each update to
   * the handling matching its level type. For an assessment level of type {@link
   * AssessmentType#TEST}, the correct option of every extended matching statement is resolved and
   * set before the level is saved.
   *
   * @param definitionId - id of training definition containing levels to be updated
   * @param updatedLevelDTOs updated levels to be stored
   * @throws EntityNotFoundException when the definition does not exist, or when one of the given
   *     levels does not belong to it
   * @throws EntityConflictException when the definition is not {@link TDState#UNRELEASED} or
   *     already has a training instance
   * @throws BadRequestException when a TEST assessment level leaves the correct option of an
   *     extended matching statement unset
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
  @TransactionalWO
  public void updateLevels(Long definitionId, List<AbstractLevelUpdateDTO> updatedLevelDTOs) {
    TrainingDefinition trainingDefinition = trainingDefinitionService.findById(definitionId);
    trainingDefinitionService.checkIfCanBeUpdated(trainingDefinition);
    Map<Long, AbstractLevel> persistedLevelsById =
        trainingDefinitionService.findAllLevelsFromDefinition(definitionId).stream()
            .collect(Collectors.toMap(AbstractLevel::getId, Function.identity()));
    for (var updatedLevelDTO : updatedLevelDTOs) {
      AbstractLevel persistedLevel = persistedLevelsById.get(updatedLevelDTO.getId());
      if (persistedLevel == null) {
        throw new EntityNotFoundException(
            new EntityErrorDetail(
                AbstractLevel.class,
                "id",
                Long.class,
                updatedLevelDTO.getId(),
                "Level was not found in definition (id: " + definitionId + ")."));
      }
      switch (updatedLevelDTO.getLevelType()) {
        case TRAINING_LEVEL:
          TrainingLevel updatedTrainingLevel =
              levelMapper.mapUpdateToEntity((TrainingLevelUpdateDTO) updatedLevelDTO);
          trainingDefinitionService.updateTrainingLevel(
              updatedTrainingLevel, (TrainingLevel) persistedLevel);
          break;
        case ACCESS_LEVEL:
          AccessLevel updatedAccessLevel =
              levelMapper.mapUpdateToEntity((AccessLevelUpdateDTO) updatedLevelDTO);
          trainingDefinitionService.updateAccessLevel(
              updatedAccessLevel, (AccessLevel) persistedLevel);
          break;
        case INFO_LEVEL:
          InfoLevel updatedInfoLevel =
              levelMapper.mapUpdateToEntity((InfoLevelUpdateDTO) updatedLevelDTO);
          trainingDefinitionService.updateInfoLevel(updatedInfoLevel, (InfoLevel) persistedLevel);
          break;
        case ASSESSMENT_LEVEL:
          AssessmentLevel updatedAssessmentLevel =
              levelMapper.mapUpdateToEntity((AssessmentLevelUpdateDTO) updatedLevelDTO);
          if (updatedAssessmentLevel.getAssessmentType() == AssessmentType.TEST) {
            this.checkAndSetCorrectOptionsOfStatements(
                updatedAssessmentLevel, (AssessmentLevelUpdateDTO) updatedLevelDTO);
          }
          trainingDefinitionService.updateAssessmentLevel(
              updatedAssessmentLevel, (AssessmentLevel) persistedLevel);
          break;
      }
    }
    this.trainingDefinitionService.auditAndSave(trainingDefinition);
  }

  /**
   * Updates a training level of the given training definition.
   *
   * @param definitionId - id of training definition containing level to be updated
   * @param trainingLevel to be updated
   * @throws EntityNotFoundException when the definition does not exist, the level does not exist,
   *     or the level does not belong to the definition
   * @throws EntityConflictException when the definition is not {@link TDState#UNRELEASED} or
   *     already has a training instance
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
  @TransactionalWO
  public void updateTrainingLevel(Long definitionId, TrainingLevelUpdateDTO trainingLevel) {
    TrainingLevel trainingLevelToUpdate = levelMapper.mapUpdateToEntity(trainingLevel);
    TrainingLevel updatedTrainingLevel =
        trainingDefinitionService.updateTrainingLevel(definitionId, trainingLevelToUpdate);
    this.trainingDefinitionService.auditAndSave(updatedTrainingLevel.getTrainingDefinition());
  }

  /**
   * Updates an info level of the given training definition.
   *
   * @param definitionId - id of training definition containing level to be updated
   * @param infoLevel to be updated
   * @throws EntityNotFoundException when the definition does not exist, the level does not exist,
   *     or the level does not belong to the definition
   * @throws EntityConflictException when the definition is not {@link TDState#UNRELEASED} or
   *     already has a training instance
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
  @TransactionalWO
  public void updateInfoLevel(Long definitionId, InfoLevelUpdateDTO infoLevel) {
    InfoLevel updatedInfoLevel =
        trainingDefinitionService.updateInfoLevel(
            definitionId, levelMapper.mapUpdateToEntity(infoLevel));
    this.trainingDefinitionService.auditAndSave(updatedInfoLevel.getTrainingDefinition());
  }

  /**
   * Updates an assessment level of the given training definition. When the assessment is of type
   * {@link AssessmentType#TEST}, the correct option of every extended matching statement is
   * resolved and set before the level is saved.
   *
   * @param definitionId - id of training definition containing level to be updated
   * @param assessmentLevelToUpdate to be updated
   * @throws EntityNotFoundException when the definition does not exist, the level does not exist,
   *     or the level does not belong to the definition
   * @throws EntityConflictException when the definition is not {@link TDState#UNRELEASED} or
   *     already has a training instance
   * @throws BadRequestException when a TEST assessment leaves the correct option of an extended
   *     matching statement unset
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
  @TransactionalWO
  public void updateAssessmentLevel(
      Long definitionId, AssessmentLevelUpdateDTO assessmentLevelToUpdate) {
    AssessmentLevel assessmentLevel = levelMapper.mapUpdateToEntity(assessmentLevelToUpdate);
    if (assessmentLevel.getAssessmentType() == AssessmentType.TEST) {
      this.checkAndSetCorrectOptionsOfStatements(assessmentLevel, assessmentLevelToUpdate);
    }
    AssessmentLevel updatedAssessmentLevel =
        trainingDefinitionService.updateAssessmentLevel(definitionId, assessmentLevel);
    this.trainingDefinitionService.auditAndSave(updatedAssessmentLevel.getTrainingDefinition());
  }

  /**
   * Resolves and sets the correct extended matching option of every extended matching statement of
   * every EMI question in the given assessment level, matching statement and option by their order
   * within the question.
   *
   * @param assessmentLevel the mapped assessment level whose statements are completed in place
   * @param assessmentLevelUpdateDTO the update carrying the correct option order for each statement
   * @throws BadRequestException when a statement leaves its correct option order unset
   */
  private void checkAndSetCorrectOptionsOfStatements(
      AssessmentLevel assessmentLevel, AssessmentLevelUpdateDTO assessmentLevelUpdateDTO) {
    assessmentLevelUpdateDTO.getQuestions().stream()
        .filter(questionDTO -> questionDTO.getQuestionType() == QuestionType.EMI)
        .forEach(
            questionDTO ->
                questionDTO
                    .getExtendedMatchingStatements()
                    .forEach(
                        statementDTO -> {
                          if (statementDTO.getCorrectOptionOrder() == null) {
                            throw new BadRequestException(
                                "You must set the correct option for the each statement in the assessment of the type TEST");
                          }
                          Question question =
                              assessmentLevel.getQuestions().get(questionDTO.getOrder());
                          ExtendedMatchingOption correctOption =
                              question
                                  .getExtendedMatchingOptions()
                                  .get(statementDTO.getCorrectOptionOrder());
                          ExtendedMatchingStatement statementToUpdate =
                              question.getExtendedMatchingStatements().get(statementDTO.getOrder());
                          statementToUpdate.setExtendedMatchingOption(correctOption);
                        }));
  }

  /**
   * Creates a new info level with default content, appended after the definition's current last
   * level.
   *
   * @param definitionId - id of definition in which level will be created
   * @return {@link BasicLevelInfoDTO} of new info level
   * @throws EntityNotFoundException when no training definition with the given id exists
   * @throws EntityConflictException when the definition is not {@link TDState#UNRELEASED} or
   *     already has a training instance
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
  @TransactionalWO
  public BasicLevelInfoDTO createInfoLevel(Long definitionId) {
    InfoLevel newInfoLevel = trainingDefinitionService.createInfoLevel(definitionId);
    return levelMapper.mapTo(newInfoLevel);
  }

  /**
   * Creates a new training level with default placeholder content, appended after the definition's
   * current last level.
   *
   * @param definitionId - id of definition in which level will be created
   * @return {@link BasicLevelInfoDTO} of new training level
   * @throws EntityNotFoundException when no training definition with the given id exists
   * @throws EntityConflictException when the definition is not {@link TDState#UNRELEASED} or
   *     already has a training instance
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
  @TransactionalWO
  public BasicLevelInfoDTO createTrainingLevel(Long definitionId) {
    TrainingLevel newTrainingLevel = trainingDefinitionService.createTrainingLevel(definitionId);
    return levelMapper.mapTo(newTrainingLevel);
  }

  /**
   * Creates a new access level with default content, appended after the definition's current last
   * level.
   *
   * @param definitionId - id of definition in which level will be created
   * @return {@link BasicLevelInfoDTO} of new access level
   * @throws EntityNotFoundException when no training definition with the given id exists
   * @throws EntityConflictException when the definition is not {@link TDState#UNRELEASED} or
   *     already has a training instance
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
  @TransactionalWO
  public BasicLevelInfoDTO createAccessLevel(Long definitionId) {
    AccessLevel newAccessLevel = trainingDefinitionService.createAccessLevel(definitionId);
    return levelMapper.mapTo(newAccessLevel);
  }

  /**
   * Creates a new assessment level of type {@link AssessmentType#QUESTIONNAIRE} with default
   * placeholder content, appended after the definition's current last level.
   *
   * @param definitionId - id of definition in which level will be created
   * @return {@link BasicLevelInfoDTO} of new assessment level
   * @throws EntityNotFoundException when no training definition with the given id exists
   * @throws EntityConflictException when the definition is not {@link TDState#UNRELEASED} or
   *     already has a training instance
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
  @TransactionalWO
  public BasicLevelInfoDTO createAssessmentLevel(Long definitionId) {
    AssessmentLevel newAssessmentLevel =
        trainingDefinitionService.createAssessmentLevel(definitionId);
    return levelMapper.mapTo(newAssessmentLevel);
  }

  /**
   * Finds a level by id. Reachable by any designer or administrator, regardless of whether they
   * are a designer of the level's own training definition.
   *
   * @param levelId - id of wanted level
   * @return wanted {@link AbstractLevelDTO}
   * @throws EntityNotFoundException when no level with the given id exists
   */
  @IsDesignerOrAdmin
  @TransactionalRO
  public AbstractLevelDTO findLevelById(Long levelId) {
    return levelMapper.mapToDTO(trainingDefinitionService.findLevelById(levelId));
  }

  /**
   * Asks the user-and-group service for one page of the users holding the given role.
   *
   * @param roleType the role its holders are requested for
   * @param pageable pageable parameter with information about pagination.
   * @param givenName restricts the result to users whose given name matches, no restriction when
   *     null
   * @param familyName restricts the result to users whose family name matches, no restriction
   *     when null
   * @return the requested page of users holding that role
   * @throws cz.cyberrange.platform.training.api.exceptions.MicroserviceApiException when the call
   *     to the user-and-group service fails
   */
  @IsDesignerOrAdmin
  @TransactionalRO
  public PageResultResource<UserRefDTO> getUsersWithGivenRole(
      RoleType roleType, Pageable pageable, String givenName, String familyName) {
    return userService.getUsersByGivenRole(roleType, pageable, givenName, familyName);
  }

  /**
   * Switches the state of a training definition. Only {@link TDState#UNRELEASED} to {@link
   * TDState#RELEASED}, {@link TDState#RELEASED} to {@link TDState#ARCHIVED}, and {@link
   * TDState#RELEASED} back to {@link TDState#UNRELEASED} are allowed transitions; requesting the
   * definition's current state is a no-op. Switching a released definition back to unreleased is
   * refused while it has a training instance.
   *
   * @param definitionId - id of training definition
   * @param state - the state to switch to
   * @throws EntityNotFoundException when no training definition with the given id exists
   * @throws EntityConflictException when the transition is not one of the allowed ones, or when
   *     switching from released to unreleased while a training instance still exists
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
  public void switchState(Long definitionId, TDState state) {
    trainingDefinitionService.switchState(definitionId, state);
  }

  /**
   * Finds Training Definitions by their ids, together with the basic detail of their levels. An
   * id matching no training definition is silently omitted from the result.
   *
   * @param ids the ids of Training Definitions to return.
   * @return the matching {@link TrainingDefinitionBasicDTO}s, each carrying its levels
   */
  @TransactionalRO
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.participatesInTrainingDefinitions(#ids)")
  public List<TrainingDefinitionBasicDTO> findTrainingDefinitionsByIds(List<Long> ids) {
    List<TrainingDefinition> definitions = trainingDefinitionService.findAllByIds(ids);
    Map<Long, List<AbstractLevelBasicDTO>> levelsByDefinitionId =
        gatherBasicLevelsByDefinitionId(
            definitions.stream().map(TrainingDefinition::getId).collect(Collectors.toList()));
    return definitions.stream()
        .map(
            definition ->
                trainingDefinitionMapper.mapToBasicDTO(
                    definition, levelsByDefinitionId.getOrDefault(definition.getId(), List.of())))
        .collect(Collectors.toList());
  }

  /**
   * Finds levels by their ids, each mapped to the basic DTO subtype matching its concrete level
   * type. An id matching no level is silently omitted from the result.
   *
   * @param ids the ids of Levels to return.
   * @return the matching {@link AbstractLevelBasicDTO}s.
   */
  @TransactionalRO
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.participatesInLevels(#ids)")
  public List<AbstractLevelBasicDTO> findLevelsByIds(List<Long> ids) {
    return levelMapper.mapToBasicDtoList(trainingDefinitionService.findAllLevelsByIds(ids));
  }

  /**
   * Finds hints by their ids. An id matching no hint is silently omitted from the result.
   *
   * @param ids the ids of Hints to return.
   * @return the matching {@link HintBasicDTO}s.
   */
  @TransactionalRO
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.participatesInLevelsWithHints(#ids)")
  public List<HintBasicDTO> findHintsByIds(List<Long> ids) {
    return hintMapper.mapToBasicDtoList(trainingDefinitionService.findAllHintsByIds(ids));
  }

  /**
   * Retrieves one page of the given training definition's authors from the user-and-group
   * service, identified by their cross-service user reference ids.
   *
   * @param trainingDefinitionId id of the training definition whose authors are retrieved
   * @param pageable pageable parameter with information about pagination.
   * @param givenName restricts the result to authors whose given name matches, no restriction
   *     when null
   * @param familyName restricts the result to authors whose family name matches, no restriction
   *     when null
   * @return the requested page of authors
   * @throws EntityNotFoundException when no training definition with the given id exists
   * @throws cz.cyberrange.platform.training.api.exceptions.MicroserviceApiException when the call
   *     to the user-and-group service fails
   */
  @IsDesignerOrOrganizerOrAdmin
  public PageResultResource<UserRefDTO> getAuthors(
      Long trainingDefinitionId, Pageable pageable, String givenName, String familyName) {
    TrainingDefinition trainingDefinition =
        trainingDefinitionService.findById(trainingDefinitionId);
    return userService.getUsersRefDTOByGivenUserIds(
        trainingDefinition.getAuthors().stream().map(UserRef::getUserRefId).toList(),
        pageable,
        givenName,
        familyName);
  }

  /**
   * Retrieves one page of a training definition's beta testing group organizers from the
   * user-and-group service, identified by their cross-service user reference ids. Returns an
   * empty page, without contacting that service, when the definition has no beta testing group
   * or that group has no organizer.
   *
   * @param trainingDefinitionId id of the training definition whose beta testers are retrieved
   * @param pageable pageable parameter with information about pagination.
   * @return the requested page of beta testers, empty when the definition has none
   * @throws EntityNotFoundException when no training definition with the given id exists
   * @throws cz.cyberrange.platform.training.api.exceptions.MicroserviceApiException when the call
   *     to the user-and-group service fails
   */
  @IsDesignerOrOrganizerOrAdmin
  public PageResultResource<UserRefDTO> getBetaTesters(
      Long trainingDefinitionId, Pageable pageable) {
    TrainingDefinition trainingDefinition =
        trainingDefinitionService.findById(trainingDefinitionId);
    if (trainingDefinition.getBetaTestingGroup() != null
        && !trainingDefinition.getBetaTestingGroup().getOrganizers().isEmpty()) {
      return userService.getUsersRefDTOByGivenUserIds(
          trainingDefinition.getBetaTestingGroup().getOrganizers().stream()
              .map(UserRef::getUserRefId)
              .toList(),
          pageable,
          null,
          null);
    }
    return new PageResultResource<>(
        Collections.emptyList(), new PageResultResource.Pagination(0, 0, 0, 0, 0));
  }

  /**
   * Retrieves one page of the users holding the training designer role from the user-and-group
   * service, excluding the given training definition's current authors.
   *
   * @param trainingDefinitionId id of the training definition whose authors are excluded from the
   *     result list.
   * @param pageable pageable parameter with information about pagination.
   * @param givenName restricts the result to designers whose given name matches, no restriction
   *     when null
   * @param familyName restricts the result to designers whose family name matches, no
   *     restriction when null
   * @return the requested page of designers, excluding the definition's authors
   * @throws EntityNotFoundException when no training definition with the given id exists
   * @throws cz.cyberrange.platform.training.api.exceptions.MicroserviceApiException when the call
   *     to the user-and-group service fails
   */
  @IsDesignerOrOrganizerOrAdmin
  @TransactionalRO
  public PageResultResource<UserRefDTO> getDesignersNotInGivenTrainingDefinition(
      Long trainingDefinitionId, Pageable pageable, String givenName, String familyName) {
    TrainingDefinition trainingDefinition =
        trainingDefinitionService.findById(trainingDefinitionId);
    Set<Long> excludedUsers =
        trainingDefinition.getAuthors().stream()
            .map(UserRef::getUserRefId)
            .collect(Collectors.toSet());
    return userService.getUsersByGivenRoleAndNotWithGivenIds(
        RoleType.ROLE_TRAINING_DESIGNER, excludedUsers, pageable, givenName, familyName);
  }

  /**
   * Adds and removes authors of a training definition in one operation, and records the
   * definition as edited. The logged in user is never removed, even when present in the removal
   * set.
   *
   * @param trainingDefinitionId id of the training definition to be updated
   * @param authorsAddition cross-service user reference ids of the authors to add, no addition
   *     when null or empty
   * @param authorsRemoval cross-service user reference ids of the authors to remove, no removal
   *     when null or empty
   * @throws EntityNotFoundException when no training definition with the given id exists
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isDesignerOfGivenTrainingDefinition(#trainingDefinitionId)")
  @TransactionalWO
  public void editAuthors(
      Long trainingDefinitionId, Set<Long> authorsAddition, Set<Long> authorsRemoval) {
    TrainingDefinition trainingDefinition =
        trainingDefinitionService.findById(trainingDefinitionId);
    Long loggedInUserRefId = securityService.getUserRefIdFromUserAndGroup();
    if (authorsRemoval != null && !authorsRemoval.isEmpty()) {
      authorsRemoval.remove(loggedInUserRefId);
      trainingDefinition.removeAuthorsByUserRefIds(authorsRemoval);
    }
    if (authorsAddition != null && !authorsAddition.isEmpty()) {
      addAuthorsToTrainingDefinition(trainingDefinition, authorsAddition);
    }
    trainingDefinitionService.auditAndSave(trainingDefinition);
  }

  /**
   * Adds the users carrying the given cross-service user reference ids as authors of a training
   * definition, creating a local user reference for any of them not already stored and skipping
   * any already listed as an author.
   *
   * @param trainingDefinition the definition to add authors to
   * @param userRefIds cross-service user reference ids of the users to add
   */
  private void addAuthorsToTrainingDefinition(
      TrainingDefinition trainingDefinition, Set<Long> userRefIds) {
    List<UserRefDTO> authors = getAllUsersRefsByGivenUsersIds(new ArrayList<>(userRefIds));
    Set<Long> actualAuthorsIds =
        trainingDefinition.getAuthors().stream()
            .map(UserRef::getUserRefId)
            .collect(Collectors.toSet());
    for (UserRefDTO author : authors) {
      if (actualAuthorsIds.contains(author.getUserRefId())) {
        continue;
      }
      UserRef userRef = userService.createOrGetUserRef(author.getUserRefId());
      trainingDefinition.addAuthor(userRef);
    }
  }

  /**
   * Retrieves the users carrying the given cross-service user reference ids in full, walking
   * every page the user-and-group service reports.
   *
   * @param participantsRefIds cross-service user reference ids of the users to retrieve
   * @return all matching users, unfiltered by name
   * @throws cz.cyberrange.platform.training.api.exceptions.MicroserviceApiException when any of
   *     the calls to the user-and-group service fails
   */
  private List<UserRefDTO> getAllUsersRefsByGivenUsersIds(List<Long> participantsRefIds) {
    List<UserRefDTO> users = new ArrayList<>();
    PageResultResource<UserRefDTO> usersPageResultResource;
    int page = 0;
    do {
      usersPageResultResource =
          userService.getUsersRefDTOByGivenUserIds(
              participantsRefIds, PageRequest.of(page, 999), null, null);
      users.addAll(usersPageResultResource.getContent());
      page++;
    } while (page < usersPageResultResource.getPagination().getTotalPages());
    return users;
  }
}
