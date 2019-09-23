package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.training.api.RestResponses.PageResultResource;
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
import org.modelmapper.internal.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;

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

    @Autowired
    public TrainingDefinitionFacadeImpl(TrainingDefinitionService trainingDefinitionService,
                                        TrainingDefinitionMapper trainingDefMapper, GameLevelMapper gameLevelMapper,
                                        InfoLevelMapper infoLevelMapper, AssessmentLevelMapper assessmentLevelMapper,
                                        BasicLevelInfoMapper basicLevelInfoMapper) {
        this.trainingDefinitionService = trainingDefinitionService;
        this.trainingDefinitionMapper = trainingDefMapper;
        this.gameLevelMapper = gameLevelMapper;
        this.infoLevelMapper = infoLevelMapper;
        this.assessmentLevelMapper = assessmentLevelMapper;
        this.basicLevelInfoMapper = basicLevelInfoMapper;
    }

    @Override
    @TransactionalRO
    public TrainingDefinitionByIdDTO findById(Long id) {
        try {
            Objects.requireNonNull(id);
            TrainingDefinitionByIdDTO trainingDefinitionByIdDTO = trainingDefinitionMapper.mapToDTOById(trainingDefinitionService.findById(id));
            trainingDefinitionByIdDTO.setLevels(gatherLevels(id));
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
            addAuthorsToTrainingDefinition(newTrainingDefinition, trainingDefinition.getAuthorsRefIds());
            return trainingDefinitionMapper.mapToDTOById(trainingDefinitionService.create(newTrainingDefinition));
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
            addAuthorsToTrainingDefinition(mappedTrainingDefinition, trainingDefinitionUpdateDTO.getAuthorsRefIds());
            trainingDefinitionService.update(mappedTrainingDefinition);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    private void addAuthorsToTrainingDefinition(TrainingDefinition trainingDefinition, Set<Long> userRefIds) {
        trainingDefinition.setAuthors(new HashSet<>());
        Set<UserInfoDTO> authors = trainingDefinitionService.getUsersWithGivenUserRefIds(userRefIds);
        for (UserInfoDTO author : authors) {
            try {
                trainingDefinition.addAuthor(trainingDefinitionService.findUserByRefId(author.getUserRefId()));
            } catch (ServiceLayerException ex) {
                trainingDefinition.addAuthor(trainingDefinitionService.createUserRef(createUserRef(author)));
            }
        }
    }

    private void addOrganizersToTrainingDefinition(TrainingDefinition trainingDefinition, Set<Long> userRefIds) {
        trainingDefinition.getBetaTestingGroup().setOrganizers(new HashSet<>());
        Set<UserInfoDTO> organizers = trainingDefinitionService.getUsersWithGivenUserRefIds(userRefIds);
        for (UserInfoDTO organizer : organizers) {
            try {
                trainingDefinition.getBetaTestingGroup().addOrganizer(trainingDefinitionService.findUserByRefId(organizer.getUserRefId()));
            } catch (ServiceLayerException ex) {
                trainingDefinition.getBetaTestingGroup().addOrganizer(trainingDefinitionService.createUserRef(createUserRef(organizer)));
            }
        }
    }

    private UserRef createUserRef(UserInfoDTO userToBeCreated) {
        UserRef userRef = new UserRef();
        userRef.setUserRefId(userToBeCreated.getUserRefId());
        userRef.setIss(userToBeCreated.getIss());
        userRef.setUserRefFamilyName(userToBeCreated.getFamilyName());
        userRef.setUserRefGivenName(userToBeCreated.getGivenName());
        userRef.setUserRefFullName(userToBeCreated.getFullName());
        userRef.setUserRefLogin(userToBeCreated.getLogin());
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
    public List<UserInfoDTO> getUsersWithGivenRole(RoleType roleType, Pageable pageable) {
        try {
            return trainingDefinitionService.getUsersWithGivenRole(roleType, pageable);
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

}
