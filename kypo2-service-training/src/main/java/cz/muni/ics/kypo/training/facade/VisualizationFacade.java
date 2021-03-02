package cz.muni.ics.kypo.training.facade;

import cz.muni.csirt.kypo.events.AbstractAuditPOJO;
import cz.muni.csirt.kypo.events.trainings.*;
import cz.muni.csirt.kypo.events.trainings.enums.LevelType;
import cz.muni.ics.kypo.training.annotations.security.IsDesignerOrOrganizerOrAdmin;
import cz.muni.ics.kypo.training.annotations.security.IsTraineeOrAdmin;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalWO;
import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.*;
import cz.muni.ics.kypo.training.api.dto.visualization.progress.LevelProgress;
import cz.muni.ics.kypo.training.api.dto.visualization.progress.PlayerProgress;
import cz.muni.ics.kypo.training.api.dto.visualization.progress.VisualizationProgressDTO;
import cz.muni.ics.kypo.training.api.enums.LevelState;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.mapping.mapstruct.LevelMapper;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The type Visualization facade.
 */
@Service
@Transactional
public class VisualizationFacade {

    private static final Logger LOG = LoggerFactory.getLogger(VisualizationFacade.class);

    private TrainingRunService trainingRunService;
    private TrainingInstanceService trainingInstanceService;
    private VisualizationService visualizationService;
    private UserService userService;
    private ElasticsearchApiService elasticsearchApiService;
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
    public VisualizationFacade(TrainingRunService trainingRunService,
                               TrainingInstanceService trainingInstanceService,
                               VisualizationService visualizationService,
                               ElasticsearchApiService elasticsearchApiService,
                               UserService userService,
                               LevelMapper levelMapper) {
        this.trainingRunService = trainingRunService;
        this.trainingInstanceService = trainingInstanceService;
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
    public VisualizationProgressDTO getProgressVisualizationAboutTrainingInstance(Long trainingInstanceId) {
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
        List<AbstractLevelDTO> levels = trainingRunService.getLevels(trainingDefinitionOfTrainingRun.getId()).stream()
                .map(level -> levelMapper.mapToDTO(level))
                .sorted(Comparator.comparingInt(AbstractLevelDTO::getOrder))
                .collect(Collectors.toList());
        visualizationProgressDTO.setLevels(levels);

        Map<Long, Map<Long, List<AbstractAuditPOJO>>> eventsFromElasticsearch = elasticsearchApiService.getAggregatedEventsByUsersAndLevels(trainingInstance);

        List<PlayerProgress> playerProgresses = new ArrayList<>();
        for (Map.Entry<Long, Map<Long, List<AbstractAuditPOJO>>> userEvents : eventsFromElasticsearch.entrySet()) {
            PlayerProgress playerProgress = new PlayerProgress();
            playerProgress.setUserRefId(userEvents.getKey());

            for (Map.Entry<Long, List<AbstractAuditPOJO>> levelEvents: userEvents.getValue().entrySet()) {
                List<AbstractAuditPOJO> events = levelEvents.getValue();
                LevelProgress levelProgress = new LevelProgress();
                levelProgress.setLevelId(levelEvents.getKey());
                levelProgress.setStartTime(events.get(0).getTimestamp());

                if (events.get(events.size() - 1) instanceof LevelCompleted) {
                    levelProgress.setState(LevelState.FINISHED);
                    levelProgress.setEndTime(events.get(events.size() - 1).getTimestamp());
                }

                //Count wrong flags number
                int levelStartedEventIndex = 0;
                if (events.get(0) instanceof TrainingRunStarted) {
                    levelStartedEventIndex = 1;
                }
                if (((LevelStarted) events.get(levelStartedEventIndex)).getLevelType() == LevelType.GAME) {
                    levelProgress.setWrongFlagsNumber(countWrongFlagsNumber(events));
                    levelProgress.setHintsTaken(getTakenHints(events));
                }
                levelProgress.setEvents(events);
                playerProgress.addLevelProgress(levelProgress);
            }
            playerProgresses.add(playerProgress);
        }
        visualizationProgressDTO.setPlayerProgress(playerProgresses);
        return visualizationProgressDTO;
    }

    private int countWrongFlagsNumber(List<AbstractAuditPOJO> events) {
        int counter = 0;
        for (AbstractAuditPOJO event : events) {
            if (event instanceof WrongFlagSubmitted) {
                counter++;
            }
        }
        return counter;
    }

    private List<Long> getTakenHints(List<AbstractAuditPOJO> events) {
        List<Long> takenHints = new ArrayList<>();
        for (AbstractAuditPOJO event : events) {
            if (event instanceof HintTaken) {
                takenHints.add(((HintTaken) event).getHintId());
            }
        }
        return takenHints;
    }

    private List<AbstractLevelVisualizationDTO> convertToAbstractLevelVisualizationDTO(List<AbstractLevel> abstractLevels) {
        List<AbstractLevelVisualizationDTO> visualizationLevelInfoDTOs = new ArrayList<>();
        abstractLevels.forEach(level ->
                visualizationLevelInfoDTOs.add(levelMapper.mapToVisualizationDTO(level)));
        return visualizationLevelInfoDTOs;
    }

}
