package cz.muni.ics.kypo.training.facade.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import cz.muni.ics.kypo.training.api.dto.*;
import cz.muni.ics.kypo.training.api.dto.run.AccessTrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.run.AccessedTrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.api.enums.Actions;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import cz.muni.ics.kypo.training.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.facade.TrainingRunFacade;
import cz.muni.ics.kypo.training.mapping.BeanMapping;
import cz.muni.ics.kypo.training.service.TrainingRunService;

/**
 * @author Dominik Pilar (445537)
 *
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
  @Transactional(readOnly = true)
  public TrainingRunDTO findById(Long id) {
    LOG.debug("findById({})", id);
    try {
      TrainingRun trainingRun = trainingRunService.findById(id);
      return beanMapping.mapTo(trainingRun, TrainingRunDTO.class);
    } catch (NullPointerException ex) {
      throw new FacadeLayerException("Given TrainingRun ID is null.");
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  @Transactional(readOnly = true)
  public PageResultResource<TrainingRunDTO> findAll(Predicate predicate, Pageable pageable) {
    LOG.debug("findAll({},{})", predicate, pageable);
    try {
      return beanMapping.mapToPageResultDTO(trainingRunService.findAll(predicate, pageable), TrainingRunDTO.class);
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  public PageResultResource<AccessedTrainingRunDTO> findAllAccessedTrainingRuns(Pageable pageable) {
    LOG.debug("findAllAccessedTrainingRuns()");
    try {
      Page<TrainingRun> trainingRuns = trainingRunService.findAllByParticipantRefLogin(pageable);
      return convertToAccessedRunDTO(trainingRuns);
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  public AccessTrainingRunDTO accessTrainingRun(String password) {
    LOG.debug("accessTrainingRun({})", password);
    AccessTrainingRunDTO accessTrainingRunDTO = new AccessTrainingRunDTO();
    try {
      AbstractLevel abstractLevel = trainingRunService.accessTrainingRun(password);
      if (abstractLevel instanceof AssessmentLevel) {
        AssessmentLevel assessmentLevel = (AssessmentLevel) abstractLevel;
        accessTrainingRunDTO.setAbstractLevelDTO(beanMapping.mapTo(assessmentLevel, AssessmentLevelDTO.class));
      } else if (abstractLevel instanceof GameLevel) {
        GameLevel gameLevel = (GameLevel) abstractLevel;
        accessTrainingRunDTO.setAbstractLevelDTO(beanMapping.mapTo(gameLevel, GameLevelDTO.class));
      } else {
        InfoLevel infoLevel = (InfoLevel) abstractLevel;
        accessTrainingRunDTO.setAbstractLevelDTO(beanMapping.mapTo(infoLevel, InfoLevelDTO.class));
      }

      accessTrainingRunDTO.setInfoLevels(getInfoAboutLevels(abstractLevel.getId()));
      return accessTrainingRunDTO;
    } catch (IllegalArgumentException ex) {
      throw new FacadeLayerException("Password cannot be null and must has length.");
    }
  }

  private List<BasicInfoLevelDTO> getInfoAboutLevels(Long firstLevelId) {
    List<BasicInfoLevelDTO> infoAboutLevels = new ArrayList<>();
    List<AbstractLevel> levels = trainingRunService.getLevels(firstLevelId);
    for (AbstractLevel al: levels) {
      if (al instanceof AssessmentLevel) {
        infoAboutLevels.add(new BasicInfoLevelDTO(al.getId(), al.getTitle(), LevelType.ASSESSMENT_LEVEL));
      } else if (al instanceof  GameLevel) {
        infoAboutLevels.add(new BasicInfoLevelDTO(al.getId(), al.getTitle(), LevelType.GAME_LEVEL));
      } else {
        infoAboutLevels.add(new BasicInfoLevelDTO(al.getId(), al.getTitle(), LevelType.INFO_LEVEL));
      }
    }
    return infoAboutLevels;
  }

  @Override
  public PageResultResource<TrainingRunDTO> findAllByTrainingDefinitionAndParticipant(Long trainingDefinitionId, Pageable pageable) {
    LOG.debug("findAllByTrainingDefinitionAndParticipant({})", trainingDefinitionId);
    try {
      Page<TrainingRun> trainingRuns = trainingRunService.findAllByTrainingDefinitionAndParticipant(trainingDefinitionId, pageable);
      return beanMapping.mapToPageResultDTO(trainingRuns, TrainingRunDTO.class);
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  public PageResultResource<TrainingRunDTO> findAllByTrainingDefinition(Long trainingDefinitionId, Pageable pageable) {
    LOG.debug("findAllByTrainingDefinition({})", trainingDefinitionId);
    try {
      Page<TrainingRun> trainingRuns = trainingRunService.findAllByTrainingDefinition(trainingDefinitionId, pageable);
      return beanMapping.mapToPageResultDTO(trainingRuns, TrainingRunDTO.class);
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }
  }

  @Override
  public PageResultResource<TrainingRunDTO> findAllByTrainingInstance(Long trainingInstanceId, Pageable pageable) {
    LOG.debug("findAllByTrainingInstance({})", trainingInstanceId);
    try {
      Page<TrainingRun> trainingRuns = trainingRunService.findAllByTrainingInstance(trainingInstanceId, pageable);
      return beanMapping.mapToPageResultDTO(trainingRuns, TrainingRunDTO.class);
    } catch (ServiceLayerException ex) {
      throw new FacadeLayerException(ex.getLocalizedMessage());
    }

  }

  @Override
  public AbstractLevelDTO getNextLevel(Long trainingRunId) {
    LOG.debug("getNextLevel({})", trainingRunId);
    AbstractLevel aL = trainingRunService.getNextLevel(trainingRunId);
    if(aL instanceof GameLevel) {
      return beanMapping.mapTo(trainingRunService.getNextLevel(trainingRunId), GameLevelDTO.class);

    } else if (aL instanceof AbstractLevel) {
      return beanMapping.mapTo(trainingRunService.getNextLevel(trainingRunId), AssessmentLevelDTO.class);
    } else {
      return beanMapping.mapTo(trainingRunService.getNextLevel(trainingRunId), InfoLevelDTO.class);
    }
  }

  @Override
  public String getSolution(Long trainingRunId) {
    LOG.debug("getSolution({})", trainingRunId);
    return trainingRunService.getSolution(trainingRunId);
  }

  @Override
  public HintDTO getHint(Long trainingRunId, Long hintId) {
    LOG.debug("getHint({},{})", trainingRunId, hintId);
    return beanMapping.mapTo(trainingRunService.getHint(trainingRunId,hintId), HintDTO.class);
  }

  @Override
  public IsCorrectFlagDTO isCorrectFlag(Long trainingRunId, String flag, boolean solutionTaken) {
    LOG.debug("isCorrectFlag({},{})", trainingRunId, flag);
    IsCorrectFlagDTO correctFlagDTO = new IsCorrectFlagDTO();
    if (solutionTaken) {
      correctFlagDTO.setRemainingAttempts(0);
      correctFlagDTO.setCorrect(trainingRunService.isCorrectFlag(trainingRunId, flag));
    } else {
      int attempts = trainingRunService.getRemainingAttempts(trainingRunId);
      correctFlagDTO.setRemainingAttempts(attempts - 1);
      correctFlagDTO.setCorrect(trainingRunService.isCorrectFlag(trainingRunId, flag));
    }

    return correctFlagDTO;
  }

  private PageResultResource<AccessedTrainingRunDTO> convertToAccessedRunDTO(Page<TrainingRun> runs) {
    List<AccessedTrainingRunDTO> accessedTrainingRunDTOS = new ArrayList<>();
    for (TrainingRun run: runs) {
      AccessedTrainingRunDTO aTRD = new AccessedTrainingRunDTO();
      aTRD.setId(run.getId());
      aTRD.setTitle(run.getTrainingInstance().getTitle());
      aTRD.setTrainingInstanceStartDate(run.getStartTime());
      aTRD.setTrainingInstanceEndDate(run.getEndTime());
      aTRD.setCurrentLevelOrder(trainingRunService.getLevelOrder(run.getTrainingInstance().getTrainingDefinition().getStartingLevel(),run.getCurrentLevel().getId()));
      aTRD.setNumberOfLevels(trainingRunService.getLevels(run.getTrainingInstance().getTrainingDefinition().getStartingLevel()).size());
      if (aTRD.getCurrentLevelOrder() == aTRD.getNumberOfLevels() || LocalDateTime.now().isAfter(aTRD.getTrainingInstanceEndDate())) {
        aTRD.setPossibleAction(Actions.RESULTS);

      } else {
        aTRD.setPossibleAction(Actions.TRY_AGAIN);
      }
    }
    PageResultResource<AccessedTrainingRunDTO> pageResultDTO = new PageResultResource<AccessedTrainingRunDTO>(accessedTrainingRunDTOS, createPagination(runs));
    return pageResultDTO;
  }


  private <T> PageResultResource.Pagination<T> createPagination(Page<?> objects) {
    PageResultResource.Pagination<T> pageMetadata = new PageResultResource.Pagination<T>();
    pageMetadata.setNumber(objects.getNumber());
    pageMetadata.setNumberOfElements(objects.getNumberOfElements());
    pageMetadata.setSize(objects.getSize());
    pageMetadata.setTotalElements(objects.getTotalElements());
    pageMetadata.setTotalPages(objects.getTotalPages());
    return pageMetadata;
  }
}
