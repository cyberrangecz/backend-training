package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
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
import cz.muni.ics.kypo.training.exceptions.ErrorCode;
import cz.muni.ics.kypo.training.exceptions.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.mapping.mapstruct.*;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.service.TrainingDefinitionService;
import cz.muni.ics.kypo.training.service.UserService;
import cz.muni.ics.kypo.training.service.impl.SecurityService;
import org.modelmapper.internal.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Pavel Šeda
 * @author Boris Jadus
 */
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
    @TransactionalRO
    public TrainingDefinitionByIdDTO findById(Long id) {
        try {
            Objects.requireNonNull(id);
            TrainingDefinition trainingDefinition = trainingDefinitionService.findById(id);
            TrainingDefinitionByIdDTO trainingDefinitionByIdDTO = trainingDefinitionMapper.mapToDTOById(trainingDefinition);
            trainingDefinitionByIdDTO.setLevels(gatherLevels(id));
            if(trainingDefinition.getBetaTestingGroup() != null) {
                trainingDefinitionByIdDTO.setBetaTestingGroupId(trainingDefinition.getBetaTestingGroup().getId());
            }
            return trainingDefinitionByIdDTO;
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
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
    @TransactionalRO
    public PageResultResource<TrainingDefinitionDTO> findAll(Predicate predicate, Pageable pageable) {
        PageResultResource<TrainingDefinitionDTO> resource = trainingDefinitionMapper.mapToPageResultResource(trainingDefinitionService.findAll(predicate, pageable));
        for (TrainingDefinitionDTO trainingDefinitionDTO : resource.getContent()) {
            trainingDefinitionDTO.setCanBeArchived(checkIfCanBeArchived(trainingDefinitionDTO.getId()));
        }
        return resource;
    }

    @Override
    public PageResultResource<TrainingDefinitionInfoDTO> findAllForOrganizers(Predicate predicate, Pageable pageable) {
        return trainingDefinitionMapper.mapToPageResultResourceInfoDTO(trainingDefinitionService.findAllForOrganizers(predicate, pageable));
    }

    @Override
    public PageResultResource<TrainingDefinitionInfoDTO> findAllBySandboxDefinitionId(Long sandboxDefinitionId, Pageable pageable) {
        Page<TrainingDefinition> trainingDefinitionsPage = trainingDefinitionService.findAllBySandboxDefinitionId(sandboxDefinitionId, pageable);
        List<TrainingDefinitionInfoDTO> trainingDefinitionInfoDTOS = new ArrayList<>();
        for (TrainingDefinition trainingDefinition : trainingDefinitionsPage.getContent()) {
            TrainingDefinitionInfoDTO trainingDefinitionInfoDTO = trainingDefinitionMapper.mapToInfoDTO(trainingDefinition);
            trainingDefinitionInfoDTOS.add(trainingDefinitionInfoDTO);
        }
        return new PageResultResource<>(trainingDefinitionInfoDTOS, trainingDefinitionMapper.createPagination(trainingDefinitionsPage));
    }

    @Override
    @TransactionalWO
    public TrainingDefinitionByIdDTO create(TrainingDefinitionCreateDTO trainingDefinition) {
        try {
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
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }


    @Override
    @TransactionalWO
    public void update(TrainingDefinitionUpdateDTO trainingDefinitionUpdateDTO) {
        try {
            Objects.requireNonNull(trainingDefinitionUpdateDTO);
            TrainingDefinition mappedTrainingDefinition = trainingDefinitionMapper.mapUpdateToEntity(trainingDefinitionUpdateDTO);
            TrainingDefinition trainingDefinition = trainingDefinitionService.findById(trainingDefinitionUpdateDTO.getId());
            if (trainingDefinitionUpdateDTO.getBetaTestingGroup() != null) {
                addOrganizersToTrainingDefinition(mappedTrainingDefinition, trainingDefinitionUpdateDTO.getBetaTestingGroup().getOrganizersRefIds());
                if (trainingDefinition.getBetaTestingGroup() != null) {
                    trainingDefinition.getBetaTestingGroup().setId(trainingDefinition.getBetaTestingGroup().getId());
                }
            } else if (trainingDefinition.getBetaTestingGroup() != null) {
                throw new FacadeLayerException(new ServiceLayerException("Cannot delete beta testing group. You only can remove organizers from group.", ErrorCode.RESOURCE_CONFLICT));
            }
            trainingDefinitionService.update(mappedTrainingDefinition);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    private void addOrganizersToTrainingDefinition(TrainingDefinition trainingDefinition, Set<Long> userRefIds) {
        trainingDefinition.getBetaTestingGroup().setOrganizers(new HashSet<>());
        PageResultResource<UserRefDTO> organizers = userService.getUsersRefDTOByGivenUserIds(userRefIds, PageRequest.of(0,999), null, null);
        for (UserRefDTO organizer : organizers.getContent()) {
            try {
                trainingDefinition.getBetaTestingGroup().addOrganizer(userService.getUserByUserRefId(organizer.getUserRefId()));
            } catch (ServiceLayerException ex) {
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
    @TransactionalWO
    public TrainingDefinitionByIdDTO clone(Long id, String title) {
        try {
            Assert.notNull(id, "Given id of training definition to be cloned");

            TrainingDefinitionByIdDTO clonedDefinition = trainingDefinitionMapper.mapToDTOById(trainingDefinitionService.clone(id, title));
            clonedDefinition.setLevels(gatherLevels(clonedDefinition.getId()));
            return clonedDefinition;
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalWO
    public List<BasicLevelInfoDTO> swapLevels(Long definitionId, Long swapLevelFrom, Long swapLevelTo) {
        try {
            Objects.requireNonNull(definitionId);
            Objects.requireNonNull(swapLevelFrom);
            Objects.requireNonNull(swapLevelTo);
            trainingDefinitionService.swapLevels(definitionId, swapLevelFrom, swapLevelTo);
            return gatherBasicLevelInfo(definitionId);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalWO
    public List<BasicLevelInfoDTO> moveLevel(Long definitionId, Long levelIdToBeMoved, Integer newPosition) {
        try {
            Objects.requireNonNull(definitionId);
            Objects.requireNonNull(levelIdToBeMoved);
            Objects.requireNonNull(newPosition);
            trainingDefinitionService.moveLevel(definitionId, levelIdToBeMoved, newPosition);
            return gatherBasicLevelInfo(definitionId);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalWO
    public void delete(Long id) {
        try {
            Objects.requireNonNull(id);
            trainingDefinitionService.delete(id);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalWO
    public List<BasicLevelInfoDTO> deleteOneLevel(Long definitionId, Long levelId) {
        try {
            Objects.requireNonNull(definitionId);
            Objects.requireNonNull(levelId);
            trainingDefinitionService.deleteOneLevel(definitionId, levelId);
            return gatherBasicLevelInfo(definitionId);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalWO
    public void updateGameLevel(Long definitionId, GameLevelUpdateDTO gameLevel) {
        try {
            Objects.requireNonNull(gameLevel);
            Objects.requireNonNull(definitionId);
            GameLevel gameLevelToUpdate = gameLevelMapper.mapUpdateToEntity(gameLevel);
            //Connecting game level with hints
            for (Hint hint : gameLevelToUpdate.getHints())
                hint.setGameLevel(gameLevelToUpdate);
            trainingDefinitionService.updateGameLevel(definitionId, gameLevelToUpdate);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalWO
    public void updateInfoLevel(Long definitionId, InfoLevelUpdateDTO infoLevel) {
        try {
            Objects.requireNonNull(infoLevel);
            Objects.requireNonNull(definitionId);
            trainingDefinitionService.updateInfoLevel(definitionId, infoLevelMapper.mapUpdateToEntity(infoLevel));
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalWO
    public void updateAssessmentLevel(Long definitionId, AssessmentLevelUpdateDTO assessmentLevel) {
        try {
            Objects.requireNonNull(assessmentLevel);
            Objects.requireNonNull(definitionId);
            trainingDefinitionService.updateAssessmentLevel(definitionId, assessmentLevelMapper.mapUpdateToEntity(assessmentLevel));
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalWO
    public BasicLevelInfoDTO createInfoLevel(Long definitionId) {
        try {
            Objects.requireNonNull(definitionId);
            InfoLevel newInfoLevel = trainingDefinitionService.createInfoLevel(definitionId);
            BasicLevelInfoDTO levelInfoDTO = basicLevelInfoMapper.mapTo(newInfoLevel);
            levelInfoDTO.setLevelType(LevelType.INFO_LEVEL);
            return levelInfoDTO;
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }


    @Override
    @TransactionalWO
    public BasicLevelInfoDTO createGameLevel(Long definitionId) {
        try {
            Objects.requireNonNull(definitionId);
            GameLevel newGameLevel = trainingDefinitionService.createGameLevel(definitionId);
            BasicLevelInfoDTO levelInfoDTO = basicLevelInfoMapper.mapTo(newGameLevel);
            levelInfoDTO.setLevelType(LevelType.GAME_LEVEL);
            return levelInfoDTO;
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }


    @Override
    @TransactionalWO
    public BasicLevelInfoDTO createAssessmentLevel(Long definitionId) {
        try {
            Objects.requireNonNull(definitionId);
            AssessmentLevel newAssessmentLevel = trainingDefinitionService.createAssessmentLevel(definitionId);
            BasicLevelInfoDTO levelInfoDTO = basicLevelInfoMapper.mapTo(newAssessmentLevel);
            levelInfoDTO.setLevelType(LevelType.ASSESSMENT_LEVEL);
            return levelInfoDTO;
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalRO
    public AbstractLevelDTO findLevelById(Long levelId) {
        try {
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
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex.getLocalizedMessage());
        }
    }

    @Override
    @TransactionalRO
    public PageResultResource<UserRefDTO> getUsersWithGivenRole(RoleType roleType, Pageable pageable, String givenName, String familyName) {
        try {
            return userService.getUsersByGivenRole(roleType, pageable, givenName, familyName);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    public void switchState(Long definitionId, TDState state) {
        try {
            Objects.requireNonNull(definitionId);
            trainingDefinitionService.switchState(definitionId, state);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    private boolean checkIfCanBeArchived(Long definitionId) {
        List<TrainingInstance> instances = trainingDefinitionService.findAllTrainingInstancesByTrainingDefinitionId(definitionId);
        for (TrainingInstance trainingInstance : instances) {
            if (trainingInstance.getEndTime().isAfter(LocalDateTime.now(Clock.systemUTC()))) return false;
        }
        return true;
    }

    @Override
    public PageResultResource<UserRefDTO> getAuthors(Long trainingDefinitionId, Pageable pageable, String givenName, String familyName) {
        try {
            TrainingDefinition trainingDefinition = trainingDefinitionService.findById(trainingDefinitionId);
            return userService.getUsersRefDTOByGivenUserIds(trainingDefinition.getAuthors().stream().map(UserRef::getUserRefId).collect(Collectors.toSet()), pageable, givenName, familyName);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    public PageResultResource<UserRefDTO> getBetaTesters(Long trainingDefinitionId, Pageable pageable) {
        try {
            TrainingDefinition trainingDefinition = trainingDefinitionService.findById(trainingDefinitionId);
            if(trainingDefinition.getBetaTestingGroup() != null && !trainingDefinition.getBetaTestingGroup().getOrganizers().isEmpty()) {
                return userService.getUsersRefDTOByGivenUserIds(trainingDefinition.getBetaTestingGroup().getOrganizers().stream().map(UserRef::getUserRefId).collect(Collectors.toSet()), pageable, null, null);
            }
            return new PageResultResource<>(Collections.emptyList(), new PageResultResource.Pagination(0,0,0,0,0));
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalRO
    public PageResultResource<UserRefDTO> getDesignersNotInGivenTrainingDefinition(Long trainingDefinitionId, Pageable pageable, String givenName, String familyName) {
        try {
            TrainingDefinition trainingDefinition = trainingDefinitionService.findById(trainingDefinitionId);
            Set<Long> excludedUsers = trainingDefinition.getAuthors().stream().map(UserRef::getUserRefId).collect(Collectors.toSet());
            return userService.getUsersByGivenRoleAndNotWithGivenIds(RoleType.ROLE_TRAINING_DESIGNER, excludedUsers, pageable, givenName, familyName);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalWO
    public void editAuthors(Long trainingDefinitionId, Set<Long> authorsAddition, Set<Long> authorsRemoval) throws FacadeLayerException {
        try {

        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
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
                } catch (ServiceLayerException ex) {
                    trainingDefinition.addAuthor(userService.createUserRef(createUserRefFromDTO(author)));
                }
            }

        } while (authors.getPagination().getTotalPages() != page);

    }
}
