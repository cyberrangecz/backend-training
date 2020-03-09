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
import cz.muni.ics.kypo.training.service.impl.SecurityService;
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

@Service
@Transactional
public class TrainingDefinitionFacadeImpl implements TrainingDefinitionFacade {

    private TrainingDefinitionService trainingDefinitionService;
    private TrainingDefinitionMapper trainingDefinitionMapper;
    private GameLevelMapper gameLevelMapper;
    private InfoLevelMapper infoLevelMapper;
    private AssessmentLevelMapper assessmentLevelMapper;
    private BasicLevelInfoMapper basicLevelInfoMapper;
    private UserService userService;
    private SecurityService securityService;

    @Autowired
    public TrainingDefinitionFacadeImpl(TrainingDefinitionService trainingDefinitionService,
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

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_TRAINEE)")
    @TransactionalRO
    public TrainingDefinitionByIdDTO findById(Long id) {
        Objects.requireNonNull(id);
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

    @Override
    @IsDesignerOrAdmin
    @TransactionalRO
    public PageResultResource<TrainingDefinitionDTO> findAll(Predicate predicate, Pageable pageable) {
        PageResultResource<TrainingDefinitionDTO> resource = trainingDefinitionMapper.mapToPageResultResource(trainingDefinitionService.findAll(predicate, pageable));
        for (TrainingDefinitionDTO trainingDefinitionDTO : resource.getContent()) {
            trainingDefinitionDTO.setCanBeArchived(checkIfCanBeArchived(trainingDefinitionDTO.getId()));
        }
        return resource;
    }

    @Override
    @IsOrganizerOrAdmin
    @TransactionalRO
    public PageResultResource<TrainingDefinitionInfoDTO> findAllForOrganizers(String state, Pageable pageable) {
        return trainingDefinitionMapper.mapToPageResultResourceInfoDTO(trainingDefinitionService.findAllForOrganizers(state, pageable));
    }

    @Override
    @IsDesignerOrAdmin
    @TransactionalWO
    public TrainingDefinitionByIdDTO create(TrainingDefinitionCreateDTO trainingDefinition) {
        Objects.requireNonNull(trainingDefinition);
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


    @Override
    @IsDesignerOrAdmin
    @TransactionalWO
    public void update(TrainingDefinitionUpdateDTO trainingDefinitionUpdateDTO) {
        Objects.requireNonNull(trainingDefinitionUpdateDTO);
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

    @Override
    @IsDesignerOrAdmin
    @TransactionalWO
    public TrainingDefinitionByIdDTO clone(Long id, String title) {
        Assert.notNull(id, "Given id of training definition to be cloned");
        TrainingDefinitionByIdDTO clonedDefinition = trainingDefinitionMapper.mapToDTOById(trainingDefinitionService.clone(id, title));
        clonedDefinition.setLevels(gatherLevels(clonedDefinition.getId()));
        return clonedDefinition;
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    @TransactionalWO
    public List<BasicLevelInfoDTO> swapLevels(Long definitionId, Long swapLevelFrom, Long swapLevelTo) {
        Objects.requireNonNull(definitionId);
        Objects.requireNonNull(swapLevelFrom);
        Objects.requireNonNull(swapLevelTo);
        trainingDefinitionService.swapLevels(definitionId, swapLevelFrom, swapLevelTo);
        return gatherBasicLevelInfo(definitionId);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    @TransactionalWO
    public List<BasicLevelInfoDTO> moveLevel(Long definitionId, Long levelIdToBeMoved, Integer newPosition) {
        Objects.requireNonNull(definitionId);
        Objects.requireNonNull(levelIdToBeMoved);
        Objects.requireNonNull(newPosition);
        trainingDefinitionService.moveLevel(definitionId, levelIdToBeMoved, newPosition);
        return gatherBasicLevelInfo(definitionId);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    @TransactionalWO
    public void delete(Long id) {
        Objects.requireNonNull(id);
        trainingDefinitionService.delete(id);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    @TransactionalWO
    public List<BasicLevelInfoDTO> deleteOneLevel(Long definitionId, Long levelId) {
        Objects.requireNonNull(definitionId);
        Objects.requireNonNull(levelId);
        trainingDefinitionService.deleteOneLevel(definitionId, levelId);
        return gatherBasicLevelInfo(definitionId);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    @TransactionalWO
    public void updateGameLevel(Long definitionId, GameLevelUpdateDTO gameLevel) {
        Objects.requireNonNull(gameLevel);
        Objects.requireNonNull(definitionId);
        GameLevel gameLevelToUpdate = gameLevelMapper.mapUpdateToEntity(gameLevel);
        for (Hint hint : gameLevelToUpdate.getHints())
            hint.setGameLevel(gameLevelToUpdate);
        trainingDefinitionService.updateGameLevel(definitionId, gameLevelToUpdate);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    @TransactionalWO
    public void updateInfoLevel(Long definitionId, InfoLevelUpdateDTO infoLevel) {
        Objects.requireNonNull(infoLevel);
        Objects.requireNonNull(definitionId);
        trainingDefinitionService.updateInfoLevel(definitionId, infoLevelMapper.mapUpdateToEntity(infoLevel));
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    @TransactionalWO
    public void updateAssessmentLevel(Long definitionId, AssessmentLevelUpdateDTO assessmentLevel) {
        Objects.requireNonNull(assessmentLevel);
        Objects.requireNonNull(definitionId);
        trainingDefinitionService.updateAssessmentLevel(definitionId, assessmentLevelMapper.mapUpdateToEntity(assessmentLevel));
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    @TransactionalWO
    public BasicLevelInfoDTO createInfoLevel(Long definitionId) {
        Objects.requireNonNull(definitionId);
        InfoLevel newInfoLevel = trainingDefinitionService.createInfoLevel(definitionId);
        BasicLevelInfoDTO levelInfoDTO = basicLevelInfoMapper.mapTo(newInfoLevel);
        levelInfoDTO.setLevelType(LevelType.INFO_LEVEL);
        return levelInfoDTO;
    }


    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    @TransactionalWO
    public BasicLevelInfoDTO createGameLevel(Long definitionId) {
        Objects.requireNonNull(definitionId);
        GameLevel newGameLevel = trainingDefinitionService.createGameLevel(definitionId);
        BasicLevelInfoDTO levelInfoDTO = basicLevelInfoMapper.mapTo(newGameLevel);
        levelInfoDTO.setLevelType(LevelType.GAME_LEVEL);
        return levelInfoDTO;
    }


    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    @TransactionalWO
    public BasicLevelInfoDTO createAssessmentLevel(Long definitionId) {
        Objects.requireNonNull(definitionId);
        AssessmentLevel newAssessmentLevel = trainingDefinitionService.createAssessmentLevel(definitionId);
        BasicLevelInfoDTO levelInfoDTO = basicLevelInfoMapper.mapTo(newAssessmentLevel);
        levelInfoDTO.setLevelType(LevelType.ASSESSMENT_LEVEL);
        return levelInfoDTO;
    }

    @Override
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

    @Override
    @IsDesignerOrAdmin
    @TransactionalRO
    public PageResultResource<UserRefDTO> getUsersWithGivenRole(RoleType roleType, Pageable pageable, String givenName, String familyName) {
        return userService.getUsersByGivenRole(roleType, pageable, givenName, familyName);
    }

    @Override
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isDesignerOfGivenTrainingDefinition(#definitionId)")
    public void switchState(Long definitionId, TDState state) {
        Objects.requireNonNull(definitionId);
        trainingDefinitionService.switchState(definitionId, state);
    }

    private boolean checkIfCanBeArchived(Long definitionId) {
        List<TrainingInstance> instances = trainingDefinitionService.findAllTrainingInstancesByTrainingDefinitionId(definitionId);
        for (TrainingInstance trainingInstance : instances) {
            if (trainingInstance.getEndTime().isAfter(LocalDateTime.now(Clock.systemUTC()))) return false;
        }
        return true;
    }

    @Override
    @IsDesignerOrOrganizerOrAdmin
    public PageResultResource<UserRefDTO> getAuthors(Long trainingDefinitionId, Pageable pageable, String givenName, String familyName) {
        TrainingDefinition trainingDefinition = trainingDefinitionService.findById(trainingDefinitionId);
        return userService.getUsersRefDTOByGivenUserIds(trainingDefinition.getAuthors().stream().map(UserRef::getUserRefId).collect(Collectors.toSet()), pageable, givenName, familyName);
    }

    @Override
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

    @Override
    @IsDesignerOrOrganizerOrAdmin
    @TransactionalRO
    public PageResultResource<UserRefDTO> getDesignersNotInGivenTrainingDefinition(Long trainingDefinitionId, Pageable pageable, String givenName, String familyName) {
        TrainingDefinition trainingDefinition = trainingDefinitionService.findById(trainingDefinitionId);
        Set<Long> excludedUsers = trainingDefinition.getAuthors().stream()
                .map(UserRef::getUserRefId)
                .collect(Collectors.toSet());
        return userService.getUsersByGivenRoleAndNotWithGivenIds(RoleType.ROLE_TRAINING_DESIGNER, excludedUsers, pageable, givenName, familyName);
    }

    @Override
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
