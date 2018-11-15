package cz.muni.ics.kypo.training.facade.impl;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.annotations.TransactionalRO;
import cz.muni.ics.kypo.training.annotations.TransactionalWO;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.BasicLevelInfoDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionCreateDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionUpdateDTO;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.facade.TrainingDefinitionFacade;
import cz.muni.ics.kypo.training.mapping.BeanMapping;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.LevelType;
import cz.muni.ics.kypo.training.service.TrainingDefinitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @author Pavel Šeda
 */
@Service
@Transactional
public class TrainingDefinitionFacadeImpl implements TrainingDefinitionFacade {

    private static final Logger LOG = LoggerFactory.getLogger(TrainingDefinitionFacadeImpl.class);

    private TrainingDefinitionService trainingDefinitionService;
    private BeanMapping beanMapping;


    @Autowired
    public TrainingDefinitionFacadeImpl(TrainingDefinitionService trainingDefinitionService, BeanMapping beanMapping) {
        this.trainingDefinitionService = trainingDefinitionService;
        this.beanMapping = beanMapping;
    }

    @Override
    @TransactionalRO
    public TrainingDefinitionDTO findById(long id) {
        LOG.debug("findById({})", id);
        try {
            Objects.requireNonNull(id);
            TrainingDefinitionDTO trainingDefinitionDTO = beanMapping.mapTo(trainingDefinitionService.findById(id), TrainingDefinitionDTO.class);
            trainingDefinitionDTO.setBasicLevelInfoDTOs(gatherBasicLevelInfo(id));
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
                basicLevelInfoDTO.setLevelType(LevelType.GAME);
            else if (levels.get(i) instanceof AssessmentLevel)
                basicLevelInfoDTO.setLevelType(LevelType.ASSESSMENT);
            else
                basicLevelInfoDTO.setLevelType(LevelType.INFO);
            levelInfoDTOs.add(basicLevelInfoDTO);
        }
        return levelInfoDTOs;
    }

    @Override
    @TransactionalRO
    public PageResultResource<TrainingDefinitionDTO> findAll(Predicate predicate, Pageable pageable) {
        LOG.debug("findAll({},{})", predicate, pageable);
        PageResultResource<TrainingDefinitionDTO> resource = beanMapping.mapToPageResultDTO(trainingDefinitionService.findAll(predicate, pageable), TrainingDefinitionDTO.class);
        for (TrainingDefinitionDTO tD : resource.getContent()){
            tD.setCanBeArchived(checkIfCanBeArchived(tD.getId()));
        }

        return resource;
        //return beanMapping.mapToPageResultDTO(trainingDefinitionService.findAll(predicate, pageable), TrainingDefinitionDTO.class);
    }

    private boolean checkIfCanBeArchived(Long definitionId) {
        List<TrainingInstance> instances = trainingDefinitionService.findAllTrainingInstancesByTrainingDefinitionId(definitionId);
        for (TrainingInstance tI : instances) {
            if (tI.getEndTime().isAfter(LocalDateTime.now())) return false;
        }
        return true;
    }

    @Override
    public PageResultResource<TrainingDefinitionDTO> findAllBySandboxDefinitionId(Long sandboxDefinitionId, Pageable pageable) {
        LOG.debug("findAllBySandboxDefinitionId({}, {})", sandboxDefinitionId, pageable);
        return beanMapping.mapToPageResultDTO(trainingDefinitionService.findAllBySandboxDefinitionId(sandboxDefinitionId, pageable),
                TrainingDefinitionDTO.class);
    }

    @Override
    @TransactionalWO
    public TrainingDefinitionDTO create(TrainingDefinitionCreateDTO trainingDefinition) {
        LOG.debug("create({})", trainingDefinition);
        try {
            Objects.requireNonNull(trainingDefinition);
            TrainingDefinition newTD = trainingDefinitionService.create(beanMapping.mapTo(trainingDefinition, TrainingDefinition.class));
            return beanMapping.mapTo(newTD, TrainingDefinitionDTO.class);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }


    @Override
    @TransactionalWO
    public void update(TrainingDefinitionUpdateDTO trainingDefinition) {
        LOG.debug("update({})", trainingDefinition);
        try {
            Objects.requireNonNull(trainingDefinition);
            trainingDefinitionService.update(beanMapping.mapTo(trainingDefinition, TrainingDefinition.class));
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalWO
    public TrainingDefinitionDTO clone(Long id) {
        LOG.debug("clone({})", id);
        try {
            Objects.requireNonNull(id);
            return beanMapping.mapTo(trainingDefinitionService.clone(id), TrainingDefinitionDTO.class);
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
            trainingDefinitionService.updateGameLevel(definitionId, beanMapping.mapTo(gameLevel, GameLevel.class));
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
            trainingDefinitionService.updateInfoLevel(definitionId, beanMapping.mapTo(infoLevel, InfoLevel.class));
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
            trainingDefinitionService.updateAssessmentLevel(definitionId, beanMapping.mapTo(assessmentLevel, AssessmentLevel.class));
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
            BasicLevelInfoDTO levelInfoDTO = beanMapping.mapTo(newInfoLevel, BasicLevelInfoDTO.class);
            levelInfoDTO.setLevelType(LevelType.INFO);
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
            BasicLevelInfoDTO levelInfoDTO = beanMapping.mapTo(newGameLevel, BasicLevelInfoDTO.class);
            levelInfoDTO.setLevelType(LevelType.GAME);
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
            BasicLevelInfoDTO levelInfoDTO = beanMapping.mapTo(newAssessmentLevel, BasicLevelInfoDTO.class);
            levelInfoDTO.setLevelType(LevelType.ASSESSMENT);
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
            AbstractLevel aL = trainingDefinitionService.findLevelById(levelId);
            AbstractLevelDTO aLDTO;
            if (aL instanceof GameLevel) {
                aLDTO = beanMapping.mapTo(aL, GameLevelDTO.class);
                aLDTO.setLevelType(LevelType.GAME);
            } else if (aL instanceof AssessmentLevel) {
                aLDTO = beanMapping.mapTo(aL, AssessmentLevelDTO.class);
                aLDTO.setLevelType(LevelType.ASSESSMENT);
            } else {
                aLDTO = beanMapping.mapTo(aL, InfoLevelDTO.class);
                aLDTO.setLevelType(LevelType.INFO);
            }
            return aLDTO;
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex.getLocalizedMessage());
        }
    }
}
