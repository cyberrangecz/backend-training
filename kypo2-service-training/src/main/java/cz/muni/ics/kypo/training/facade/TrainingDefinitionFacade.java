package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.annotations.security.IsDesignerOrOrganizerOrAdmin;
import cz.muni.ics.kypo.training.annotations.security.IsDesignerOrAdmin;
import cz.muni.ics.kypo.training.annotations.security.IsOrganizerOrAdmin;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.*;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.*;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import cz.muni.ics.kypo.training.api.enums.RoleType;
import cz.muni.ics.kypo.training.api.enums.TDState;
import cz.muni.ics.kypo.training.exceptions.EntityConflictException;
import cz.muni.ics.kypo.training.exceptions.EntityErrorDetail;
import cz.muni.ics.kypo.training.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.training.mapping.mapstruct.*;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.service.TrainingDefinitionService;
import cz.muni.ics.kypo.training.service.UserService;
import cz.muni.ics.kypo.training.service.SecurityService;
import org.modelmapper.internal.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The type Training definition facade.
 */
@Service
@Transactional
public class TrainingDefinitionFacade {

    private TrainingDefinitionService trainingDefinitionService;
    private TrainingDefinitionMapper trainingDefinitionMapper;
    private GameLevelMapper gameLevelMapper;
    private InfoLevelMapper infoLevelMapper;
    private AssessmentLevelMapper assessmentLevelMapper;
    private BasicLevelInfoMapper basicLevelInfoMapper;
    private UserService userService;
    private SecurityService securityService;

    /**
     * Instantiates a new Training definition facade.
     *
     * @param trainingDefinitionService the training definition service
     * @param trainingDefMapper         the training def mapper
     * @param gameLevelMapper           the game level mapper
     * @param infoLevelMapper           the info level mapper
     * @param assessmentLevelMapper     the assessment level mapper
     * @param basicLevelInfoMapper      the basic level info mapper
     * @param userService               the user service
     * @param securityService           the security service
     */
    @Autowired
    public TrainingDefinitionFacade(TrainingDefinitionService trainingDefinitionService,
                                    TrainingDefinitionMapper trainingDefMapper, GameLevelMapper gameLevelMapper,
                                    InfoLevelMapper infoLevelMapper, AssessmentLevelMapper assessmentLevelMapper,
                                    BasicLevelInfoMapper basicLevelInfoMapper, UserService userService, SecurityService securityService) {
        this.trainingDefinitionService = trainingDefinitionService;
        this.trainingDefinitionMapper = trainingDefMapper;
        this.gameLevelMapper = gameLevelMapper;
        this.infoLevelMapper = infoLevelMapper;
        this.assessmentLevelMapper = assessmentLevelMapper;
        this.basicLevelInfoMapper = basicLevelInfoMapper;
        this.userService = userService;
        this.securityService = securityService;
    }

    /**
     * Finds specific Training Definition by id
     *
     * @param id of a Training Definition that would be returned
     * @return specific {@link TrainingDefinitionByIdDTO}
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_TRAINEE)")
    @TransactionalRO
    public TrainingDefinitionByIdDTO findById(Long id) {
        TrainingDefinition trainingDefinition = trainingDefinitionService.findById(id);
        TrainingDefinitionByIdDTO trainingDefinitionByIdDTO = trainingDefinitionMapper.mapToDTOById(trainingDefinition);
        trainingDefinitionByIdDTO.setLevels(gatherLevels(id));
        if(trainingDefinition.getBetaTestingGroup() != null) {
            trainingDefinitionByIdDTO.setBetaTestingGroupId(trainingDefinition.getBetaTestingGroup().getId());
        }
        return trainingDefinitionByIdDTO;
    }

    private List<BasicLevelInfoDTO> gatherBasicLevelInfo(Long definitionId) {
        List<AbstractLevel> levels = trainingDefinitionService.findAllLevelsFromDefinition(definitionId);
        List<BasicLevelInfoDTO> levelInfoDTOs = new ArrayList<>();

        levels.forEach(level -> {
            BasicLevelInfoDTO basicLevelInfoDTO = new BasicLevelInfoDTO();
            basicLevelInfoDTO.setId(level.getId());
            basicLevelInfoDTO.setTitle(level.getTitle());
            basicLevelInfoDTO.setOrder(level.getOrder());
            if (level instanceof GameLevel)
                basicLevelInfoDTO.setLevelType(LevelType.GAME_LEVEL);
            else if (level instanceof AssessmentLevel)
                basicLevelInfoDTO.setLevelType(LevelType.ASSESSMENT_LEVEL);
            else
                basicLevelInfoDTO.setLevelType(LevelType.INFO_LEVEL);
            levelInfoDTOs.add(basicLevelInfoDTO);
        });
        return levelInfoDTOs;
    }

    private List<AbstractLevelDTO> gatherLevels(Long definitionId) {
        List<AbstractLevel> levels = trainingDefinitionService.findAllLevelsFromDefinition(definitionId);
        List<AbstractLevelDTO> levelDTOS = new ArrayList<>();

        for (AbstractLevel abstractLevel : levels) {
            if (abstractLevel instanceof GameLevel) {
                GameLevelDTO lDTO = gameLevelMapper.mapToDTO((GameLevel) abstractLevel);
                lDTO.setLevelType(LevelType.GAME_LEVEL);
                levelDTOS.add(lDTO);
            } else if (abstractLevel instanceof InfoLevel) {
                InfoLevelDTO lDTO = infoLevelMapper.mapToDTO((InfoLevel) abstractLevel);
                lDTO.setLevelType(LevelType.INFO_LEVEL);
                levelDTOS.add(lDTO);
            } else {
                AssessmentLevelDTO lDTO = assessmentLevelMapper.mapToDTO((AssessmentLevel) abstractLevel);
                lDTO.setLevelType(LevelType.ASSESSMENT_LEVEL);
                levelDTOS.add(lDTO);
            }
        }
        return levelDTOS;
    }

    /**
     * Find all Training Definitions.
     *
     * @param predicate represents a predicate (boolean-valued function) of one argument.
     * @param pageable  pageable parameter with information about pagination.
     * @return page of all {@link TrainingDefinitionDTO}
     */
    @IsDesignerOrAdmin
    @TransactionalRO
    public PageResultResource<TrainingDefinitionDTO> findAll(Predicate predicate, Pageable pageable) {
        PageResultResource<TrainingDefinitionDTO> resource = trainingDefinitionMapper.mapToPageResultResource(trainingDefinitionService.findAll(predicate, pageable));
        for (TrainingDefinitionDTO trainingDefinitionDTO : resource.getContent()) {
            trainingDefinitionDTO.setCanBeArchived(checkIfCanBeArchived(trainingDefinitionDTO.getId()));
        }
        return resource;
    }

    /**
     * Find all Training Definitions.
     *
     * @param state    represents a string if the training definitions should be relased or not.
     * @param pageable pageable parameter with information about pagination.
     * @return page of all {@link TrainingDefinitionInfoDTO} accessible for organizers
     */
    @IsOrganizerOrAdmin
    @TransactionalRO
    public PageResultResource<TrainingDefinitionInfoDTO> findAllForOrganizers(String state, Pageable pageable) {
        return trainingDefinitionMapper.mapToPageResultResourceInfoDTO(trainingDefinitionService.findAllForOrganizers(state, pageable));
    }

    /**
     * Creates new training definition
     *
     * @param trainingDefinition to be created
     * @return DTO of created definition, {@link TrainingDefinitionCreateDTO}
     */
    @IsDesignerOrAdmin
    @TransactionalWO
    public TrainingDefinitionByIdDTO create(TrainingDefinitionCreateDTO trainingDefinition) {
        TrainingDefinition newTrainingDefinition = trainingDefinitionMapper.mapCreateToEntity(trainingDefinition);
        if (trainingDefinition.getBetaTestingGroup() != null) {
            addOrganizersToTrainingDefinition(newTrainingDefinition, trainingDefinition.getBetaTestingGroup().getOrganizersRefIds());
        }
        TrainingDefinition createdTrainingDefinition = trainingDefinitionService.create(newTrainingDefinition);
        TrainingDefinitionByIdDTO trainingDefinitionByIdDTO = trainingDefinitionMapper.mapToDTOById(createdTrainingDefinition);
        if(createdTrainingDefinition.getBetaTestingGroup() != null) {
            trainingDefinitionByIdDTO.setBetaTestingGroupId(createdTrainingDefinition.getBetaTestingGroup().getId());
        }
        return trainingDefinitionByIdDTO;
    }

    /**
     * Updates training definition
     *
     * @param trainingDefinitionUpdateDTO to be updated
     */
    @IsDesignerOrAdmin
    @TransactionalWO
    public void update(TrainingDefinitionUpdateDTO trainingDefinitionUpdateDTO) {
        TrainingDefinition mappedTrainingDefinition = trainingDefinitionMapper.mapUpdateToEntity(trainingDefinitionUpdateDTO);
        TrainingDefinition trainingDefinition = trainingDefinitionService.findById(trainingDefinitionUpdateDTO.getId());
        mappedTrainingDefinition.setAuthors(new HashSet<>(trainingDefinition.getAuthors()));
        if (trainingDefinitionUpdateDTO.getBetaTestingGroup() != null) {
            addOrganizersToTrainingDefinition(mappedTrainingDefinition, trainingDefinitionUpdateDTO.getBetaTestingGroup().getOrganizersRefIds());
            if (trainingDefinition.getBetaTestingGroup() != null) {
                trainingDefinition.getBetaTestingGroup().setId(trainingDefinition.getBetaTestingGroup().getId());
            }
        } else if (trainingDefinition.getBetaTestingGroup() != null) {
            throw new EntityConflictException(new EntityErrorDetail(BetaTestingGroup.class, "id", Long.class, trainingDefinition.getBetaTestingGroup().getId(),
                    "Cannot delete beta testing group. You only can remove organizers from group."));
        }
        trainingDefinitionService.update(mappedTrainingDefinition);
    }

    private void addOrganizersToTrainingDefinition(TrainingDefinition trainingDefinition, Set<Long> userRefIds) {
        trainingDefinition.getBetaTestingGroup().setOrganizers(new HashSet<>());
        PageResultResource<UserRefDTO> organizers = userService.getUsersRefDTOByGivenUserIds(userRefIds, PageRequest.of(0,999), null, null);
        for (UserRefDTO organizer : organizers.getContent()) {
            try {
                trainingDefinition.getBetaTestingGroup().addOrganizer(userService.getUserByUserRefId(organizer.getUserRefId()));
            } catch (EntityNotFoundException ex) {
                trainingDefinition.getBetaTestingGroup().addOrganizer(userService.createUserRef(createUserRefFromDTO(organizer)));
            }
        }
    }

    private UserRef createUserRefFromDTO(UserRefDTO userToBeCreated) {
        UserRef userRef = new UserRef();
        userRef.setUserRefId(userToBeCreated.getUserRefId());
        return userRef;
    }

    /**
     * Clones Training Definition by id
     *
     * @param id    of definition to be cloned
     * @param title the title of cloned definition
     * @return DTO of cloned definition, {@link TrainingDefinitionByIdDTO}
     */
    @IsDesignerOrAdmin
    @TransactionalWO
    public TrainingDefinitionByIdDTO clone(Long id, String title) {
        TrainingDefinitionByIdDTO clonedDefinition = trainingDefinitionMapper.mapToDTOById(trainingDefinitionService.clone(id, title));
        clonedDefinition.setLevels(gatherLevels(clonedDefinition.getId()));
        return clonedDefinition;
    }

    /**
     * Swaps between levels. Swap basically means swapping the order attribute between these two levels.
     *
     * @param definitionId  - Id of definition containing levels, this training definition is updating its last edited column.
     * @param swapLevelFrom - Id of a first level to be swapped.
     * @param swapLevelTo   - Id of a second level to be swapped.
     * @return the list of {@link BasicLevelInfoDTO} about all levels from given definition
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    @TransactionalWO
    public List<BasicLevelInfoDTO> swapLevels(Long definitionId, Long swapLevelFrom, Long swapLevelTo) {
        trainingDefinitionService.swapLevels(definitionId, swapLevelFrom, swapLevelTo);
        return gatherBasicLevelInfo(definitionId);
    }

    /**
     * Move level to the different position and modify orders of levels between moved level and new position.
     *
     * @param definitionId     - Id of definition containing levels, this training definition is updating its last edited column.
     * @param levelIdToBeMoved - id of the level to be moved to the new position
     * @param newPosition      - position where level will be moved
     * @return the list of {@link BasicLevelInfoDTO} about all levels from given definition
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    @TransactionalWO
    public List<BasicLevelInfoDTO> moveLevel(Long definitionId, Long levelIdToBeMoved, Integer newPosition) {
        trainingDefinitionService.moveLevel(definitionId, levelIdToBeMoved, newPosition);
        return gatherBasicLevelInfo(definitionId);
    }

    /**
     * Deletes specific training instance based on id
     *
     * @param id of definition to be deleted
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#id)")
    @TransactionalWO
    public void delete(Long id) {
        trainingDefinitionService.delete(id);
    }

    /**
     * deletes specific level by id
     *
     * @param definitionId - id of definition containing level to be deleted
     * @param levelId      - id of level to be deleted
     * @return the list of {@link BasicLevelInfoDTO} about all levels from given definition
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    @TransactionalWO
    public List<BasicLevelInfoDTO> deleteOneLevel(Long definitionId, Long levelId) {
        trainingDefinitionService.deleteOneLevel(definitionId, levelId);
        return gatherBasicLevelInfo(definitionId);
    }

    /**
     * updates game level from training definition
     *
     * @param definitionId - id of training definition containing level to be updated
     * @param gameLevel    to be updated
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    @TransactionalWO
    public void updateGameLevel(Long definitionId, GameLevelUpdateDTO gameLevel) {
        GameLevel gameLevelToUpdate = gameLevelMapper.mapUpdateToEntity(gameLevel);
        for (Hint hint : gameLevelToUpdate.getHints())
            hint.setGameLevel(gameLevelToUpdate);
        trainingDefinitionService.updateGameLevel(definitionId, gameLevelToUpdate);
    }

    /**
     * updates info level from training definition
     *
     * @param definitionId - id of training definition containing level to be updated
     * @param infoLevel    to be updated
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    @TransactionalWO
    public void updateInfoLevel(Long definitionId, InfoLevelUpdateDTO infoLevel) {
        trainingDefinitionService.updateInfoLevel(definitionId, infoLevelMapper.mapUpdateToEntity(infoLevel));
    }

    /**
     * updates assessment level from training definition
     *
     * @param definitionId    - id of training definition containing level to be updated
     * @param assessmentLevel to be updated
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    @TransactionalWO
    public void updateAssessmentLevel(Long definitionId, AssessmentLevelUpdateDTO assessmentLevel) {
        trainingDefinitionService.updateAssessmentLevel(definitionId, assessmentLevelMapper.mapUpdateToEntity(assessmentLevel));
    }

    /**
     * creates new info level in training definition
     *
     * @param definitionId - id of definition in which level will be created
     * @return {@link BasicLevelInfoDTO} of new info level
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    @TransactionalWO
    public BasicLevelInfoDTO createInfoLevel(Long definitionId) {
        InfoLevel newInfoLevel = trainingDefinitionService.createInfoLevel(definitionId);
        BasicLevelInfoDTO levelInfoDTO = basicLevelInfoMapper.mapTo(newInfoLevel);
        levelInfoDTO.setLevelType(LevelType.INFO_LEVEL);
        return levelInfoDTO;
    }

    /**
     * creates new game level in training definition
     *
     * @param definitionId - id of definition in which level will be created
     * @return {@link BasicLevelInfoDTO} of new game level
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    @TransactionalWO
    public BasicLevelInfoDTO createGameLevel(Long definitionId) {
        GameLevel newGameLevel = trainingDefinitionService.createGameLevel(definitionId);
        BasicLevelInfoDTO levelInfoDTO = basicLevelInfoMapper.mapTo(newGameLevel);
        levelInfoDTO.setLevelType(LevelType.GAME_LEVEL);
        return levelInfoDTO;
    }

    /**
     * creates new assessment level in training definition
     *
     * @param definitionId - id of definition in which level will be created
     * @return {@link BasicLevelInfoDTO} of new assessment level
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    @TransactionalWO
    public BasicLevelInfoDTO createAssessmentLevel(Long definitionId) {
        AssessmentLevel newAssessmentLevel = trainingDefinitionService.createAssessmentLevel(definitionId);
        BasicLevelInfoDTO levelInfoDTO = basicLevelInfoMapper.mapTo(newAssessmentLevel);
        levelInfoDTO.setLevelType(LevelType.ASSESSMENT_LEVEL);
        return levelInfoDTO;
    }

    /**
     * Finds specific level by id
     *
     * @param levelId - id of wanted level
     * @return wanted {@link AbstractLevelDTO}
     */
    @IsDesignerOrAdmin
    @TransactionalRO
    public AbstractLevelDTO findLevelById(Long levelId) {
        AbstractLevel abstractLevel = trainingDefinitionService.findLevelById(levelId);
        AbstractLevelDTO abstractLevelDTO;
        if (abstractLevel instanceof GameLevel) {
            abstractLevelDTO = gameLevelMapper.mapToDTO((GameLevel) abstractLevel);
            abstractLevelDTO.setLevelType(LevelType.GAME_LEVEL);
        } else if (abstractLevel instanceof AssessmentLevel) {
            abstractLevelDTO = assessmentLevelMapper.mapToDTO((AssessmentLevel) abstractLevel);
            abstractLevelDTO.setLevelType(LevelType.ASSESSMENT_LEVEL);
        } else {
            abstractLevelDTO = infoLevelMapper.mapToDTO((InfoLevel) abstractLevel);
            abstractLevelDTO.setLevelType(LevelType.INFO_LEVEL);
        }
        return abstractLevelDTO;
    }

    /**
     * Get users with given role
     *
     * @param roleType   the wanted role type
     * @param pageable   pageable parameter with information about pagination.
     * @param givenName  the given name
     * @param familyName the family name
     * @return list of users {@link UserRefDTO}
     */
    @IsDesignerOrAdmin
    @TransactionalRO
    public PageResultResource<UserRefDTO> getUsersWithGivenRole(RoleType roleType, Pageable pageable, String givenName, String familyName) {
        return userService.getUsersByGivenRole(roleType, pageable, givenName, familyName);
    }

    /**
     * Switch state of definition to unreleased
     *
     * @param definitionId - id of training definition
     * @param state        - new state of TD
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    public void switchState(Long definitionId, TDState state) {
        trainingDefinitionService.switchState(definitionId, state);
    }

    private boolean checkIfCanBeArchived(Long definitionId) {
        List<TrainingInstance> instances = trainingDefinitionService.findAllTrainingInstancesByTrainingDefinitionId(definitionId);
        for (TrainingInstance trainingInstance : instances) {
            if (trainingInstance.getEndTime().isAfter(LocalDateTime.now(Clock.systemUTC()))) return false;
        }
        return true;
    }

    /**
     * Retrieve all authors for given training definition.
     *
     * @param trainingDefinitionId id of the training definition for which to get the authors
     * @param pageable             pageable parameter with information about pagination.
     * @param givenName            optional parameter used for filtration
     * @param familyName           optional parameter used for filtration
     * @return returns all authors in given training definition.
     */
    @IsDesignerOrOrganizerOrAdmin
    public PageResultResource<UserRefDTO> getAuthors(Long trainingDefinitionId, Pageable pageable, String givenName, String familyName) {
        TrainingDefinition trainingDefinition = trainingDefinitionService.findById(trainingDefinitionId);
        return userService.getUsersRefDTOByGivenUserIds(trainingDefinition.getAuthors().stream().map(UserRef::getUserRefId).collect(Collectors.toSet()), pageable, givenName, familyName);
    }

    /**
     * Retrieve all beta testers for given training definition.
     *
     * @param trainingDefinitionId id of the training definition for which to get the beta testers
     * @param pageable             pageable parameter with information about pagination.
     * @return returns all beta testers in given training definition.
     */
    @IsDesignerOrOrganizerOrAdmin
    public PageResultResource<UserRefDTO> getBetaTesters(Long trainingDefinitionId, Pageable pageable) {
        TrainingDefinition trainingDefinition = trainingDefinitionService.findById(trainingDefinitionId);
        if(trainingDefinition.getBetaTestingGroup() != null && !trainingDefinition.getBetaTestingGroup().getOrganizers().isEmpty()) {
            return userService.getUsersRefDTOByGivenUserIds(trainingDefinition.getBetaTestingGroup().getOrganizers().stream()
                    .map(UserRef::getUserRefId)
                    .collect(Collectors.toSet()), pageable, null, null);
        }
        return new PageResultResource<>(Collections.emptyList(), new PageResultResource.Pagination(0,0,0,0,0));
    }

    /**
     * Retrieve all designers not in the given training definition.
     *
     * @param trainingDefinitionId id of the training definition which users should be excluded from the result list.
     * @param pageable             pageable parameter with information about pagination.
     * @param givenName            optional parameter used for filtration
     * @param familyName           optional parameter used for filtration
     * @return returns all designers not in the given training definition.
     */
    @IsDesignerOrOrganizerOrAdmin
    @TransactionalRO
    public PageResultResource<UserRefDTO> getDesignersNotInGivenTrainingDefinition(Long trainingDefinitionId, Pageable pageable, String givenName, String familyName) {
        TrainingDefinition trainingDefinition = trainingDefinitionService.findById(trainingDefinitionId);
        Set<Long> excludedUsers = trainingDefinition.getAuthors().stream()
                .map(UserRef::getUserRefId)
                .collect(Collectors.toSet());
        return userService.getUsersByGivenRoleAndNotWithGivenIds(RoleType.ROLE_TRAINING_DESIGNER, excludedUsers, pageable, givenName, familyName);
    }

    /**
     * Concurrently add authors to the given training definition and remove authors from the training definition.
     *
     * @param trainingDefinitionId if of the training definition to be updated
     * @param authorsAddition      ids of the authors to be added to the training definition
     * @param authorsRemoval       ids of the authors to be removed from the training definition.
     */
    @IsDesignerOrAdmin
    @TransactionalWO
    public void editAuthors(Long trainingDefinitionId, Set<Long> authorsAddition, Set<Long> authorsRemoval) {
        TrainingDefinition trainingDefinition = trainingDefinitionService.findById(trainingDefinitionId);
        Long loggedInUserRefId = securityService.getUserRefIdFromUserAndGroup();
        if(authorsRemoval != null && !authorsRemoval.isEmpty()) {
            authorsRemoval.remove(loggedInUserRefId);
            trainingDefinition.removeAuthorsByUserRefIds(authorsRemoval);
        }
        if(authorsAddition != null && !authorsAddition.isEmpty()) {
            addAuthorsToTrainingDefinition(trainingDefinition, authorsAddition);
        }
    }

    private void addAuthorsToTrainingDefinition(TrainingDefinition trainingDefinition, Set<Long> userRefIds) {
        PageResultResource<UserRefDTO> authors;
        int page = 0;
        do {
            authors = userService.getUsersRefDTOByGivenUserIds(userRefIds, PageRequest.of(page,999), null, null);
            Set<Long> actualAuthorsIds = trainingDefinition.getAuthors().stream().map(UserRef::getUserRefId).collect(Collectors.toSet());
            page++;
            for (UserRefDTO author : authors.getContent()) {
                if(actualAuthorsIds.contains(author.getUserRefId())) {
                    continue;
                }
                try {
                    trainingDefinition.addAuthor(userService.getUserByUserRefId(author.getUserRefId()));
                } catch (EntityNotFoundException ex) {
                    trainingDefinition.addAuthor(userService.createUserRef(createUserRefFromDTO(author)));
                }
            }

        } while (authors.getPagination().getTotalPages() != page);

    }
}
