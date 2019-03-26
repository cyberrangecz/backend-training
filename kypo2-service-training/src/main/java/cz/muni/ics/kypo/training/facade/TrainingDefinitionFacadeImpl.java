package cz.muni.ics.kypo.training.facade;

import com.google.gson.JsonObject;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.BasicLevelInfoDTO;
import cz.muni.ics.kypo.training.api.dto.UserInfoDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionCreateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionUpdateDTO;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import cz.muni.ics.kypo.training.api.enums.RoleType;
import cz.muni.ics.kypo.training.exceptions.ErrorCode;
import cz.muni.ics.kypo.training.exceptions.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.mapping.mapstruct.*;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.service.TrainingDefinitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Pavel Šeda
 */
@Service
@Transactional
public class TrainingDefinitionFacadeImpl implements TrainingDefinitionFacade {

    private static final Logger LOG = LoggerFactory.getLogger(TrainingDefinitionFacadeImpl.class);

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
    public TrainingDefinitionDTO findById(Long id) {
        LOG.debug("findById({})", id);
        try {
            Objects.requireNonNull(id);
            TrainingDefinitionDTO trainingDefinitionDTO = trainingDefinitionMapper.mapToDTO(trainingDefinitionService.findById(id));
            trainingDefinitionDTO.setLevels(gatherLevels(id));
            return trainingDefinitionDTO;
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    private Set<BasicLevelInfoDTO> gatherBasicLevelInfo(Long definitionId) {
        List<AbstractLevel> levels = trainingDefinitionService.findAllLevelsFromDefinition(definitionId);
        Set<BasicLevelInfoDTO> levelInfoDTOs = new HashSet<>();

        for (int i = 0; i < levels.size(); i++) {
            BasicLevelInfoDTO basicLevelInfoDTO = new BasicLevelInfoDTO();
            basicLevelInfoDTO.setId(levels.get(i).getId());
            basicLevelInfoDTO.setOrder(i);
            basicLevelInfoDTO.setTitle(levels.get(i).getTitle());
            if (levels.get(i) instanceof GameLevel)
                basicLevelInfoDTO.setLevelType(LevelType.GAME_LEVEL);
            else if (levels.get(i) instanceof AssessmentLevel)
                basicLevelInfoDTO.setLevelType(LevelType.ASSESSMENT_LEVEL);
            else
                basicLevelInfoDTO.setLevelType(LevelType.INFO_LEVEL);
            levelInfoDTOs.add(basicLevelInfoDTO);
        }
        return levelInfoDTOs;
    }

    private Set<AbstractLevelDTO> gatherLevels(Long definitionId) {
        List<AbstractLevel> levels = trainingDefinitionService.findAllLevelsFromDefinition(definitionId);
        Set<AbstractLevelDTO> levelDTOS = new HashSet<>();

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
        LOG.debug("findAllTrainingDefinitions({},{})", predicate, pageable);
        PageResultResource<TrainingDefinitionDTO> resource = trainingDefinitionMapper.mapToPageResultResource(trainingDefinitionService.findAll(predicate, pageable));
        for (TrainingDefinitionDTO trainingDefinitionDTO : resource.getContent()) {
            trainingDefinitionDTO.setCanBeArchived(checkIfCanBeArchived(trainingDefinitionDTO.getId()));
        }
        return resource;
    }

    private boolean checkIfCanBeArchived(Long definitionId) {
        List<TrainingInstance> instances = trainingDefinitionService.findAllTrainingInstancesByTrainingDefinitionId(definitionId);
        for (TrainingInstance trainingInstance : instances) {
            if (trainingInstance.getEndTime().isAfter(LocalDateTime.now())) return false;
        }
        return true;
    }

    @Override
    public PageResultResource<TrainingDefinitionDTO> findAllBySandboxDefinitionId(Long sandboxDefinitionId, Pageable pageable) {
        LOG.debug("findAllBySandboxDefinitionId({}, {})", sandboxDefinitionId, pageable);
        return trainingDefinitionMapper.mapToPageResultResource(trainingDefinitionService.findAllBySandboxDefinitionId(sandboxDefinitionId, pageable));
    }

    @Override
    @TransactionalWO
    public TrainingDefinitionDTO create(TrainingDefinitionCreateDTO trainingDefinition) {
        LOG.debug("create({})", trainingDefinition);
        try {
            Objects.requireNonNull(trainingDefinition);
            TrainingDefinition newTrainingDefinition = trainingDefinitionMapper.mapCreateToEntity(trainingDefinition);
            if(trainingDefinition.getBetaTestingGroup() != null) {
                addOrganizersToTrainingDefinition(newTrainingDefinition, trainingDefinition.getBetaTestingGroup().getOrganizers());
            }
            addAuthorsToTrainingDefinition(newTrainingDefinition, trainingDefinition.getAuthors());
            return trainingDefinitionMapper.mapToDTO(trainingDefinitionService.create(newTrainingDefinition));
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }


    @Override
    @TransactionalWO
    public void update(TrainingDefinitionUpdateDTO trainingDefinitionUpdateDTO) {
        LOG.debug("update({})", trainingDefinitionUpdateDTO);
        try {
            Objects.requireNonNull(trainingDefinitionUpdateDTO);
            TrainingDefinition mappedTrainingDefinition = trainingDefinitionMapper.mapUpdateToEntity(trainingDefinitionUpdateDTO);
            TrainingDefinition trainingDefinition = trainingDefinitionService.findById(trainingDefinitionUpdateDTO.getId());
            if(trainingDefinition.getBetaTestingGroup() != null) {
                if(trainingDefinitionUpdateDTO.getBetaTestingGroup() != null) {
                    trainingDefinition.getBetaTestingGroup().setId(trainingDefinition.getBetaTestingGroup().getId());
                    addOrganizersToTrainingDefinition(mappedTrainingDefinition, trainingDefinitionUpdateDTO.getBetaTestingGroup().getOrganizers());
                } else {
                    throw new FacadeLayerException(new ServiceLayerException("Cannot delete beta testing group. You only can remove organizers from group.", ErrorCode.RESOURCE_CONFLICT));
                }
            }
            addAuthorsToTrainingDefinition(mappedTrainingDefinition, trainingDefinitionUpdateDTO.getAuthors());
            trainingDefinitionService.update(mappedTrainingDefinition);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    private void addAuthorsToTrainingDefinition(TrainingDefinition trainingDefinition, Set<UserInfoDTO> authors) {
        trainingDefinition.setAuthors(new HashSet<>());
        for (UserInfoDTO author: authors) {
            try {
                trainingDefinition.addAuthor(trainingDefinitionService.findUserRefByLogin(author.getLogin()));
            } catch (ServiceLayerException ex) {
                UserRef userRef = new UserRef();
                userRef.setUserRefLogin(author.getLogin());
                userRef.setUserRefFullName(author.getFullName());
                trainingDefinition.addAuthor(trainingDefinitionService.createUserRef(userRef));
            }
        }
    }

    private void addOrganizersToTrainingDefinition(TrainingDefinition trainingDefinition, Set<UserInfoDTO> organizers) {
        trainingDefinition.getBetaTestingGroup().setOrganizers(new HashSet<>());
        for (UserInfoDTO organizer : organizers) {
            try {
                trainingDefinition.getBetaTestingGroup().addOrganizer(trainingDefinitionService.findUserRefByLogin(organizer.getLogin()));
            } catch (ServiceLayerException ex) {
                UserRef userRef = new UserRef();
                userRef.setUserRefLogin(organizer.getLogin());
                userRef.setUserRefFullName(organizer.getFullName());
                trainingDefinition.getBetaTestingGroup().addOrganizer(trainingDefinitionService.createUserRef(userRef));
            }
        }
    }

    @Override
    @TransactionalWO
    public TrainingDefinitionDTO clone(Long id) {
        LOG.debug("clone({})", id);
        try {
            Objects.requireNonNull(id);
            return trainingDefinitionMapper.mapToDTO(trainingDefinitionService.clone(id));
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalWO
    public Set<BasicLevelInfoDTO> swapLeft(Long definitionId, Long levelId) {
        LOG.debug("swapLeft({},{})", definitionId, levelId);
        try {
            Objects.requireNonNull(definitionId);
            Objects.requireNonNull(levelId);
            trainingDefinitionService.swapLeft(definitionId, levelId);
            return gatherBasicLevelInfo(definitionId);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }


    @Override
    @TransactionalWO
    public Set<BasicLevelInfoDTO> swapRight(Long definitionId, Long levelId) {
        LOG.debug("swapRight({},{})", definitionId, levelId);
        try {
            Objects.requireNonNull(definitionId);
            Objects.requireNonNull(levelId);
            trainingDefinitionService.swapRight(definitionId, levelId);
            return gatherBasicLevelInfo(definitionId);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }


    @Override
    @TransactionalWO
    public void delete(Long id) {
        LOG.debug("delete({})", id);
        try {
            Objects.requireNonNull(id);
            trainingDefinitionService.delete(id);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalWO
    public Set<BasicLevelInfoDTO> deleteOneLevel(Long definitionId, Long levelId) {
        LOG.debug("deleteOneLevel({}, {})", definitionId, levelId);
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
        LOG.debug("updateGameLevel({}, {})", definitionId, gameLevel);
        try {
            Objects.requireNonNull(gameLevel);
            Objects.requireNonNull(definitionId);
            trainingDefinitionService.updateGameLevel(definitionId, gameLevelMapper.mapUpdateToEntity(gameLevel));
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalWO
    public void updateInfoLevel(Long definitionId, InfoLevelUpdateDTO infoLevel) {
        LOG.debug("updateInfoLevel({}, {})", definitionId, infoLevel);
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
        LOG.debug("updateAssessmentLevel({}, {})", definitionId, assessmentLevel);
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
        LOG.debug("createInfoLevel({})", definitionId);
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
        LOG.debug("createGameLevel({})", definitionId);
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
        LOG.debug("assessmentInfoLevel({})", definitionId);
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
        LOG.debug("findLevelById({})", levelId);
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

    private String getFullNameOfLoggedInUser() {
        OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
        JsonObject credentials = (JsonObject) authentication.getUserAuthentication().getCredentials();
        return credentials.get("name").getAsString();
    }


}
