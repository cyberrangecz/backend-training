package cz.muni.ics.kypo.training.facade.impl;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.annotations.TransactionalRO;
import cz.muni.ics.kypo.training.annotations.TransactionalWO;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.IsCorrectFlagDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelDTO;
import cz.muni.ics.kypo.training.api.dto.hint.HintDTO;
import cz.muni.ics.kypo.training.api.dto.BasicLevelInfoDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelDTO;
import cz.muni.ics.kypo.training.api.dto.run.AccessTrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.run.AccessedTrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.api.enums.Actions;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.facade.TrainingRunFacade;
import cz.muni.ics.kypo.training.mapping.BeanMapping;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.LevelType;
import cz.muni.ics.kypo.training.service.TrainingRunService;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dominik Pilar (445537)
 */
@Service
@Transactional
public class TrainingRunFacadeImpl implements TrainingRunFacade {

    private static final Logger LOG = LoggerFactory.getLogger(TrainingRunFacadeImpl.class);

    private TrainingRunService trainingRunService;
    private BeanMapping beanMapping;

    @Autowired
    public TrainingRunFacadeImpl(TrainingRunService trainingRunService, BeanMapping beanMapping) {
        this.trainingRunService = trainingRunService;
        this.beanMapping = beanMapping;
    }

    @Override
    @TransactionalRO
    public TrainingRunDTO findById(Long id) {
        LOG.debug("findById({})", id);
        try {
            TrainingRun trainingRun = trainingRunService.findById(id);
            return beanMapping.mapTo(trainingRun, TrainingRunDTO.class);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalRO
    public PageResultResource<TrainingRunDTO> findAll(Predicate predicate, Pageable pageable) {
        LOG.debug("findAll({},{})", predicate, pageable);
        return beanMapping.mapToPageResultDTO(trainingRunService.findAll(predicate, pageable), TrainingRunDTO.class);
    }

    @Override
    @TransactionalRO
    public PageResultResource<AccessedTrainingRunDTO> findAllAccessedTrainingRuns(Pageable pageable) {
        LOG.debug("findAllAccessedTrainingRuns()");
        Page<TrainingRun> trainingRuns = trainingRunService.findAllByParticipantRefLogin(pageable);
        return convertToAccessedRunDTO(trainingRuns);

    }

    @Override
    @TransactionalWO
    public AccessTrainingRunDTO resumeTrainingRun(Long trainingRunId) {
        LOG.debug("resumeTrainingRun({})", trainingRunId);
        AccessTrainingRunDTO accessTrainingRunDTO = new AccessTrainingRunDTO();
        try {
            AbstractLevel abstractLevel = trainingRunService.resumeTrainingRun(trainingRunId);
            accessTrainingRunDTO.setAbstractLevelDTO(getCorrectAbstractLevelDTO(abstractLevel));

            accessTrainingRunDTO.setInfoAboutLevels(getInfoAboutLevels(abstractLevel.getId()));
            return accessTrainingRunDTO;
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalWO
    public AccessTrainingRunDTO accessTrainingRun(String password) {
        LOG.debug("accessTrainingRun({})", password);
        AccessTrainingRunDTO accessTrainingRunDTO = new AccessTrainingRunDTO();
        try {
            AbstractLevel abstractLevel = trainingRunService.accessTrainingRun(password);
            accessTrainingRunDTO.setAbstractLevelDTO(getCorrectAbstractLevelDTO(abstractLevel));
            accessTrainingRunDTO.setInfoAboutLevels(getInfoAboutLevels(abstractLevel.getId()));
            return accessTrainingRunDTO;
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    private List<BasicLevelInfoDTO> getInfoAboutLevels(Long firstLevelId) {
        List<BasicLevelInfoDTO> infoAboutLevels = new ArrayList<>();
        List<AbstractLevel> levels = trainingRunService.getLevels(firstLevelId);
        for (AbstractLevel al : levels) {
            if (al instanceof AssessmentLevel) {
                infoAboutLevels.add(new BasicLevelInfoDTO(al.getId(), al.getTitle(), LevelType.ASSESSMENT, levels.indexOf(al)));
            } else if (al instanceof GameLevel) {
                infoAboutLevels.add(new BasicLevelInfoDTO(al.getId(), al.getTitle(), LevelType.GAME, levels.indexOf(al)));
            } else {
                infoAboutLevels.add(new BasicLevelInfoDTO(al.getId(), al.getTitle(), LevelType.INFO, levels.indexOf(al)));
            }
        }
        return infoAboutLevels;
    }

    @Override
    @TransactionalRO
    public PageResultResource<TrainingRunDTO> findAllByTrainingDefinitionAndParticipant(Long trainingDefinitionId, Pageable pageable) {
        LOG.debug("findAllByTrainingDefinitionAndParticipant({})", trainingDefinitionId);
        Page<TrainingRun> trainingRuns = trainingRunService.findAllByTrainingDefinitionAndParticipant(trainingDefinitionId, pageable);
        return beanMapping.mapToPageResultDTO(trainingRuns, TrainingRunDTO.class);
    }

    @Override
    @TransactionalRO
    public PageResultResource<TrainingRunDTO> findAllByTrainingDefinition(Long trainingDefinitionId, Pageable pageable) {
        LOG.debug("findAllByTrainingDefinition({})", trainingDefinitionId);
        Page<TrainingRun> trainingRuns = trainingRunService.findAllByTrainingDefinition(trainingDefinitionId, pageable);
        return beanMapping.mapToPageResultDTO(trainingRuns, TrainingRunDTO.class);
    }

    @Override
    @TransactionalWO
    public AbstractLevelDTO getNextLevel(Long trainingRunId) {
        LOG.debug("getNextLevel({})", trainingRunId);
        AbstractLevel aL;
        try {
            aL = trainingRunService.getNextLevel(trainingRunId);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
        AbstractLevelDTO abstractLevelDTO;
        if (aL instanceof GameLevel) {
            abstractLevelDTO = beanMapping.mapTo(aL, GameLevelDTO.class);
        } else if (aL instanceof AssessmentLevel) {
            abstractLevelDTO = beanMapping.mapTo(aL, AssessmentLevelDTO.class);
        } else {
            abstractLevelDTO = beanMapping.mapTo(aL, InfoLevelDTO.class);
        }
        return abstractLevelDTO;
    }

    @Override
    @TransactionalWO
    public String getSolution(Long trainingRunId) {
        LOG.debug("getSolution({})", trainingRunId);
        try {
            return trainingRunService.getSolution(trainingRunId);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalWO
    public HintDTO getHint(Long trainingRunId, Long hintId) {
        LOG.debug("getHint({},{})", trainingRunId, hintId);
        try {
            return beanMapping.mapTo(trainingRunService.getHint(trainingRunId, hintId), HintDTO.class);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalWO
    public IsCorrectFlagDTO isCorrectFlag(Long trainingRunId, String flag) {
        LOG.debug("isCorrectFlag({},{})", trainingRunId, flag);
        IsCorrectFlagDTO correctFlagDTO = new IsCorrectFlagDTO();
        try {
            correctFlagDTO.setCorrect(trainingRunService.isCorrectFlag(trainingRunId, flag));
            correctFlagDTO.setRemainingAttempts(trainingRunService.getRemainingAttempts(trainingRunId));
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
        return correctFlagDTO;
    }

    @Override
    @TransactionalWO
    public void archiveTrainingRun(Long trainingRunId) {
        LOG.debug("archiveTrainingRun({})", trainingRunId);
        try {
            trainingRunService.archiveTrainingRun(trainingRunId);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalWO
    public void evaluateResponsesToAssessment(Long trainingRunId, String responsesAsString) {
        LOG.debug("evaluateAndStoreAssessment({})", trainingRunId);
        try {
            trainingRunService.evaluateResponsesToAssessment(trainingRunId, responsesAsString);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    private PageResultResource<AccessedTrainingRunDTO> convertToAccessedRunDTO(Page<TrainingRun> runs) {
        List<AccessedTrainingRunDTO> accessedTrainingRunDTOS = new ArrayList<>();
        for (TrainingRun run : runs) {
            AccessedTrainingRunDTO aTRD = new AccessedTrainingRunDTO();
            aTRD.setId(run.getId());
            aTRD.setTitle(run.getTrainingInstance().getTitle());
            aTRD.setTrainingInstanceStartDate(run.getStartTime());
            aTRD.setTrainingInstanceEndDate(run.getEndTime());
            aTRD.setCurrentLevelOrder(trainingRunService.getLevelOrder(run.getTrainingInstance().getTrainingDefinition().getStartingLevel(), run.getCurrentLevel().getId()));
            aTRD.setNumberOfLevels(trainingRunService.getLevels(run.getTrainingInstance().getTrainingDefinition().getStartingLevel()).size());
            if (aTRD.getCurrentLevelOrder() == aTRD.getNumberOfLevels() || LocalDateTime.now().isAfter(aTRD.getTrainingInstanceEndDate())) {
                aTRD.setPossibleAction(Actions.RESULTS);

            } else {
                aTRD.setPossibleAction(Actions.TRY_AGAIN);
            }
        }
        return new PageResultResource<>(accessedTrainingRunDTOS, createPagination(runs));
    }

    private PageResultResource.Pagination createPagination(Page<?> objects) {
        PageResultResource.Pagination pageMetadata = new PageResultResource.Pagination();
        pageMetadata.setNumber(objects.getNumber());
        pageMetadata.setNumberOfElements(objects.getNumberOfElements());
        pageMetadata.setSize(objects.getSize());
        pageMetadata.setTotalElements(objects.getTotalElements());
        pageMetadata.setTotalPages(objects.getTotalPages());
        return pageMetadata;
    }

    private AbstractLevelDTO getCorrectAbstractLevelDTO(AbstractLevel abstractLevel) {
        AbstractLevelDTO abstractLevelDTO;
        if (abstractLevel instanceof AssessmentLevel) {
            AssessmentLevel assessmentLevel = (AssessmentLevel) abstractLevel;
            abstractLevelDTO = beanMapping.mapTo(assessmentLevel, AssessmentLevelDTO.class);
        } else if (abstractLevel instanceof GameLevel) {
            GameLevel gameLevel = (GameLevel) abstractLevel;
            abstractLevelDTO = beanMapping.mapTo(gameLevel, GameLevelDTO.class);
        } else {
            InfoLevel infoLevel = (InfoLevel) abstractLevel;
            abstractLevelDTO = beanMapping.mapTo(infoLevel, InfoLevelDTO.class);
        }
        return abstractLevelDTO;

    }
}
