package cz.muni.ics.kypo.training.facade;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.JsonLoader;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.BasicLevelInfoDTO;
import cz.muni.ics.kypo.training.api.dto.IsCorrectFlagDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.muni.ics.kypo.training.api.dto.hint.HintDTO;
import cz.muni.ics.kypo.training.api.dto.run.AccessTrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.run.AccessedTrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.api.enums.Actions;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import cz.muni.ics.kypo.training.exceptions.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.mapping.mapstruct.*;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import cz.muni.ics.kypo.training.service.TrainingRunService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
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
    private TrainingRunMapper trainingRunMapper;
    private GameLevelMapper gameLevelMapper;
    private AssessmentLevelMapper assessmentLevelMapper;
    private InfoLevelMapper infoLevelMapper;
    private HintMapper hintMapper;

    @Autowired
    public TrainingRunFacadeImpl(TrainingRunService trainingRunService, TrainingRunMapper trainingRunMapper,
                                 GameLevelMapper gameLevelMapper, AssessmentLevelMapper assessmentLevelMapper,
                                 InfoLevelMapper infoLevelMapper, HintMapper hintMapper) {
        this.trainingRunService = trainingRunService;
        this.trainingRunMapper = trainingRunMapper;
        this.gameLevelMapper = gameLevelMapper;
        this.assessmentLevelMapper = assessmentLevelMapper;
        this.infoLevelMapper = infoLevelMapper;
        this.hintMapper = hintMapper;
    }

    @Override
    @TransactionalRO
    public TrainingRunDTO findById(Long id) {
        LOG.debug("findById({})", id);
        try {
            TrainingRun trainingRun = trainingRunService.findById(id);
            return trainingRunMapper.mapToDTO(trainingRun);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalRO
    public PageResultResource<TrainingRunDTO> findAll(Predicate predicate, Pageable pageable) {
        LOG.debug("findAllTrainingDefinitions({},{})", predicate, pageable);
        return trainingRunMapper.mapToPageResultResource(trainingRunService.findAll(predicate, pageable));
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
            TrainingRun trainingRun = trainingRunService.resumeTrainingRun(trainingRunId);
            accessTrainingRunDTO.setTrainingRunID(trainingRun.getId());
            accessTrainingRunDTO.setAbstractLevelDTO(getCorrectAbstractLevelDTO(trainingRun.getCurrentLevel()));
            accessTrainingRunDTO.setShowStepperBar(trainingRun.getTrainingInstance().getTrainingDefinition().isShowStepperBar());
            accessTrainingRunDTO.setInfoAboutLevels(getInfoAboutLevels(trainingRun.getTrainingInstance().getTrainingDefinition().getId()));
            accessTrainingRunDTO.setSandboxInstanceId(trainingRun.getSandboxInstanceRef().getSandboxInstanceRef());
            return accessTrainingRunDTO;
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalWO
    public AccessTrainingRunDTO accessTrainingRun(String accessToken) {
        LOG.debug("accessTrainingRun({})", accessToken);
        AccessTrainingRunDTO accessTrainingRunDTO = new AccessTrainingRunDTO();
        try {
            TrainingRun trainingRun = trainingRunService.accessTrainingRun(accessToken);
            accessTrainingRunDTO.setTrainingRunID(trainingRun.getId());
            accessTrainingRunDTO.setShowStepperBar(trainingRun.getTrainingInstance().getTrainingDefinition().isShowStepperBar());
            accessTrainingRunDTO.setAbstractLevelDTO(getCorrectAbstractLevelDTO(trainingRun.getCurrentLevel()));
            accessTrainingRunDTO.setInfoAboutLevels(getInfoAboutLevels(trainingRun.getCurrentLevel().getTrainingDefinition().getId()));
            accessTrainingRunDTO.setSandboxInstanceId(trainingRun.getSandboxInstanceRef().getSandboxInstanceRef());
            return accessTrainingRunDTO;
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    private List<BasicLevelInfoDTO> getInfoAboutLevels(Long definitionId) {
        List<BasicLevelInfoDTO> infoAboutLevels = new ArrayList<>();
        List<AbstractLevel> levels = trainingRunService.getLevels(definitionId);
        for (AbstractLevel abstractLevel : levels) {
            if (abstractLevel instanceof AssessmentLevel) {
                infoAboutLevels.add(new BasicLevelInfoDTO(abstractLevel.getId(), abstractLevel.getTitle(), LevelType.ASSESSMENT_LEVEL, abstractLevel.getOrder()));
            } else if (abstractLevel instanceof GameLevel) {
                infoAboutLevels.add(new BasicLevelInfoDTO(abstractLevel.getId(), abstractLevel.getTitle(), LevelType.GAME_LEVEL, abstractLevel.getOrder()));
            } else {
                infoAboutLevels.add(new BasicLevelInfoDTO(abstractLevel.getId(), abstractLevel.getTitle(), LevelType.INFO_LEVEL, abstractLevel.getOrder()));
            }
        }
        return infoAboutLevels;
    }

    @Override
    @TransactionalRO
    public PageResultResource<TrainingRunDTO> findAllByTrainingDefinitionAndParticipant(Long trainingDefinitionId, Pageable pageable) {
        LOG.debug("findAllByTrainingDefinitionAndParticipant({})", trainingDefinitionId);
        Page<TrainingRun> trainingRuns = trainingRunService.findAllByTrainingDefinitionAndParticipant(trainingDefinitionId, pageable);
        return trainingRunMapper.mapToPageResultResource(trainingRuns);
    }

    @Override
    @TransactionalRO
    public PageResultResource<TrainingRunDTO> findAllByTrainingDefinition(Long trainingDefinitionId, Pageable pageable) {
        LOG.debug("findAllByTrainingDefinition({})", trainingDefinitionId);
        Page<TrainingRun> trainingRuns = trainingRunService.findAllByTrainingDefinition(trainingDefinitionId, pageable);
        return trainingRunMapper.mapToPageResultResource(trainingRuns);
    }

    @Override
    @TransactionalWO
    public AbstractLevelDTO getNextLevel(Long trainingRunId) {
        LOG.debug("getNextLevel({})", trainingRunId);
        AbstractLevel abstractLevel;
        try {
            abstractLevel = trainingRunService.getNextLevel(trainingRunId);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
        return getCorrectAbstractLevelDTO(abstractLevel);
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
            return hintMapper.mapToDTO(trainingRunService.getHint(trainingRunId, hintId));
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

    private PageResultResource<AccessedTrainingRunDTO> convertToAccessedRunDTO(Page<TrainingRun> trainingRuns) {
        List<AccessedTrainingRunDTO> accessedTrainingRunDTOS = new ArrayList<>();
        for (TrainingRun trainingRun : trainingRuns) {
            AccessedTrainingRunDTO accessedTrainingRunDTO = new AccessedTrainingRunDTO();
            accessedTrainingRunDTO.setId(trainingRun.getId());
            accessedTrainingRunDTO.setTitle(trainingRun.getTrainingInstance().getTitle());
            accessedTrainingRunDTO.setTrainingInstanceStartDate(trainingRun.getTrainingInstance().getStartTime());
            accessedTrainingRunDTO.setTrainingInstanceEndDate(trainingRun.getTrainingInstance().getEndTime());
            accessedTrainingRunDTO.setCurrentLevelOrder(trainingRun.getCurrentLevel().getOrder());
            //number of levels equals maxOrder of definition
            accessedTrainingRunDTO.setNumberOfLevels(trainingRunService.getMaxLevelOrder(trainingRun.getTrainingInstance().getTrainingDefinition().getId()));
            if (accessedTrainingRunDTO.getCurrentLevelOrder() == accessedTrainingRunDTO.getNumberOfLevels() || LocalDateTime.now().isAfter(accessedTrainingRunDTO.getTrainingInstanceEndDate())) {
                accessedTrainingRunDTO.setPossibleAction(Actions.RESULTS);

            } else {
                accessedTrainingRunDTO.setPossibleAction(Actions.TRY_AGAIN);
            }
            accessedTrainingRunDTOS.add(accessedTrainingRunDTO);
        }
        return new PageResultResource<>(accessedTrainingRunDTOS, createPagination(trainingRuns));
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
            abstractLevelDTO = assessmentLevelMapper.mapToDTO(assessmentLevel);
            abstractLevelDTO.setLevelType(LevelType.ASSESSMENT_LEVEL);
            deleteInfoAboutCorrectnessFromQuestions((AssessmentLevelDTO) abstractLevelDTO);
        } else if (abstractLevel instanceof GameLevel) {
            GameLevel gameLevel = (GameLevel) abstractLevel;
            abstractLevelDTO = gameLevelMapper.mapToViewDTO(gameLevel);
            abstractLevelDTO.setLevelType(LevelType.GAME_LEVEL);
        } else {
            InfoLevel infoLevel = (InfoLevel) abstractLevel;
            abstractLevelDTO = infoLevelMapper.mapToDTO(infoLevel);
	    abstractLevelDTO.setLevelType(LevelType.INFO_LEVEL);
        }
        return abstractLevelDTO;
    }

    private void deleteInfoAboutCorrectnessFromQuestions(AssessmentLevelDTO assessmentLevelDTO) {
        try {
            JsonNode jsonNode = JsonLoader.fromString(assessmentLevelDTO.getQuestions());
            for (JsonNode question : jsonNode) {
                ((ObjectNode) question).remove("correct_choices");
                if(question.has("choices")) {
                    for (JsonNode choices : question.get("choices")) {
                        ((ObjectNode) choices).remove("pair");
                        ((ObjectNode) choices).remove("is_correct");

                        }
                    }
                }
            assessmentLevelDTO.setQuestions(jsonNode.toString());
        }catch (IOException ex) {

        }

    }
}
