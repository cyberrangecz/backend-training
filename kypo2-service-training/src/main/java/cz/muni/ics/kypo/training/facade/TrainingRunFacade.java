package cz.muni.ics.kypo.training.facade;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.JsonLoader;
import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.annotations.security.IsOrganizerOrAdmin;
import cz.muni.ics.kypo.training.annotations.security.IsTrainee;
import cz.muni.ics.kypo.training.annotations.security.IsTraineeOrAdmin;
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
import cz.muni.ics.kypo.training.mapping.mapstruct.*;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.service.TrainingRunService;
import cz.muni.ics.kypo.training.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class TrainingRunFacade {

    private static final Logger LOG = LoggerFactory.getLogger(TrainingRunFacade.class);

    private TrainingRunService trainingRunService;
    private TrainingRunMapper trainingRunMapper;
    private GameLevelMapper gameLevelMapper;
    private AssessmentLevelMapper assessmentLevelMapper;
    private InfoLevelMapper infoLevelMapper;
    private HintMapper hintMapper;
    private UserService userService;

    @Autowired
    public TrainingRunFacade(TrainingRunService trainingRunService, TrainingRunMapper trainingRunMapper,
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

    /**
     * Finds specific Training Run by id
     *
     * @param id of a Training Run that would be returned
     * @return specific {@link TrainingRunByIdDTO}
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isTraineeOfGivenTrainingRun(#id)")
    @TransactionalRO
    public TrainingRunByIdDTO findById(Long id) {
        TrainingRun trainingRun = trainingRunService.findById(id);
        TrainingRunByIdDTO trainingRunByIdDTO = trainingRunMapper.mapToFindByIdDTO(trainingRun);
        trainingRunByIdDTO.setDefinitionId(trainingRun.getTrainingInstance().getTrainingDefinition().getId());
        trainingRunByIdDTO.setInstanceId(trainingRun.getTrainingInstance().getId());
        trainingRunByIdDTO.setParticipantRef(userService.getUserRefDTOByUserRefId(trainingRunByIdDTO.getParticipantRef().getUserRefId()));
        return trainingRunByIdDTO;
    }

    /**
     * Find all Training Runs.
     *
     * @param predicate specifies query to the database.
     * @param pageable  pageable parameter with information about pagination.
     * @return page of all {@link TrainingRunDTO}
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)")
    @TransactionalRO
    public PageResultResource<TrainingRunDTO> findAll(Predicate predicate, Pageable pageable) {
        PageResultResource<TrainingRunDTO> trainingRunDTOPageResultResource = trainingRunMapper.mapToPageResultResource(trainingRunService.findAll(predicate, pageable));
        addParticipantsToTrainingRunDTOs(trainingRunDTOPageResultResource.getContent());
        return trainingRunDTOPageResultResource;
    }

    /**
     * Delete selected training runs.
     *
     * @param trainingRunIds training runs to delete
     * @param forceDelete    indicates if this training run should be force deleted.
     */
    @IsOrganizerOrAdmin
    @TransactionalWO
    public void deleteTrainingRuns(List<Long> trainingRunIds, boolean forceDelete) {
        trainingRunIds.forEach(trainingRunId -> trainingRunService.deleteTrainingRun(trainingRunId, forceDelete));
    }

    /**
     * Delete selected training run.
     *
     * @param trainingRunId training run to delete
     * @param forceDelete   indicates if this training run should be force deleted.
     */
    @IsOrganizerOrAdmin
    @TransactionalWO
    public void deleteTrainingRun(Long trainingRunId, boolean forceDelete) {
        trainingRunService.deleteTrainingRun(trainingRunId, forceDelete);
    }

    /**
     * Finds all Training Runs of logged in user.
     *
     * @param pageable    pageable parameter with information about pagination.
     * @param sortByTitle optional parameter. "asc" for ascending sort, "desc" for descending and null if sort is not wanted
     * @return Page of all {@link AccessedTrainingRunDTO} of logged in user.
     */
    @IsTraineeOrAdmin
    @TransactionalRO
    public PageResultResource<AccessedTrainingRunDTO> findAllAccessedTrainingRuns(Pageable pageable, String sortByTitle) {
        Page<TrainingRun> trainingRuns = trainingRunService.findAllByParticipantRefUserRefId(pageable);
        return convertToAccessedRunDTO(trainingRuns, sortByTitle);
    }

    /**
     * Resume given training run.
     *
     * @param trainingRunId id of Training Run to be resumed.
     * @return {@link AccessTrainingRunDTO} response
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
    @TransactionalWO
    public AccessTrainingRunDTO resumeTrainingRun(Long trainingRunId) {
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
    }

    /**
     * Access Training Run by logged in user based on given accessToken.
     *
     * @param accessToken of one training instance
     * @return {@link AccessTrainingRunDTO} response
     */
    @IsTraineeOrAdmin
    @TransactionalWO
    public AccessTrainingRunDTO accessTrainingRun(String accessToken) {
        TrainingRun trainingRun = trainingRunService.accessTrainingRun(accessToken);
        return convertToAccessTrainingRunDTO(trainingRun);
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

    /**
     * Finds all Training Runs by specific Training Definition and logged in user.
     *
     * @param trainingDefinitionId id of Training Definition
     * @param pageable             pageable parameter with information about pagination.
     * @return Page of all {@link AccessedTrainingRunDTO} of logged in user and given definition.
     */
    @IsTrainee
    @TransactionalRO
    public PageResultResource<TrainingRunDTO> findAllByTrainingDefinitionAndParticipant(Long trainingDefinitionId, Pageable pageable) {
        Page<TrainingRun> trainingRuns = trainingRunService.findAllByTrainingDefinitionAndParticipant(trainingDefinitionId, pageable);
        PageResultResource<TrainingRunDTO> trainingRunDTOPageResultResource = trainingRunMapper.mapToPageResultResource(trainingRuns);
        addParticipantsToTrainingRunDTOs(trainingRunDTOPageResultResource.getContent());
        return trainingRunDTOPageResultResource;
    }

    /**
     * Finds all Training Runs of specific training definition.
     *
     * @param trainingDefinitionId id of Training Definition whose Training Runs would be returned.
     * @param pageable             pageable parameter with information about pagination.
     * @return Page of all {@link AccessedTrainingRunDTO} of given definition.
     */
    @IsTrainee
    @TransactionalRO
    public PageResultResource<TrainingRunDTO> findAllByTrainingDefinition(Long trainingDefinitionId, Pageable pageable) {
        Page<TrainingRun> trainingRuns = trainingRunService.findAllByTrainingDefinition(trainingDefinitionId, pageable);
        PageResultResource<TrainingRunDTO> trainingRunDTOPageResultResource = trainingRunMapper.mapToPageResultResource(trainingRuns);
        addParticipantsToTrainingRunDTOs(trainingRunDTOPageResultResource.getContent());
        return trainingRunDTOPageResultResource;
    }

    /**
     * Gets next level of given Training Run and set new current level.
     *
     * @param trainingRunId id of Training Run whose next level should be returned.
     * @return {@link AbstractLevelDTO}
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
    @TransactionalWO
    public AbstractLevelDTO getNextLevel(Long trainingRunId) {
        AbstractLevel abstractLevel;
        abstractLevel = trainingRunService.getNextLevel(trainingRunId);
        return getCorrectAbstractLevelDTO(abstractLevel);
    }

    /**
     * Gets solution of current level of given Training Run.
     *
     * @param trainingRunId id of Training Run which current level gets solution for.
     * @return solution of current level.
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
    @TransactionalWO
    public String getSolution(Long trainingRunId) {
        return trainingRunService.getSolution(trainingRunId);
    }

    /**
     * Gets hint of given current level of given Training Run.
     *
     * @param trainingRunId id of Training Run which current level gets hint for.
     * @param hintId        id of hint to be returned.
     * @return {@link HintDTO}
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
    @TransactionalWO
    public HintDTO getHint(Long trainingRunId, Long hintId) {
        return hintMapper.mapToDTO(trainingRunService.getHint(trainingRunId, hintId));
    }

    /**
     * Check given flag of given Training Run.
     *
     * @param trainingRunId id of Training Run to check flag.
     * @param flag          string which player submit.
     * @return true if flag is correct, false if flag is wrong.
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
    @TransactionalWO
    public IsCorrectFlagDTO isCorrectFlag(Long trainingRunId, String flag) {
        IsCorrectFlagDTO correctFlagDTO = new IsCorrectFlagDTO();
        correctFlagDTO.setCorrect(trainingRunService.isCorrectFlag(trainingRunId, flag));
        correctFlagDTO.setRemainingAttempts(trainingRunService.getRemainingAttempts(trainingRunId));
        if (correctFlagDTO.getRemainingAttempts() == 0) {
            correctFlagDTO.setSolution(getSolution(trainingRunId));
        }
        return correctFlagDTO;
    }

    /**
     * Finish training run.
     *
     * @param trainingRunId id of Training Run to be finished.
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
    @TransactionalWO
    public void finishTrainingRun(Long trainingRunId) {
        trainingRunService.finishTrainingRun(trainingRunId);
    }

    /**
     * Evaluate and store responses to assessment.
     *
     * @param trainingRunId     id of Training Run to be finish.
     * @param responsesAsString responses to assessment
     */
    @IsTrainee
    @TransactionalWO
    public void evaluateResponsesToAssessment(Long trainingRunId, String responsesAsString) {
        trainingRunService.evaluateResponsesToAssessment(trainingRunId, responsesAsString);
    }

    /**
     * Retrieve info about the participant of the given training run.
     *
     * @param trainingRunId id of the training run whose participant you want to get.
     * @return returns participant of the given training run.
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
    @TransactionalRO
    public UserRefDTO getParticipant(Long trainingRunId) {
        TrainingRun trainingRun = trainingRunService.findById(trainingRunId);
        return userService.getUserRefDTOByUserRefId(trainingRun.getParticipantRef().getUserRefId());
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
            ex.printStackTrace();
        }

    }
}
