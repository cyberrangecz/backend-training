package cz.muni.ics.kypo.training.facade;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.JsonLoader;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.BasicLevelInfoDTO;
import cz.muni.ics.kypo.training.api.dto.IsCorrectFlagDTO;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.muni.ics.kypo.training.api.dto.hint.HintDTO;
import cz.muni.ics.kypo.training.api.dto.hint.TakenHintDTO;
import cz.muni.ics.kypo.training.api.dto.run.AccessTrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.run.AccessedTrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunByIdDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.api.enums.Actions;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.exceptions.ErrorCode;
import cz.muni.ics.kypo.training.exceptions.FacadeLayerException;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.mapping.mapstruct.*;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.service.TrainingRunService;
import cz.muni.ics.kypo.training.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class TrainingRunFacadeImpl implements TrainingRunFacade {

    private static final Logger LOG = LoggerFactory.getLogger(TrainingRunFacadeImpl.class);

    private TrainingRunService trainingRunService;
    private TrainingRunMapper trainingRunMapper;
    private GameLevelMapper gameLevelMapper;
    private AssessmentLevelMapper assessmentLevelMapper;
    private InfoLevelMapper infoLevelMapper;
    private HintMapper hintMapper;
    private UserService userService;

    @Autowired
    public TrainingRunFacadeImpl(TrainingRunService trainingRunService, TrainingRunMapper trainingRunMapper,
                                 GameLevelMapper gameLevelMapper, AssessmentLevelMapper assessmentLevelMapper,
                                 InfoLevelMapper infoLevelMapper, HintMapper hintMapper, UserService userService) {
        this.trainingRunService = trainingRunService;
        this.trainingRunMapper = trainingRunMapper;
        this.gameLevelMapper = gameLevelMapper;
        this.assessmentLevelMapper = assessmentLevelMapper;
        this.infoLevelMapper = infoLevelMapper;
        this.hintMapper = hintMapper;
        this.userService = userService;
    }

    @Override
    @TransactionalRO
    public TrainingRunByIdDTO findById(Long id) {
        try {
            TrainingRun trainingRun = trainingRunService.findById(id);
            TrainingRunByIdDTO trainingRunByIdDTO = trainingRunMapper.mapToFindByIdDTO(trainingRun);
            trainingRunByIdDTO.setDefinitionId(trainingRun.getTrainingInstance().getTrainingDefinition().getId());
            trainingRunByIdDTO.setInstanceId(trainingRun.getTrainingInstance().getId());
            trainingRunByIdDTO.setParticipantRef(userService.getUserRefDTOByUserRefId(trainingRunByIdDTO.getParticipantRef().getUserRefId()));
            return trainingRunByIdDTO;
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalRO
    public PageResultResource<TrainingRunDTO> findAll(Predicate predicate, Pageable pageable) {
        PageResultResource<TrainingRunDTO>  trainingRunDTOPageResultResource = trainingRunMapper.mapToPageResultResource(trainingRunService.findAll(predicate, pageable));
        addParticipantsToTrainingRunDTOs(trainingRunDTOPageResultResource.getContent());
        return trainingRunDTOPageResultResource;
    }

    @Override
    @TransactionalWO
    public void deleteTrainingRuns(List<Long> trainingRunIds) {
        trainingRunService.deleteTrainingRuns(trainingRunIds);
    }

    @Override
    @TransactionalWO
    public void deleteTrainingRun(Long trainingRunId) {
        trainingRunService.deleteTrainingRun(trainingRunId);
    }

    @Override
    @TransactionalRO
    public PageResultResource<AccessedTrainingRunDTO> findAllAccessedTrainingRuns(Pageable pageable, String sortByTitle) {
        Page<TrainingRun> trainingRuns = trainingRunService.findAllByParticipantRefUserRefId(pageable);
        return convertToAccessedRunDTO(trainingRuns, sortByTitle);
    }

    @Override
    @TransactionalWO
    public AccessTrainingRunDTO resumeTrainingRun(Long trainingRunId) {
        try {
            TrainingRun trainingRun = trainingRunService.resumeTrainingRun(trainingRunId);
            AccessTrainingRunDTO accessTrainingRunDTO = convertToAccessTrainingRunDTO(trainingRun);
            if (trainingRun.getCurrentLevel() instanceof GameLevel) {
                if (trainingRun.isSolutionTaken()) {
                    accessTrainingRunDTO.setTakenSolution(((GameLevel) trainingRun.getCurrentLevel()).getSolution());
                }
                trainingRun.getHintInfoList().forEach(hintInfo -> {
                            if (hintInfo.getGameLevelId().equals(trainingRun.getCurrentLevel().getId())) {
                                accessTrainingRunDTO.getTakenHints().add(convertToTakenHintDTO(hintInfo));
                            }
                        }
                );
            }
            return accessTrainingRunDTO;
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalWO
    public AccessTrainingRunDTO accessTrainingRun(String accessToken) {
        try {
            TrainingRun trainingRun = trainingRunService.accessTrainingRun(accessToken);
            if (trainingRun.getSandboxInstanceRefId() == null){
                trainingRun = trainingRunService.assignSandbox(trainingRun);
            }
            return convertToAccessTrainingRunDTO(trainingRun);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        } catch (DataIntegrityViolationException ex) {
            throw new FacadeLayerException(new ServiceLayerException(ex.getLocalizedMessage(), ErrorCode.UNEXPECTED_ERROR));
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
        Page<TrainingRun> trainingRuns = trainingRunService.findAllByTrainingDefinitionAndParticipant(trainingDefinitionId, pageable);
        PageResultResource<TrainingRunDTO>  trainingRunDTOPageResultResource = trainingRunMapper.mapToPageResultResource(trainingRuns);
        addParticipantsToTrainingRunDTOs(trainingRunDTOPageResultResource.getContent());
        return trainingRunDTOPageResultResource;
    }

    @Override
    @TransactionalRO
    public PageResultResource<TrainingRunDTO> findAllByTrainingDefinition(Long trainingDefinitionId, Pageable pageable) {
        Page<TrainingRun> trainingRuns = trainingRunService.findAllByTrainingDefinition(trainingDefinitionId, pageable);
        PageResultResource<TrainingRunDTO>  trainingRunDTOPageResultResource = trainingRunMapper.mapToPageResultResource(trainingRuns);
        addParticipantsToTrainingRunDTOs(trainingRunDTOPageResultResource.getContent());
        return trainingRunDTOPageResultResource;
    }

    @Override
    @TransactionalWO
    public AbstractLevelDTO getNextLevel(Long trainingRunId) {
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
        try {
            return trainingRunService.getSolution(trainingRunId);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalWO
    public HintDTO getHint(Long trainingRunId, Long hintId) {
        try {
            return hintMapper.mapToDTO(trainingRunService.getHint(trainingRunId, hintId));
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalWO
    public IsCorrectFlagDTO isCorrectFlag(Long trainingRunId, String flag) {
        IsCorrectFlagDTO correctFlagDTO = new IsCorrectFlagDTO();
        try {
            correctFlagDTO.setCorrect(trainingRunService.isCorrectFlag(trainingRunId, flag));
            correctFlagDTO.setRemainingAttempts(trainingRunService.getRemainingAttempts(trainingRunId));
            if (correctFlagDTO.getRemainingAttempts() == 0) {
                correctFlagDTO.setSolution(getSolution(trainingRunId));
            }
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
        return correctFlagDTO;
    }

    @Override
    @TransactionalWO
    public void finishTrainingRun(Long trainingRunId) {
        try {
            trainingRunService.finishTrainingRun(trainingRunId);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalWO
    public void evaluateResponsesToAssessment(Long trainingRunId, String responsesAsString) {
        try {
            trainingRunService.evaluateResponsesToAssessment(trainingRunId, responsesAsString);
        } catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    @Override
    @TransactionalRO
    public UserRefDTO getParticipant(Long trainingRunId) {
        try {
            TrainingRun trainingRun = trainingRunService.findById(trainingRunId);
            return userService.getUserRefDTOByUserRefId(trainingRun.getParticipantRef().getUserRefId());
        }catch (ServiceLayerException ex) {
            throw new FacadeLayerException(ex);
        }
    }

    private void addParticipantsToTrainingRunDTOs(List<TrainingRunDTO> trainingRunDTOS) {
        trainingRunDTOS.forEach(trainingRunDTO ->
                trainingRunDTO.setParticipantRef(userService.getUserRefDTOByUserRefId(trainingRunDTO.getParticipantRef().getUserRefId())));
    }


    private PageResultResource<AccessedTrainingRunDTO> convertToAccessedRunDTO(Page<TrainingRun> trainingRuns, String sortByTitle) {
        List<AccessedTrainingRunDTO> accessedTrainingRunDTOS = new ArrayList<>();
        for (TrainingRun trainingRun : trainingRuns) {
            AccessedTrainingRunDTO accessedTrainingRunDTO = new AccessedTrainingRunDTO();
            accessedTrainingRunDTO.setId(trainingRun.getId());
            accessedTrainingRunDTO.setTitle(trainingRun.getTrainingInstance().getTitle());
            accessedTrainingRunDTO.setTrainingInstanceStartDate(trainingRun.getTrainingInstance().getStartTime());
            accessedTrainingRunDTO.setTrainingInstanceEndDate(trainingRun.getTrainingInstance().getEndTime());
            accessedTrainingRunDTO.setInstanceId(trainingRun.getTrainingInstance().getId());
            accessedTrainingRunDTO.setNumberOfLevels(trainingRunService.getMaxLevelOrder(trainingRun.getTrainingInstance().getTrainingDefinition().getId()) + 1);
            accessedTrainingRunDTO.setCurrentLevelOrder(trainingRun.getCurrentLevel().getOrder() + 1);
            boolean isTrainingRunFinished = trainingRun.isLevelAnswered() && accessedTrainingRunDTO.getCurrentLevelOrder() == accessedTrainingRunDTO.getNumberOfLevels();
            boolean isTrainingInstanceRunning = LocalDateTime.now(Clock.systemUTC()).isBefore(accessedTrainingRunDTO.getTrainingInstanceEndDate());
            if (isTrainingRunFinished || !isTrainingInstanceRunning) {
                accessedTrainingRunDTO.setPossibleAction(Actions.RESULTS);
            } else if (!isTrainingRunFinished && isTrainingInstanceRunning) {
                accessedTrainingRunDTO.setPossibleAction(Actions.RESUME);
            } else {
                accessedTrainingRunDTO.setPossibleAction(Actions.NONE);
            }
            accessedTrainingRunDTOS.add(accessedTrainingRunDTO);
        }
        if (sortByTitle != null && !sortByTitle.isBlank()) {
            if (accessedTrainingRunDTOS.size() > 0) {
                if (sortByTitle.equals("asc")) {
                    accessedTrainingRunDTOS.sort(Comparator.comparing(AccessedTrainingRunDTO::getTitle));
                } else if (sortByTitle.equals("desc")) {
                    accessedTrainingRunDTOS.sort(Comparator.comparing(AccessedTrainingRunDTO::getTitle).reversed());
                }
            }
        }
        return new PageResultResource<>(accessedTrainingRunDTOS, createPagination(trainingRuns));
    }

    private TakenHintDTO convertToTakenHintDTO(HintInfo hintInfo) {
        TakenHintDTO takenHintDTO = new TakenHintDTO();
        takenHintDTO.setId(hintInfo.getHintId());
        takenHintDTO.setContent(hintInfo.getHintContent());
        takenHintDTO.setTitle(hintInfo.getHintTitle());
        return takenHintDTO;
    }

    private AccessTrainingRunDTO convertToAccessTrainingRunDTO(TrainingRun trainingRun) {
        AccessTrainingRunDTO accessTrainingRunDTO = new AccessTrainingRunDTO();
        accessTrainingRunDTO.setTrainingRunID(trainingRun.getId());
        accessTrainingRunDTO.setAbstractLevelDTO(getCorrectAbstractLevelDTO(trainingRun.getCurrentLevel()));
        accessTrainingRunDTO.setShowStepperBar(trainingRun.getTrainingInstance().getTrainingDefinition().isShowStepperBar());
        accessTrainingRunDTO.setInfoAboutLevels(getInfoAboutLevels(trainingRun.getCurrentLevel().getTrainingDefinition().getId()));
        accessTrainingRunDTO.setSandboxInstanceRefId(trainingRun.getSandboxInstanceRefId());
        accessTrainingRunDTO.setInstanceId(trainingRun.getTrainingInstance().getId());
        accessTrainingRunDTO.setStartTime(trainingRun.getStartTime());
        return accessTrainingRunDTO;
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
                if (question.has("choices")) {
                    for (JsonNode choices : question.get("choices")) {
                        ((ObjectNode) choices).remove("pair");
                        ((ObjectNode) choices).remove("is_correct");

                    }
                }
            }
            assessmentLevelDTO.setQuestions(jsonNode.toString());
        } catch (IOException ex) {

        }

    }
}
