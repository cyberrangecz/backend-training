package cz.muni.ics.kypo.training.facade;

import cz.muni.ics.kypo.training.annotations.security.IsDesignerOrOrganizerOrAdmin;
import cz.muni.ics.kypo.training.annotations.security.IsTraineeOrAdmin;
import cz.muni.ics.kypo.training.annotations.transactions.TransactionalRO;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.visualization.*;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.mapping.mapstruct.HintMapper;
import cz.muni.ics.kypo.training.mapping.mapstruct.LevelMapper;
import cz.muni.ics.kypo.training.persistence.model.*;
import cz.muni.ics.kypo.training.service.TrainingInstanceService;
import cz.muni.ics.kypo.training.service.TrainingRunService;
import cz.muni.ics.kypo.training.service.UserService;
import cz.muni.ics.kypo.training.service.VisualizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    public VisualizationFacade(TrainingRunService trainingRunService, TrainingInstanceService trainingInstanceService,
                               VisualizationService visualizationService, UserService userService, LevelMapper levelMapper) {
        this.trainingRunService = trainingRunService;
        this.trainingInstanceService = trainingInstanceService;
        this.visualizationService = visualizationService;
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
    @TransactionalRO
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

    private List<AbstractLevelVisualizationDTO> convertToAbstractLevelVisualizationDTO(List<AbstractLevel> abstractLevels) {
        List<AbstractLevelVisualizationDTO> visualizationLevelInfoDTOs = new ArrayList<>();
        abstractLevels.forEach(level ->
            visualizationLevelInfoDTOs.add(levelMapper.mapToVisualizationDTO(level)));
        return visualizationLevelInfoDTOs;
    }

}
