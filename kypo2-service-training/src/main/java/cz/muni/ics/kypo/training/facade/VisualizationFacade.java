package cz.muni.ics.kypo.training.facade;

import cz.muni.csirt.kypo.events.AbstractAuditPOJO;
import cz.muni.csirt.kypo.events.trainings.*;
import cz.muni.csirt.kypo.events.trainings.LevelCompleted;
import cz.muni.csirt.kypo.events.trainings.TrainingRunEnded;
import cz.muni.csirt.kypo.events.trainings.enums.LevelType;
import cz.muni.ics.kypo.training.annotations.security.IsDesignerOrOrganizerOrAdmin;
import cz.muni.ics.kypo.training.annotations.security.IsTraineeOrAdmin;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.*;
import cz.muni.ics.kypo.training.api.dto.visualization.clustering.*;
import cz.muni.ics.kypo.training.api.dto.visualization.commons.PlayerDataDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.leveltabs.*;
import cz.muni.ics.kypo.training.api.dto.visualization.progress.LevelDefinitionProgressDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.table.TableLevelDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.table.TablePlayerDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.timeline.TimelineDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.commons.VisualizationAbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.timeline.TimelineLevelDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.commons.EventDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.timeline.TimelinePlayerDTO;
import cz.muni.ics.kypo.training.api.enums.AssessmentType;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.mapping.mapstruct.LevelMapper;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.persistence.model.AssessmentLevel;
import cz.muni.ics.kypo.training.api.dto.visualization.progress.LevelProgress;
import cz.muni.ics.kypo.training.api.dto.visualization.progress.PlayerProgress;
import cz.muni.ics.kypo.training.api.dto.visualization.progress.VisualizationProgressDTO;
import cz.muni.ics.kypo.training.api.enums.LevelState;
import cz.muni.ics.kypo.training.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.stream.Collectors;

/**
 * The type Visualization facade.
 */
@Service
@Transactional
public class VisualizationFacade {

    private static final Logger LOG = LoggerFactory.getLogger(VisualizationFacade.class);

    private TrainingDefinitionService trainingDefinitionService;
    private TrainingInstanceService trainingInstanceService;
    private TrainingRunService trainingRunService;
    private VisualizationService visualizationService;
    private ElasticsearchApiService elasticsearchApiService;
    private UserService userService;
    private LevelMapper levelMapper;

    /**
     * Instantiates a new Visualization facade.
     *
     * @param trainingRunService      the training run service
     * @param trainingInstanceService the training instance service
     * @param visualizationService    the visualization service
     * @param levelMapper             the level mapper
     * @param userService             the user service
     */
    @Autowired
    public VisualizationFacade(TrainingDefinitionService trainingDefinitionService,
                               TrainingInstanceService trainingInstanceService,
                               TrainingRunService trainingRunService,
                               VisualizationService visualizationService,
                               ElasticsearchApiService elasticsearchApiService,
                               UserService userService,
                               LevelMapper levelMapper) {
        this.trainingDefinitionService = trainingDefinitionService;
        this.trainingInstanceService = trainingInstanceService;
        this.trainingRunService = trainingRunService;
        this.visualizationService = visualizationService;
        this.elasticsearchApiService = elasticsearchApiService;
        this.levelMapper = levelMapper;
        this.userService = userService;
    }

    /**
     * Gather all the necessary information about the users with given ids.
     *
     * @param usersIds ids of the users to be retrieved.
     * @param pageable pageable parameter with information about pagination.
     * @return basic info about the users with given ids.
     */
    @IsDesignerOrOrganizerOrAdmin
    public PageResultResource<UserRefDTO> getUsersByIds(Set<Long> usersIds, Pageable pageable) {
        return userService.getUsersRefDTOByGivenUserIds(usersIds, pageable, null, null);
    }

    /**
     * Gather all the necessary information about the training run needed to visualize the result.
     *
     * @param trainingRunId id of Training Run to gets info.
     * @return basic info about the training definition of given a training run and the necessary info about all levels from that training run.
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
    @TransactionalWO
    public VisualizationInfoDTO getVisualizationInfoAboutTrainingRun(Long trainingRunId) {
        TrainingRun trainingRun = trainingRunService.findByIdWithLevel(trainingRunId);
        TrainingDefinition trainingDefinitionOfTrainingRun = trainingRun.getTrainingInstance().getTrainingDefinition();
        return new VisualizationInfoDTO(trainingDefinitionOfTrainingRun.getId(), trainingDefinitionOfTrainingRun.getTitle(),
                trainingDefinitionOfTrainingRun.getEstimatedDuration(), convertToAbstractLevelVisualizationDTO(visualizationService.getLevelsForTraineeVisualization(trainingRun)));
    }

    /**
     * Gather all the necessary information about the training run needed to visualize the result.
     *
     * @param trainingRunId id of Training Run to gets info.
     * @return the list of all commands in a sandbox.
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#instanceId)" +
            "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
    @TransactionalWO
    public List<Map<String, Object>> getAllCommandsInTrainingRun(Long instanceId, Long trainingRunId) {
        TrainingRun trainingRun = trainingRunService.findById(trainingRunId);
        Long sandboxInstanceRef = trainingRun.getSandboxInstanceRefId();
        return elasticsearchApiService.findAllConsoleCommandsFromSandbox(sandboxInstanceRef);
    }

    /**
     * Gather all the necessary information about the training instance needed to visualize the result.
     *
     * @param trainingInstanceId id of Training Instance to gets info.
     * @return basic info about the training definition of given a training instance and the necessary info about all levels from that training instance.
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
    @TransactionalRO
    public VisualizationInfoDTO getVisualizationInfoAboutTrainingInstance(Long trainingInstanceId) {
        TrainingInstance trainingInstance = trainingInstanceService.findById(trainingInstanceId);
        TrainingDefinition trainingDefinitionOfTrainingRun = trainingInstance.getTrainingDefinition();
        return new VisualizationInfoDTO(trainingDefinitionOfTrainingRun.getId(), trainingDefinitionOfTrainingRun.getTitle(),
                trainingDefinitionOfTrainingRun.getEstimatedDuration(), convertToAbstractLevelVisualizationDTO(visualizationService.getLevelsForOrganizerVisualization(trainingInstance)));
    }

    /**
     * Gather all the necessary information about the users for specific training instance.
     *
     * @param trainingInstanceId id of Training Instance to gets info about all participants.
     * @return basic info about the participants of given a training instance.
     */
    @IsTraineeOrAdmin
    public List<UserRefDTO> getParticipantsForGivenTrainingInstance(Long trainingInstanceId) {
        Set<Long> participantsRefIds = visualizationService.getAllParticipantsRefIdsForSpecificTrainingInstance(trainingInstanceId);
        PageResultResource<UserRefDTO> participantsInfo;
        List<UserRefDTO> participants = new ArrayList<>();
        int page = 0;
        do {
            participantsInfo = userService.getUsersRefDTOByGivenUserIds(participantsRefIds, PageRequest.of(page, 999), null, null);
            participants.addAll(participantsInfo.getContent());
            page++;
        }
        while (participantsInfo.getPagination().getNumber() != participantsInfo.getPagination().getTotalPages());
        return participants;
    }

    /**
     * Gather all the necessary information about the progress of training instance needed to visualize the result.
     *
     * @param trainingInstanceId id of Training Instance to gets info.
     * @return info about the training instance and the necessary info about all players, levels and events from that training instance.
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
    @TransactionalRO
    public VisualizationProgressDTO getProgressVisualization(Long trainingInstanceId) {
        TrainingInstance trainingInstance = trainingInstanceService.findById(trainingInstanceId);
        TrainingDefinition trainingDefinitionOfTrainingRun = trainingInstance.getTrainingDefinition();

        VisualizationProgressDTO visualizationProgressDTO = new VisualizationProgressDTO();
        visualizationProgressDTO.setStartTime(trainingInstance.getStartTime().toEpochSecond(ZoneOffset.UTC));
        visualizationProgressDTO.setCurrentTime(LocalDateTime.now(Clock.systemUTC()).toEpochSecond(ZoneOffset.UTC));
        visualizationProgressDTO.setEstimatedEndTime(trainingInstance.getEndTime().toEpochSecond(ZoneOffset.UTC));

        //Players
        Set<Long> playersIds = visualizationService.getAllParticipantsRefIdsForSpecificTrainingInstance(trainingInstanceId);
        List<UserRefDTO> players = new ArrayList<>(userService.getUsersRefDTOByGivenUserIds(playersIds, PageRequest.of(0, 20), null, null).getContent());
        players.sort(Comparator.comparingLong(UserRefDTO::getUserRefId));
        visualizationProgressDTO.setPlayers(players);

        //Levels
        List<LevelDefinitionProgressDTO> levels = trainingRunService.getLevels(trainingDefinitionOfTrainingRun.getId()).stream()
                .map(level -> levelMapper.mapToLevelDefinitionProgressDTO(level))
                .sorted(Comparator.comparingInt(LevelDefinitionProgressDTO::getOrder))
                .collect(Collectors.toList());
        visualizationProgressDTO.setLevels(levels);

        Map<Long, Map<Long, List<AbstractAuditPOJO>>> eventsFromElasticsearch = elasticsearchApiService.getAggregatedEventsByTrainingRunsAndLevels(trainingInstance);

        List<PlayerProgress> playerProgresses = new ArrayList<>();
        for (Map.Entry<Long, Map<Long, List<AbstractAuditPOJO>>> userEvents : eventsFromElasticsearch.entrySet()) {
            PlayerProgress playerProgress = new PlayerProgress();
            playerProgress.setTrainingRunId(userEvents.getKey());

            for (Map.Entry<Long, List<AbstractAuditPOJO>> levelEvents : userEvents.getValue().entrySet()) {
                List<AbstractAuditPOJO> events = levelEvents.getValue();
                LevelProgress levelProgress = new LevelProgress();
                levelProgress.setLevelId(levelEvents.getKey());
                levelProgress.setState(LevelState.RUNNING);
                //Count wrong flags number
                int levelStartedEventIndex = 0;
                if (events.get(0) instanceof TrainingRunStarted) {
                    levelStartedEventIndex = 1;
                }
                levelProgress.setStartTime(events.get(levelStartedEventIndex).getTimestamp());
                if (((LevelStarted) events.get(levelStartedEventIndex)).getLevelType() == LevelType.GAME) {
                    this.countWrongFlagsAndAddTakenHints(levelProgress, events);
                }

                int levelCompletedEventIndex = getLevelCompletedEventIndex(events);
                if (levelCompletedEventIndex != -1) {
                    levelProgress.setState(LevelState.FINISHED);
                    levelProgress.setEndTime(events.get(levelCompletedEventIndex).getTimestamp());
                }
                levelProgress.setEvents(events);
                playerProgress.setUserRefId(events.get(0).getUserRefId());
                playerProgress.addLevelProgress(levelProgress);
            }
            playerProgresses.add(playerProgress);
        }
        visualizationProgressDTO.setPlayerProgress(playerProgresses);
        return visualizationProgressDTO;
    }

    private int getLevelCompletedEventIndex(List<AbstractAuditPOJO> events) {
        AbstractAuditPOJO lastEvent = events.get(events.size() - 1);
        if (!(lastEvent instanceof LevelCompleted) && !(lastEvent instanceof TrainingRunEnded)) {
            return -1;
        }
        return lastEvent instanceof TrainingRunEnded ? events.size() - 2 : events.size() - 1;

    }

    private void countWrongFlagsAndAddTakenHints(LevelProgress levelProgress, List<AbstractAuditPOJO> events) {
        levelProgress.setWrongFlagsNumber(0L);
        for (AbstractAuditPOJO event : events) {
            if (event instanceof WrongFlagSubmitted) {
                levelProgress.increaseWrongFlagsNumber();
            } else if (event instanceof HintTaken) {
                levelProgress.addHintTaken(((HintTaken) event).getHintId());
            }
        }
    }

    private List<AbstractLevelVisualizationDTO> convertToAbstractLevelVisualizationDTO(List<AbstractLevel> abstractLevels) {
        List<AbstractLevelVisualizationDTO> visualizationLevelInfoDTOs = new ArrayList<>();
        abstractLevels.forEach(level ->
                visualizationLevelInfoDTOs.add(levelMapper.mapToVisualizationDTO(level)));
        return visualizationLevelInfoDTOs;
    }

    /**
     * Gather all the necessary information for organizer to display clustering visualizations.
     *
     * @param trainingInstanceId id of training instance.
     * @return data for clustering visualizations.
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
    @TransactionalRO
    public ClusteringVisualizationDTO getClusteringVisualizationsForOrganizer(Long trainingInstanceId) {
        return getClusteringVisualizations(trainingInstanceId, null);
    }

    /**
     * Gather all the necessary information for trainee to display clustering visualizations.
     *
     * @param trainingRunId id of training run.
     * @return data for clustering visualizations.
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
    @TransactionalWO
    public ClusteringVisualizationDTO getClusteringVisualizationsForTrainee(Long trainingRunId) {
        TrainingRun trainingRun = trainingRunService.findByIdWithLevel(trainingRunId);
        return getClusteringVisualizations(trainingRun.getTrainingInstance().getId(), trainingRun.getParticipantRef().getUserRefId());
    }

    private ClusteringVisualizationDTO getClusteringVisualizations(Long trainingInstanceId, Long userRefIdToAnonymize) {
        TrainingInstanceData trainingInstanceData = getTrainingInstanceData(trainingInstanceId, elasticsearchApiService::getAggregatedEventsByLevelsAndTrainingRuns,
                this::retrieveRunsIdsFromEventsAggregatedByLevelsAndTrainingRuns, userRefIdToAnonymize);
        TrainingInstanceStatistics trainingInstanceStatistics = new TrainingInstanceStatistics();
        // Must be before getFinalResultsField() because of statistics
        List<ClusteringLevelDTO> levelsField = new ArrayList<>();
        for (AbstractLevel abstractLevel : trainingInstanceData.levels) {
            levelsField.add(mapToLevelResultDTO(abstractLevel, trainingInstanceData, trainingInstanceStatistics));
        }
        GameResultsDTO finalResultsField = mapToFinalResultsDTO(trainingInstanceData, trainingInstanceStatistics);
        return new ClusteringVisualizationDTO(finalResultsField, levelsField);
    }

    /**
     * Gather all the necessary information for organizer to display table visualizations.
     *
     * @param trainingInstanceId id of training instance.
     * @return data for table visualizations.
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
    @TransactionalRO
    public List<PlayerDataDTO> getTableVisualizationsForOrganizer(Long trainingInstanceId) {
        return getTableVisualizations(trainingInstanceId, null);
    }

    /**
     * Gather all the necessary information for trainee to display table visualizations.
     *
     * @param trainingRunId id of training run.
     * @return data for table visualizations.
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
    @TransactionalWO
    public List<PlayerDataDTO> getTableVisualizationsForTrainee(Long trainingRunId) {
        TrainingRun trainingRun = trainingRunService.findByIdWithLevel(trainingRunId);
        return getTableVisualizations(trainingRun.getTrainingInstance().getId(), trainingRun.getParticipantRef().getUserRefId());
    }

    private List<PlayerDataDTO> getTableVisualizations(Long trainingInstanceId, Long userRefIdToAnonymize) {
        TrainingInstanceData trainingInstanceData = getTrainingInstanceData(trainingInstanceId,
                elasticsearchApiService::getAggregatedEventsByTrainingRunsAndLevels, this::retrieveRunIdsFromEventsAggregatedByRunsAndLevels, userRefIdToAnonymize);

        return trainingInstanceData.events.entrySet().stream().map(runEvents -> {
            AbstractAuditPOJO lastLevelEvent = null;
            UserRefDTO participantInfo = trainingInstanceData.participantsByTrainingRuns.get(runEvents.getKey());
            TablePlayerDTO tablePlayerDataDTO = new TablePlayerDTO(participantInfo, runEvents.getKey());

            for (AbstractLevel abstractLevel : trainingInstanceData.levels) {
                List<AbstractAuditPOJO> levelEvents = runEvents.getValue().get(abstractLevel.getId());
                if (levelEvents == null) {
                    break;
                }
                lastLevelEvent = levelEvents.get(levelEvents.size() - 1);
                tablePlayerDataDTO.addTableLevel(mapToTableLevelDTO(abstractLevel.getId(), abstractLevel.getOrder(), levelEvents));
            }
            tablePlayerDataDTO.setGameScore(lastLevelEvent == null ? 0 : lastLevelEvent.getTotalGameScore());
            tablePlayerDataDTO.setAssessmentScore(lastLevelEvent == null ? 0 : lastLevelEvent.getTotalAssessmentScore());
            tablePlayerDataDTO.setFinished(lastLevelEvent instanceof TrainingRunEnded);
            tablePlayerDataDTO.setTrainingTime(lastLevelEvent == null ? 0 : lastLevelEvent.getGameTime());
            return tablePlayerDataDTO;
        }).collect(Collectors.toList());
    }

    /**
     * Gather all the necessary information about the training instance to display table visualizations.
     *
     * @param trainingInstanceId id of Training Instance to gets info.
     * @return data for table visualizations.
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
    @TransactionalRO
    public List<LevelTabsLevelDTO> getLevelTabsVisualizations(Long trainingInstanceId) {
        TrainingInstanceData trainingInstanceData = getTrainingInstanceData(trainingInstanceId, elasticsearchApiService::getAggregatedEventsByLevelsAndTrainingRuns,
                this::retrieveRunsIdsFromEventsAggregatedByLevelsAndTrainingRuns, null);
        List<LevelTabsLevelDTO> levelTabsData = new ArrayList<>();
        for (AbstractLevel level : trainingInstanceData.levels) {
            levelTabsData.add(mapToLevelTabsLevelDTO(level, trainingInstanceData));
        }
        return levelTabsData;
    }

    /**
     * Gather all the necessary information for organizer to display timeline visualizations.
     *
     * @param trainingInstanceId id of Training Instance to gets info.
     * @return data for clustering visualizations.
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingInstance(#trainingInstanceId)")
    @TransactionalRO
    public TimelineDTO getTimelineVisualizationsForTrainingInstance(Long trainingInstanceId) {
        return getTimelineVisualizations(trainingInstanceId, null);
    }

    /**
     * Gather all the necessary information for trainee to display timeline visualizations.
     *
     * @param trainingRunId id of training run.
     * @return data for clustering visualizations.
     */
    @PreAuthorize("hasAuthority(T(cz.muni.ics.kypo.training.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isTraineeOfGivenTrainingRun(#trainingRunId)")
    @TransactionalWO
    public TimelineDTO getTimelineVisualizationsForTrainee(Long trainingRunId) {
        TrainingRun trainingRun = trainingRunService.findByIdWithLevel(trainingRunId);
        return getTimelineVisualizations(trainingRun.getTrainingInstance().getId(), trainingRun.getParticipantRef().getUserRefId());
    }

    private TimelineDTO getTimelineVisualizations(Long trainingInstanceId, Long userRefIdToAnonymize) {
        TrainingInstanceData trainingInstanceData = getTrainingInstanceData(trainingInstanceId, elasticsearchApiService::getAggregatedEventsByTrainingRunsAndLevels,
                this::retrieveRunIdsFromEventsAggregatedByRunsAndLevels, userRefIdToAnonymize);

        TimelineDTO timelineDTO = new TimelineDTO();
        timelineDTO.setEstimatedTime(TimeUnit.MINUTES.toMillis(trainingInstanceData.trainingDefinition.getEstimatedDuration()));
        timelineDTO.setMaxScoreOfLevels(trainingInstanceData.levels.stream()
                .map(AbstractLevel::getMaxScore)
                .collect(Collectors.toList()));

        TrainingInstanceStatistics trainingInstanceStatistics = new TrainingInstanceStatistics();
        for (Map.Entry<Long, Map<Long, List<AbstractAuditPOJO>>> runEvents : trainingInstanceData.events.entrySet()) {
            timelineDTO.addPlayerData(mapToTimelinePlayerDataDTO(trainingInstanceData.participantsByTrainingRuns.get(runEvents.getKey()),
                    trainingInstanceData.levels, runEvents, trainingInstanceStatistics));
        }
        timelineDTO.setAverageTime(trainingInstanceStatistics.getAverageTime());
        timelineDTO.setMaxParticipantTime(trainingInstanceStatistics.getMaxTime());
        return timelineDTO;
    }

    // Private methods
    private PlayerDataDTO mapToTimelinePlayerDataDTO(UserRefDTO player,
                                                     List<AbstractLevel> levels,
                                                     Map.Entry<Long, Map<Long, List<AbstractAuditPOJO>>> runEvents,
                                                     TrainingInstanceStatistics trainingInstanceStatistics) {
        AbstractAuditPOJO firstEvent = runEvents.getValue().get(levels.get(0).getId()).get(0);
        ProcessedLevelsData processedLevelsData = getProcessedLevelsData(levels, runEvents);
        if (processedLevelsData.lastLevelEvent instanceof TrainingRunEnded) {
            trainingInstanceStatistics.addFinishedTrainingRun((TrainingRunEnded) processedLevelsData.lastLevelEvent);
        }
        int gameScore = processedLevelsData.lastLevelEvent.getTotalGameScore();
        int assessmentScore = processedLevelsData.lastLevelEvent.getTotalAssessmentScore();

        TimelinePlayerDTO timelinePlayerDTO = new TimelinePlayerDTO(player, firstEvent.getTrainingRunId(), gameScore, assessmentScore);
        timelinePlayerDTO.setTrainingTime(processedLevelsData.lastLevelEvent.getGameTime() - firstEvent.getGameTime());
        timelinePlayerDTO.setLevels(processedLevelsData.timelineLevels);
        return timelinePlayerDTO;
    }

    private LevelTabsLevelDTO mapToLevelTabsLevelDTO(AbstractLevel level,
                                                     TrainingInstanceData trainingInstanceData) {
        LevelTabsLevelDTO.LevelTabsLevelBuilder levelTabsLevelBuilder = new LevelTabsLevelDTO.LevelTabsLevelBuilder()
                .id(level.getId())
                .title(level.getTitle())
                .estimatedTime(TimeUnit.MINUTES.toMillis(level.getEstimatedDuration()))
                .order(level.getOrder())
                .maxLevelScore(level.getMaxScore());
        if (level instanceof GameLevel) {
            levelTabsLevelBuilder
                    .levelType(cz.muni.ics.kypo.training.api.enums.LevelType.GAME_LEVEL)
                    .content(((GameLevel) level).getContent())
                    .correctFlag(((GameLevel) level).getFlag())
                    .players(mapToLevelTabsPlayerDTOs(trainingInstanceData.events, level))
                    .hints(((GameLevel) level).getHints().stream()
                            .map(hint -> new LevelTabsHintDTO(hint.getId(), hint.getOrder(), hint.getTitle(), hint.getHintPenalty()))
                            .collect(Collectors.toList()));
        } else if (level instanceof InfoLevel) {
            levelTabsLevelBuilder
                    .levelType(cz.muni.ics.kypo.training.api.enums.LevelType.INFO_LEVEL)
                    .content(((InfoLevel) level).getContent());
        } else {
            levelTabsLevelBuilder
                    .levelType(cz.muni.ics.kypo.training.api.enums.LevelType.ASSESSMENT_LEVEL)
                    .assessmentType(AssessmentType.valueOf(((AssessmentLevel) level).getAssessmentType().name()));
        }
        return levelTabsLevelBuilder.build();
    }

    private List<LevelTabsPlayerDTO> mapToLevelTabsPlayerDTOs(Map<Long, Map<Long, List<AbstractAuditPOJO>>> events,
                                                              AbstractLevel level) {
        List<LevelTabsPlayerDTO> players = new ArrayList<>();
        for (Map.Entry<Long, List<AbstractAuditPOJO>> runEvents : events.getOrDefault(level.getId(), Collections.emptyMap()).entrySet()) {
            AbstractAuditPOJO firstEvent = runEvents.getValue().get(0);
            AbstractAuditPOJO lastEvent = runEvents.getValue().get(runEvents.getValue().size() - 1);

            LevelTabsPlayerDTO levelTabPlayerDTO = new LevelTabsPlayerDTO();
            levelTabPlayerDTO.setId(firstEvent.getUserRefId());
            levelTabPlayerDTO.setTrainingRunId(firstEvent.getTrainingRunId());
            levelTabPlayerDTO.setDisplayedSolution(false);
            levelTabPlayerDTO.setTime(lastEvent.getGameTime() - firstEvent.getGameTime());
            levelTabPlayerDTO.setParticipantLevelScore(lastEvent.getActualScoreInLevel());
            for (AbstractAuditPOJO userLevelEvent : runEvents.getValue()) {
                if (userLevelEvent instanceof SolutionDisplayed) {
                    levelTabPlayerDTO.setDisplayedSolution(true);
                } else if (userLevelEvent instanceof HintTaken) {
                    levelTabPlayerDTO.addHint();
                } else if (userLevelEvent instanceof WrongFlagSubmitted) {
                    levelTabPlayerDTO.addWrongFlag(((WrongFlagSubmitted) userLevelEvent).getFlagContent());
                }
            }
            players.add(levelTabPlayerDTO);
        }
        return players;

    }

    private TableLevelDTO mapToTableLevelDTO(Long levelId, int levelOrder, List<AbstractAuditPOJO> levelEvents) {
        TableLevelDTO.TableLevelBuilder tableLevelBuilder = new TableLevelDTO.TableLevelBuilder()
                .id(levelId)
                .order(levelOrder);
        int levelStartedEventIndex = levelEvents.get(0) instanceof LevelStarted ? 0 : 1;

        if (((LevelStarted) levelEvents.get(levelStartedEventIndex)).getLevelType() == cz.muni.csirt.kypo.events.trainings.enums.LevelType.GAME) {
            tableLevelBuilder.levelType(cz.muni.ics.kypo.training.api.enums.LevelType.GAME_LEVEL);
            countHintsTakenAndWrongFlags(tableLevelBuilder, levelEvents);
        } else if (((LevelStarted) levelEvents.get(levelStartedEventIndex)).getLevelType() == cz.muni.csirt.kypo.events.trainings.enums.LevelType.ASSESSMENT) {
            tableLevelBuilder.levelType(cz.muni.ics.kypo.training.api.enums.LevelType.ASSESSMENT_LEVEL);
        } else if (((LevelStarted) levelEvents.get(levelStartedEventIndex)).getLevelType() == cz.muni.csirt.kypo.events.trainings.enums.LevelType.INFO) {
            tableLevelBuilder.levelType(cz.muni.ics.kypo.training.api.enums.LevelType.INFO_LEVEL);
        }
        tableLevelBuilder.score(levelEvents.get(levelEvents.size() - 1).getActualScoreInLevel());
        return tableLevelBuilder.build();

    }

    private ClusteringLevelDTO mapToLevelResultDTO(AbstractLevel abstractLevel,
                                                   TrainingInstanceData trainingInstanceData,
                                                   TrainingInstanceStatistics trainingInstanceStatistics) {
        ClusteringLevelDTO.ClusteringLevelBuilder clusteringLevelBuilder = new ClusteringLevelDTO.ClusteringLevelBuilder()
                .id(abstractLevel.getId())
                .title(abstractLevel.getTitle())
                .order(abstractLevel.getOrder())
                .estimatedTime(TimeUnit.MINUTES.toMillis(abstractLevel.getEstimatedDuration()));
        if (abstractLevel instanceof GameLevel) {
            clusteringLevelBuilder.levelType(cz.muni.ics.kypo.training.api.enums.LevelType.GAME_LEVEL);
        } else if (abstractLevel instanceof InfoLevel) {
            clusteringLevelBuilder.levelType(cz.muni.ics.kypo.training.api.enums.LevelType.INFO_LEVEL);
        } else if (abstractLevel instanceof AssessmentLevel) {
            clusteringLevelBuilder.levelType(cz.muni.ics.kypo.training.api.enums.LevelType.ASSESSMENT_LEVEL);
        }
        LevelStatistics levelStatistics = new LevelStatistics();
        Map<Long, List<AbstractAuditPOJO>> levelEventsOfTrainingRuns = trainingInstanceData.events.getOrDefault(abstractLevel.getId(), Collections.emptyMap());
        for (Map.Entry<Long, List<AbstractAuditPOJO>> trainingRunLevelEvents : levelEventsOfTrainingRuns.entrySet()) {
            AbstractAuditPOJO firstLevelEvent = trainingRunLevelEvents.getValue().get(0);
            AbstractAuditPOJO lastLevelEvent = trainingRunLevelEvents.getValue().get(trainingRunLevelEvents.getValue().size() - 1);
            UserRefDTO participantInfo = trainingInstanceData.participantsByTrainingRuns.get(trainingRunLevelEvents.getKey());
            ClusteringLevelPlayerDTO playerDataDTOForLevel = new ClusteringLevelPlayerDTO(participantInfo, trainingRunLevelEvents.getKey(),
                    lastLevelEvent.getGameTime() - firstLevelEvent.getGameTime(), lastLevelEvent.getActualScoreInLevel(), lastLevelEvent instanceof LevelCompleted);
            if (lastLevelEvent instanceof LevelCompleted || lastLevelEvent instanceof TrainingRunEnded) {
                levelStatistics.updateStatistics(playerDataDTOForLevel.getTrainingTime(), playerDataDTOForLevel.getParticipantLevelScore());

                if (lastLevelEvent instanceof TrainingRunEnded) {
                    trainingInstanceStatistics.addFinishedTrainingRun((TrainingRunEnded) lastLevelEvent);
                }
            }
            trainingInstanceStatistics.lastEventsOfTrainingRuns.put(playerDataDTOForLevel.getTrainingRunId(), lastLevelEvent);
            clusteringLevelBuilder.addPlayerData(playerDataDTOForLevel);
        }
        clusteringLevelBuilder.averageScore(levelStatistics.getAverageScore())
                .averageTime(levelStatistics.getAverageTime())
                .maxParticipantScore(levelStatistics.getMaxScore())
                .maxParticipantTime(levelStatistics.getMaxTime());
        return clusteringLevelBuilder.build();
    }

    private void countHintsTakenAndWrongFlags(TableLevelDTO.TableLevelBuilder builder, List<AbstractAuditPOJO> levelEvents) {
        int wrongFlags = 0;
        int hintsTaken = 0;
        for (AbstractAuditPOJO levelEvent : levelEvents) {
            if (levelEvent instanceof WrongFlagSubmitted) {
                wrongFlags++;
            }
            if (levelEvent instanceof HintTaken) {
                hintsTaken++;
            }
        }
        builder.hintsTaken(hintsTaken)
                .wrongFlags(wrongFlags);
    }

    private GameResultsDTO mapToFinalResultsDTO(TrainingInstanceData trainingInstanceData,
                                                TrainingInstanceStatistics trainingInstanceStatistics) {
        GameResultsDTO finalResults = new GameResultsDTO();
        finalResults.setEstimatedTime(TimeUnit.MINUTES.toMillis(trainingInstanceData.trainingDefinition.getEstimatedDuration()));
        for (Map.Entry<Long, AbstractAuditPOJO> lastEventOfTrainingRun : trainingInstanceStatistics.lastEventsOfTrainingRuns.entrySet()) {
            UserRefDTO participantInfo = trainingInstanceData.participantsByTrainingRuns.get(lastEventOfTrainingRun.getKey());
            finalResults.addPlayerData(new GameResultsPlayerDTO(participantInfo, lastEventOfTrainingRun.getKey(), lastEventOfTrainingRun.getValue().getGameTime(),
                    lastEventOfTrainingRun.getValue().getTotalGameScore(), lastEventOfTrainingRun.getValue().getTotalAssessmentScore(), lastEventOfTrainingRun.getValue() instanceof TrainingRunEnded));
        }
        finalResults.setAverageScore(trainingInstanceStatistics.getAverageScore());
        finalResults.setAverageGameScore(trainingInstanceStatistics.getAverageGameScore());
        finalResults.setAverageAssessmentScore(trainingInstanceStatistics.getAverageAssessmentScore());
        finalResults.setMaxParticipantScore(trainingInstanceStatistics.getMaxScore());
        finalResults.setMaxParticipantGameScore(trainingInstanceStatistics.getMaxGameScore());
        finalResults.setMaxParticipantAssessmentScore(trainingInstanceStatistics.getMaxAssessmentScore());
        finalResults.setAverageTime(trainingInstanceStatistics.getAverageTime());
        finalResults.setMaxParticipantTime(trainingInstanceStatistics.getMaxTime());
        return finalResults;
    }

    private TrainingInstanceData getTrainingInstanceData(Long trainingInstanceId,
                                                         Function<TrainingInstance, Map<Long, Map<Long, List<AbstractAuditPOJO>>>> aggregatedEventsFunction,
                                                         Function<Map<Long, Map<Long, List<AbstractAuditPOJO>>>, Set<Long>> trainingRunsIdsRetrieveFunction,
                                                         @Nullable Long userRefIdToAnonymize) {
        TrainingInstanceData trainingInstanceData = new TrainingInstanceData();
        trainingInstanceData.trainingInstance = trainingInstanceService.findById(trainingInstanceId);
        trainingInstanceData.trainingDefinition = trainingInstanceData.trainingInstance.getTrainingDefinition();
        trainingInstanceData.levels = trainingDefinitionService.findAllLevelsFromDefinition(trainingInstanceData.trainingInstance.getTrainingDefinition().getId());
        trainingInstanceData.events = aggregatedEventsFunction.apply(trainingInstanceData.trainingInstance);
        Set<Long> trainingRunsIdsFromEvents = trainingRunsIdsRetrieveFunction.apply(trainingInstanceData.events);
        Set<TrainingRun> trainingRuns = trainingRunService.findAllByTrainingInstanceId(trainingInstanceId)
                .stream()
                .filter(trainingRun -> trainingRunsIdsFromEvents.contains(trainingRun.getId()))
                .collect(Collectors.toSet());
        Set<Long> participantsIds = trainingRuns.stream()
                .map(trainingRun -> trainingRun.getParticipantRef().getUserRefId())
                .collect(Collectors.toSet());
        Map<Long, UserRefDTO> participantsByIds = userService.getUsersRefDTOByGivenUserIds(participantsIds, PageRequest.of(0, 999), null, null)
                .getContent()
                .stream()
                .collect(Collectors.toMap(UserRefDTO::getUserRefId, Function.identity()));

        if (userRefIdToAnonymize != null && !participantsByIds.isEmpty()) {
            participantsByIds.values().forEach(userRefDTO -> this.anonymizeTheUser(userRefDTO, "other player"));
            this.anonymizeTheUser(participantsByIds.get(userRefIdToAnonymize), "you");
        }
        trainingInstanceData.participantsByTrainingRuns = trainingRuns.stream()
                .collect(Collectors.toMap(TrainingRun::getId, trainingRun -> participantsByIds.get(trainingRun.getParticipantRef().getUserRefId())));
        return trainingInstanceData;
    }

    private void anonymizeTheUser(UserRefDTO participant, String anonymizedName) {
        participant.setUserRefFamilyName(anonymizedName);
        participant.setUserRefFullName(anonymizedName);
        participant.setUserRefGivenName(anonymizedName);
        participant.setUserRefSub(anonymizedName);
    }

    private Set<Long> retrieveRunsIdsFromEventsAggregatedByLevelsAndTrainingRuns(Map<Long, Map<Long, List<AbstractAuditPOJO>>> events) {
        return events.values()
                .stream()
                .flatMap(v -> v.keySet().stream())
                .collect(Collectors.toSet());
    }

    private Set<Long> retrieveRunIdsFromEventsAggregatedByRunsAndLevels(Map<Long, Map<Long, List<AbstractAuditPOJO>>> events) {
        return events.keySet();
    }

    private ProcessedLevelsData getProcessedLevelsData(List<AbstractLevel> levels, Map.Entry<Long, Map<Long, List<AbstractAuditPOJO>>> userEvents) {
        ProcessedLevelsData processedLevelData = new ProcessedLevelsData();
        for (AbstractLevel abstractLevel : levels) {
            List<AbstractAuditPOJO> levelEvents = userEvents.getValue().get(abstractLevel.getId());
            if (levelEvents == null) {
                break;
            }
            ProcessedEventsData processedEventsData = getProcessedEventsData(abstractLevel.getOrder(), levelEvents);
            TimelineLevelDTO.TimelineLevelBuilder timelineLevelBuilder = new TimelineLevelDTO.TimelineLevelBuilder()
                    .id(abstractLevel.getId())
                    .order(abstractLevel.getOrder())
                    .startTime(levelEvents.get(0).getGameTime())
                    .events(processedEventsData.timelineEvents)
                    .participantScore(processedEventsData.lastLevelEvent.getActualScoreInLevel());

            if (abstractLevel instanceof GameLevel) {
                timelineLevelBuilder.levelType(cz.muni.ics.kypo.training.api.enums.LevelType.GAME_LEVEL)
                        .order(abstractLevel.getOrder())
                        .correctFlagTime(processedEventsData.correctFlagTime)
                        .solutionDisplayedTime(processedEventsData.solutionDisplayedTime);
            } else if (abstractLevel instanceof InfoLevel) {
                timelineLevelBuilder.levelType(cz.muni.ics.kypo.training.api.enums.LevelType.INFO_LEVEL);
            } else {
                timelineLevelBuilder.levelType(cz.muni.ics.kypo.training.api.enums.LevelType.ASSESSMENT_LEVEL)
                        .assessmentType(AssessmentType.valueOf(((AssessmentLevel) abstractLevel).getAssessmentType().name()));
            }
            processedLevelData.lastLevelEvent = processedEventsData.lastLevelEvent;
            processedLevelData.timelineLevels.add(timelineLevelBuilder.build());
        }
        return processedLevelData;
    }

    private ProcessedEventsData getProcessedEventsData(int levelOrder, List<AbstractAuditPOJO> levelEvents) {
        ProcessedEventsData processedEventsData = new ProcessedEventsData();
        for (AbstractAuditPOJO levelEvent : levelEvents) {
            int score = levelEvent.getTotalGameScore() + levelEvent.getTotalAssessmentScore() + levelEvent.getActualScoreInLevel();
            EventDTO eventDTO = new EventDTO(levelEvent.getGameTime());
            if (levelEvent instanceof TrainingRunStarted) {
                eventDTO.setText("Training run " + levelEvent.getTrainingRunId() + " started.");
            } else if (levelEvent instanceof LevelStarted) {
                eventDTO.setText("Level " + levelOrder + " started.");
            } else if (levelEvent instanceof HintTaken) {
                eventDTO.setText("Hint '" + ((HintTaken) levelEvent).getHintTitle() + "' taken.");
            } else if (levelEvent instanceof WrongFlagSubmitted) {
                eventDTO.setText("Wrong flag submitted.");
            } else if (levelEvent instanceof CorrectFlagSubmitted) {
                score -= levelEvent.getActualScoreInLevel();
                processedEventsData.correctFlagTime = levelEvent.getGameTime();
                eventDTO.setText("Correct flag submitted.");
            } else if (levelEvent instanceof SolutionDisplayed) {
                score -= levelEvent.getActualScoreInLevel();
                eventDTO.setText("Solution displayed.");
                processedEventsData.solutionDisplayedTime = levelEvent.getGameTime();
            } else if (levelEvent instanceof LevelCompleted) {
                score -= levelEvent.getActualScoreInLevel();
                eventDTO.setText("Level " + levelOrder + " completed.");
            } else if (levelEvent instanceof TrainingRunEnded) {
                score -= levelEvent.getActualScoreInLevel();
                eventDTO.setText("Training run " + levelEvent.getTrainingRunId() + " ended.");
            } else if (levelEvent instanceof TrainingRunResumed) {
                eventDTO.setText("Training run " + levelEvent.getTrainingRunId() + " resumed.");
            } else if (levelEvent instanceof TrainingRunSurrendered) {
                eventDTO.setText("Training run " + levelEvent.getTrainingRunId() + " surrendered.");
            } else if (levelEvent instanceof AssessmentAnswers) {
                score -= levelEvent.getActualScoreInLevel();
                eventDTO.setText("Assessment answered.");
            }
            eventDTO.setScore(score);
            processedEventsData.timelineEvents.add(eventDTO);
            processedEventsData.lastLevelEvent = levelEvent;
        }
        return processedEventsData;
    }

    // Inner classes
    private class TrainingInstanceStatistics {
        float sumOfTrainingRunsTime;
        float sumOfTrainingRunsGameScore;
        float sumOfTrainingRunsAssessmentScore;
        int numOfFinishedTrainingRuns;
        long maxTime;
        int maxScore;
        int maxGameScore;
        int maxAssessmentScore;
        Map<Long, AbstractAuditPOJO> lastEventsOfTrainingRuns = new HashMap<>();

        public void addFinishedTrainingRun(TrainingRunEnded lastLevelEvent) {
            this.sumOfTrainingRunsTime += lastLevelEvent.getGameTime();
            this.sumOfTrainingRunsGameScore += lastLevelEvent.getTotalGameScore();
            this.sumOfTrainingRunsAssessmentScore += lastLevelEvent.getTotalAssessmentScore();
            this.checkAndSetMaxTime(lastLevelEvent.getGameTime());
            this.checkAndSetMaxScore(lastLevelEvent.getTotalGameScore() + lastLevelEvent.getTotalAssessmentScore());
            this.checkAndSetGameMaxScore(lastLevelEvent.getTotalGameScore());
            this.checkAndSetAssessmentMaxScore(lastLevelEvent.getTotalAssessmentScore());
            this.numOfFinishedTrainingRuns++;
        }

        public void checkAndSetMaxTime(long maxTime) {
            if (maxTime > this.maxTime) {
                this.maxTime = maxTime;
            }
        }

        private void checkAndSetMaxScore(int maxScore) {
            if (maxScore > this.maxScore) {
                this.maxScore = maxScore;
            }
        }

        private void checkAndSetGameMaxScore(int maxGameScore) {
            if (maxGameScore > this.maxGameScore) {
                this.maxGameScore = maxGameScore;
            }
        }

        private void checkAndSetAssessmentMaxScore(int maxAssessmentScore) {
            if (maxAssessmentScore > this.maxAssessmentScore) {
                this.maxAssessmentScore = maxAssessmentScore;
            }
        }

        public long getMaxTime() {
            return maxTime;
        }

        public int getMaxScore() {
            return maxScore;
        }

        public int getMaxGameScore() {
            return maxGameScore;
        }

        public int getMaxAssessmentScore() {
            return maxAssessmentScore;
        }

        public float getAverageTime() {
            return sumOfTrainingRunsTime == 0 ? 0 : sumOfTrainingRunsTime / numOfFinishedTrainingRuns;
        }

        public float getAverageScore() {
            return (sumOfTrainingRunsGameScore + sumOfTrainingRunsAssessmentScore) == 0 ? 0 : (sumOfTrainingRunsGameScore + sumOfTrainingRunsAssessmentScore) / numOfFinishedTrainingRuns;
        }

        public float getAverageGameScore() {
            return sumOfTrainingRunsGameScore == 0 ? 0 : sumOfTrainingRunsGameScore / numOfFinishedTrainingRuns;
        }

        public float getAverageAssessmentScore() {
            return sumOfTrainingRunsAssessmentScore == 0 ? 0 : sumOfTrainingRunsAssessmentScore / numOfFinishedTrainingRuns;
        }
    }

    private class LevelStatistics {
        float sumOfLevelsTime;
        float sumOfLevelsScore;
        long maxTime;
        int maxScore;
        int numOfParticipants;

        public void updateStatistics(long trainingTime, int levelScore) {
            this.sumOfLevelsTime += trainingTime;
            this.sumOfLevelsScore += levelScore;
            this.numOfParticipants++;
            this.checkAndSetMaxTime(trainingTime);
            this.checkAndSetMaxScore(levelScore);
        }

        private void checkAndSetMaxTime(long maxTime) {
            if (maxTime > this.maxTime) {
                this.maxTime = maxTime;
            }
        }

        private void checkAndSetMaxScore(int maxScore) {
            if (maxScore > this.maxScore) {
                this.maxScore = maxScore;
            }
        }

        public long getMaxTime() {
            return maxTime;
        }

        public int getMaxScore() {
            return maxScore;
        }

        public float getAverageTime() {
            return sumOfLevelsTime == 0 ? 0 : sumOfLevelsTime / numOfParticipants;
        }

        public float getAverageScore() {
            return sumOfLevelsScore == 0 ? 0 : sumOfLevelsScore / numOfParticipants;
        }
    }

    private class ProcessedLevelsData {
        private final List<VisualizationAbstractLevelDTO> timelineLevels = new ArrayList<>();
        private AbstractAuditPOJO lastLevelEvent;
    }

    private class ProcessedEventsData {
        private final List<EventDTO> timelineEvents = new ArrayList<>();
        private long solutionDisplayedTime;
        private long correctFlagTime;
        private AbstractAuditPOJO lastLevelEvent;
    }

    private class TrainingInstanceData {
        TrainingDefinition trainingDefinition;
        TrainingInstance trainingInstance;
        List<AbstractLevel> levels;
        Map<Long, Map<Long, List<AbstractAuditPOJO>>> events;
        Map<Long, UserRefDTO> participantsByTrainingRuns;
    }
}
