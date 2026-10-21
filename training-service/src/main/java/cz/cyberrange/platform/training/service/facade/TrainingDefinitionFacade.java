package cz.cyberrange.platform.training.service.facade;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.training.api.dto.AbstractLevelBasicDTO;
import cz.cyberrange.platform.training.api.dto.AbstractLevelDTO;
import cz.cyberrange.platform.training.api.dto.UserRefDTO;
import cz.cyberrange.platform.training.api.dto.hint.HintBasicDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.AbstractTrainingDefinitionDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionBasicDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionCreateDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionInfoDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionMitreTechniquesDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionUpdateDTO;
import cz.cyberrange.platform.training.api.dto.trainingdefinition.TrainingDefinitionWithLevelsDTO;
import cz.cyberrange.platform.training.api.enums.RoleType;
import cz.cyberrange.platform.training.api.enums.TDState;
import cz.cyberrange.platform.training.api.exceptions.BadRequestException;
import cz.cyberrange.platform.training.api.exceptions.EntityConflictException;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.api.responses.PageResultResource;
import cz.cyberrange.platform.training.persistence.model.AbstractLevel;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.repository.TrainingDefinitionRepository;
import cz.cyberrange.platform.training.service.annotations.security.IsDesignerOrAdmin;
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
import cz.cyberrange.platform.training.service.services.UserNameFilter;
import cz.cyberrange.platform.training.service.services.UserService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The type Training definition facade */
@Service
@Transactional
public class TrainingDefinitionFacade {

  /**
   * Admits an administrator, and a designer or organizer who may see the training definition named
   * by the {@code trainingDefinitionId} argument.
   */
  private static final String VISIBLE_DEFINITION_TO_DESIGNER_OR_ORGANIZER =
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + " or (hasAnyAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_DESIGNER,"
          + " T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ORGANIZER)"
          + " and @securityService.isVisibleTrainingDefinition(#trainingDefinitionId))";

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
   * the flag telling whether it can be archived. Open to an administrator, an author of the
   * definition, and an organizer of any instance created from it.
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
   * Sets the archiving flag and the instance count on the given definition DTO according to whether
   * the definition it describes can currently be archived and how many training instances it has.
   *
   * @param trainingDefinitionDTO the DTO to complete, carrying the id of the definition to decide
   *     for
   * @return the same instance, with the flag and the count set
   */
  private <T extends TrainingDefinitionDTO> T addArchivingInfo(T trainingDefinitionDTO) {
    trainingDefinitionDTO.setCanBeArchived(
        trainingDefinitionService.canBeArchived(trainingDefinitionDTO.getId()));
    trainingDefinitionService.completeInstanceCounts(List.of(trainingDefinitionDTO));
    return trainingDefinitionDTO;
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
   * definition; any other caller sees only definitions where they are an author.
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
   * according to whether any of its training instances ends in the future, and its instance count.
   *
   * @param trainingDefinitionPage the page of entities to map
   * @return the mapped page, with the archiving flag and the instance count set on every DTO
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
    trainingDefinitionService.completeInstanceCounts(resource.getContent());
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
   * an administrator sees every unreleased definition; a caller holding the designer role sees the
   * unreleased definitions where they are an author; any other caller sees none.
   *
   * @param state whether released or unreleased definitions are wanted
   * @param pageable pageable parameter with information about pagination.
   * @return page of matching {@link TrainingDefinitionInfoDTO}
   * @throws BadRequestException when {@code state} is neither released nor unreleased
   */
  @IsOrganizerOrAdmin
  @TransactionalRO
  public PageResultResource<TrainingDefinitionInfoDTO> findAllForOrganizers(
      TDState state, Pageable pageable) {
    PageResultResource<TrainingDefinitionInfoDTO> page =
        findAllForOrganizersWithoutCounts(state, pageable);
    trainingDefinitionService.completeInstanceCounts(page.getContent());
    return page;
  }

  private PageResultResource<TrainingDefinitionInfoDTO> findAllForOrganizersWithoutCounts(
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
      } else if (securityService.hasRole(RoleTypeSecurity.ROLE_TRAINING_DESIGNER)) {
        return trainingDefinitionMapper.mapToPageResultResourceInfoDTO(
            trainingDefinitionService.findAllForDesignersUnreleased(loggedInUserId, pageable));
      } else {
        return trainingDefinitionMapper.mapToPageResultResourceInfoDTO(Page.empty(pageable));
      }
    }
    throw new BadRequestException(
        "It is required to provide training definition state that is RELEASED or UNRELEASED");
  }

  /**
   * Creates a new training definition together with its levels, assigning the requesting user as an
   * author.
   *
   * @param trainingDefinition the definition to create, its levels in presentation order
   * @return the created definition together with its levels, {@link
   *     TrainingDefinitionWithLevelsDTO}
   * @throws BadRequestException when a level carries an id or fails a content check
   * @throws cz.cyberrange.platform.training.api.exceptions.UnprocessableEntityException when a
   *     training level's hint penalties exceed its maximal score
   */
  @IsDesignerOrAdmin
  @TransactionalWO
  public TrainingDefinitionWithLevelsDTO create(TrainingDefinitionCreateDTO trainingDefinition) {
    TrainingDefinition newTrainingDefinition =
        trainingDefinitionMapper.mapCreateToEntity(trainingDefinition);
    TrainingDefinition createdTrainingDefinition =
        trainingDefinitionService.create(
            newTrainingDefinition,
            trainingDefinition.getLevels().stream()
                .map(levelMapper::mapCreateToEntity)
                .collect(Collectors.toList()));
    return mapToDTOWithoutInstances(createdTrainingDefinition);
  }

  /**
   * Replaces the content and the whole level list of a training definition in one transaction,
   * keeping its state, creation time and existing authors, which the requesting user joins. A level
   * without an id is created, a level carrying the id of one of the definition's levels replaces
   * that level, and every stored level the list leaves out is deleted. Reachable only by an
   * administrator or by a designer of the definition.
   *
   * @param definitionId id of the definition to update
   * @param trainingDefinitionUpdateDTO the new content and levels, with the version it was made on
   * @return the updated definition together with its levels, its new version, its instance count
   *     and the flag telling whether it can be archived, {@link TrainingDefinitionWithLevelsDTO}
   * @throws EntityNotFoundException when no training definition with the given id exists, or a
   *     level id names no level of the definition
   * @throws EntityConflictException when the definition is not {@link TDState#UNRELEASED}, already
   *     has a training instance, or carries another version
   * @throws BadRequestException when a level id repeats, a level would change its type, or a level
   *     fails a content check
   * @throws cz.cyberrange.platform.training.api.exceptions.UnprocessableEntityException when a
   *     training level's hint penalties exceed its maximal score
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
  @TransactionalWO
  public TrainingDefinitionWithLevelsDTO update(
      Long definitionId, TrainingDefinitionUpdateDTO trainingDefinitionUpdateDTO) {
    TrainingDefinition updatedTrainingDefinition =
        trainingDefinitionService.update(
            definitionId,
            trainingDefinitionUpdateDTO.getVersion(),
            trainingDefinitionMapper.mapUpdateToEntity(trainingDefinitionUpdateDTO),
            trainingDefinitionUpdateDTO.getLevels().stream()
                .map(levelMapper::mapUpdateToEntity)
                .collect(Collectors.toList()));
    return addArchivingInfo(
        trainingDefinitionMapper.mapToDTOWithLevels(
            updatedTrainingDefinition, gatherLevels(definitionId)));
  }

  /**
   * Maps a stored training definition that has no training instance to its DTO with its levels,
   * marked as one that can be archived and counting no instance.
   *
   * @param trainingDefinition the stored definition to map
   * @return the definition together with its levels in presentation order
   */
  private TrainingDefinitionWithLevelsDTO mapToDTOWithoutInstances(
      TrainingDefinition trainingDefinition) {
    TrainingDefinitionWithLevelsDTO trainingDefinitionDTO =
        trainingDefinitionMapper.mapToDTOWithLevels(
            trainingDefinition, gatherLevels(trainingDefinition.getId()));
    trainingDefinitionDTO.setCanBeArchived(true);
    return trainingDefinitionDTO;
  }

  /**
   * Clones a training definition together with its levels, assigning the requesting user as an
   * author of the clone. Reachable only by an administrator or by a designer of the definition
   * being cloned.
   *
   * @param id id of the definition to clone
   * @param title the title to give the clone
   * @return the cloned definition together with its levels, {@link TrainingDefinitionWithLevelsDTO}
   * @throws EntityNotFoundException when no training definition with the given id exists
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isDesignerOfGivenTrainingDefinition(#id)")
  @TransactionalWO
  public TrainingDefinitionWithLevelsDTO clone(Long id, String title) {
    return mapToDTOWithoutInstances(trainingDefinitionService.clone(id, title));
  }

  /**
   * Deletes a training definition together with all of its levels.
   *
   * @param id id of the definition to delete
   * @throws EntityNotFoundException when no training definition with the given id exists
   * @throws EntityConflictException when the definition is {@link TDState#RELEASED} or already has
   *     a training instance
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + "or @securityService.isDesignerOfGivenTrainingDefinition(#id)")
  @TransactionalWO
  public void delete(Long id) {
    trainingDefinitionService.delete(id);
  }

  /**
   * Finds a level by id. Reachable by an administrator, and by a designer when the level's training
   * definition is not unreleased or the designer authors it.
   *
   * @param levelId - id of wanted level
   * @return wanted {@link AbstractLevelDTO}
   * @throws EntityNotFoundException when no level with the given id exists
   */
  @PreAuthorize(
      "hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)"
          + " or (hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_DESIGNER)"
          + " and @securityService.isVisibleLevel(#levelId))")
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
   * @param familyName restricts the result to users whose family name matches, no restriction when
   *     null
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
   * state the definition already holds is a no-op. Switching a released definition back to
   * unreleased is refused while it has a training instance.
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
   * Finds Training Definitions by their ids, together with the basic detail of their levels. An id
   * matching no training definition is silently omitted from the result.
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
    List<TrainingDefinitionBasicDTO> basicDTOs =
        definitions.stream()
            .map(
                definition ->
                    trainingDefinitionMapper.mapToBasicDTO(
                        definition,
                        levelsByDefinitionId.getOrDefault(definition.getId(), List.of())))
            .collect(Collectors.toList());
    trainingDefinitionService.completeInstanceCounts(basicDTOs);
    return basicDTOs;
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
   * Retrieves one page of the given training definition's authors from the user-and-group service,
   * identified by their cross-service user reference ids. Reachable by an administrator, and by a
   * designer or organizer when the definition is not unreleased or the caller authors it.
   *
   * @param trainingDefinitionId id of the training definition whose authors are retrieved
   * @param pageable pageable parameter with information about pagination.
   * @param nameFilter restricts the result to authors matching it by name
   * @return the requested page of authors
   * @throws EntityNotFoundException when no training definition with the given id exists
   * @throws cz.cyberrange.platform.training.api.exceptions.MicroserviceApiException when the call
   *     to the user-and-group service fails
   */
  @PreAuthorize(VISIBLE_DEFINITION_TO_DESIGNER_OR_ORGANIZER)
  @TransactionalRO
  public PageResultResource<UserRefDTO> getAuthors(
      Long trainingDefinitionId, Pageable pageable, UserNameFilter nameFilter) {
    TrainingDefinition trainingDefinition =
        trainingDefinitionService.findById(trainingDefinitionId);
    return userService.getUsersRefDTOByGivenUserIds(
        trainingDefinition.getAuthors().stream().map(UserRef::getUserRefId).toList(),
        pageable,
        nameFilter);
  }

  /**
   * Retrieves one page of the users holding the training designer role from the user-and-group
   * service, excluding the given training definition's current authors. Reachable by an
   * administrator, and by a designer or organizer when the definition is not unreleased or the
   * caller authors it.
   *
   * @param trainingDefinitionId id of the training definition whose authors are excluded from the
   *     result list.
   * @param pageable pageable parameter with information about pagination.
   * @param nameFilter restricts the result to designers matching it by name
   * @return the requested page of designers, excluding the definition's authors
   * @throws EntityNotFoundException when no training definition with the given id exists
   * @throws cz.cyberrange.platform.training.api.exceptions.MicroserviceApiException when the call
   *     to the user-and-group service fails
   */
  @PreAuthorize(VISIBLE_DEFINITION_TO_DESIGNER_OR_ORGANIZER)
  @TransactionalRO
  public PageResultResource<UserRefDTO> getDesignersNotInGivenTrainingDefinition(
      Long trainingDefinitionId, Pageable pageable, UserNameFilter nameFilter) {
    TrainingDefinition trainingDefinition =
        trainingDefinitionService.findById(trainingDefinitionId);
    Set<Long> excludedUsers =
        trainingDefinition.getAuthors().stream()
            .map(UserRef::getUserRefId)
            .collect(Collectors.toSet());
    return userService.getUsersByGivenRoleAndNotWithGivenIds(
        RoleType.ROLE_TRAINING_DESIGNER, excludedUsers, pageable, nameFilter);
  }

  /**
   * Adds and removes authors of a training definition in one operation, and records the definition
   * as edited. The logged in user is never removed, even when present in the removal set.
   *
   * @param trainingDefinitionId id of the training definition to be updated
   * @param authorsAddition cross-service user reference ids of the authors to add, no addition when
   *     null or empty
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
   * definition, creating a local user reference for any of them not already stored and skipping any
   * already listed as an author.
   *
   * @param trainingDefinition the definition to add authors to
   * @param userRefIds cross-service user reference ids of the users to add
   */
  private void addAuthorsToTrainingDefinition(
      TrainingDefinition trainingDefinition, Set<Long> userRefIds) {
    List<UserRefDTO> authors =
        userService.getUsersRefDTOByGivenUserIds(new ArrayList<>(userRefIds));
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
}
