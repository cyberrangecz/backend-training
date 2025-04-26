package cz.cyberrange.platform.training.service.facade;

import cz.cyberrange.platform.training.api.dto.run.AccessTrainingRunDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.lobby.team.TeamScoreDTO;
import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.training.api.exceptions.ResourceNotReadyException;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.enums.TrainingType;
import cz.cyberrange.platform.training.service.annotations.security.IsOrganizerOrAdmin;
import cz.cyberrange.platform.training.service.annotations.security.IsTraineeOrAdmin;
import cz.cyberrange.platform.training.service.annotations.transactions.TransactionalRO;
import cz.cyberrange.platform.training.service.mapping.mapstruct.HintMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.LevelMapper;
import cz.cyberrange.platform.training.service.mapping.mapstruct.TrainingRunMapper;
import cz.cyberrange.platform.training.service.services.CoopTrainingRunService;
import cz.cyberrange.platform.training.service.services.ScoreboardService;
import cz.cyberrange.platform.training.service.services.SecurityService;
import cz.cyberrange.platform.training.service.services.TrainingDefinitionService;
import cz.cyberrange.platform.training.service.services.TrainingInstanceLobbyService;
import cz.cyberrange.platform.training.service.services.TrainingRunService;
import cz.cyberrange.platform.training.service.services.UserService;
import cz.cyberrange.platform.training.service.services.api.AnswersStorageApiService;
import cz.cyberrange.platform.training.service.services.api.TrainingFeedbackApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The type Training run facade.
 */
@Service
public class CoopTrainingRunFacade extends TrainingRunFacade {

    private static final Logger LOG = LoggerFactory.getLogger(CoopTrainingRunFacade.class);
    private final TrainingInstanceLobbyService trainingInstanceLobbyService;
    private final CoopTrainingRunService coopTrainingRunService;
    private final ScoreboardService scoreboardService;

    /**
     * Instantiates a new Training run facade.
     *
     * @param trainingRunService the training run service
     * @param securityService    the security service
     * @param userService        the user service
     * @param trainingRunMapper  the training run mapper
     * @param levelMapper        the level mapper
     * @param hintMapper         the hint mapper
     */
    @Autowired
    public CoopTrainingRunFacade(
            TrainingRunService trainingRunService,
            TrainingDefinitionService trainingDefinitionService,
            AnswersStorageApiService answersStorageApiService,
            SecurityService securityService,
            UserService userService,
            TrainingFeedbackApiService trainingFeedbackApiService,
            TrainingRunMapper trainingRunMapper,
            LevelMapper levelMapper,
            HintMapper hintMapper,
            TrainingInstanceLobbyService trainingInstanceLobbyService,
            CoopTrainingRunService coopTrainingRunService,
            ScoreboardService scoreboardService) {
        super(trainingRunService, trainingDefinitionService, answersStorageApiService, securityService, userService, trainingFeedbackApiService, trainingRunMapper, levelMapper, hintMapper);
        this.trainingInstanceLobbyService = trainingInstanceLobbyService;
        this.coopTrainingRunService = coopTrainingRunService;
        this.scoreboardService = scoreboardService;
    }

    /**
     * Check whether the user should wait for the start of his run
     *
     * @param accessToken instance access token
     * @return waiting state
     */
    @IsTraineeOrAdmin
    @Transactional
    public boolean isWaitingForStart(String accessToken) {
        TrainingInstance trainingInstance = trainingRunService.getTrainingInstanceForParticularAccessToken(accessToken);
        Long participantRefId = securityService.getUserRefIdFromUserAndGroup();

        if (trainingInstance.getType() == TrainingType.LINEAR) {
            return trainingInstance.notStarted();
        }

        if (trainingRunService.findRunningTrainingRunOfUser(accessToken, participantRefId).isPresent() ||
                trainingInstanceLobbyService.isInLockedTeam(trainingInstance.getId(), participantRefId)) {
            return false;
        }

        if (!trainingInstanceLobbyService.isWaitingForStart(trainingInstance.getId(), participantRefId, !trainingInstance.notStarted())) {
            trainingInstanceLobbyService.addUserToQueue(trainingInstance.getTrainingInstanceLobby().getTrainingInstance().getId(), participantRefId);
            trainingInstanceLobbyService.updateTrainingInstanceLobby(trainingInstance.getTrainingInstanceLobby());
        }
        return true;
    }

    /**
     * Access Training Run by logged in user based on given accessToken.
     *
     * @param accessToken of one training instance
     * @return {@link AccessTrainingRunDTO} response
     */
    @IsTraineeOrAdmin
    @Transactional
    @Override
    public AccessTrainingRunDTO accessTrainingRun(String accessToken) {
        TrainingInstance trainingInstance = coopTrainingRunService.getTrainingInstanceForParticularAccessToken(accessToken);
        // checking if the user is not accessing to his existing training run (resume action)
        Long participantRefId = securityService.getUserRefIdFromUserAndGroup();
        if (trainingInstance.notStarted()) {
            throw new ResourceNotReadyException(new EntityErrorDetail("The training instance has not started yet."));
        }

        Optional<TrainingRun> accessedTrainingRun = coopTrainingRunService.findRunningTrainingRunOfUser(accessToken, participantRefId);
        LOG.error("Run found: {}", accessedTrainingRun.isPresent());
        if (accessedTrainingRun.isPresent()) {
            TrainingRun trainingRun = coopTrainingRunService.resumeTrainingRun(accessedTrainingRun.get().getId());
            return convertToAccessTrainingRunDTO(trainingRun);
        }
        // Check if the user already clicked access training run, in that case, it returns an exception (it prevents concurrent accesses).
        coopTrainingRunService.trAcquisitionLockToPreventManyRequestsFromSameUser(participantRefId, trainingInstance.getId(), accessToken);
        try {
            TrainingRun trainingRun;
            try {
                // Catch concurrent attempt to create a training run by all team members
                trainingRun = coopTrainingRunService.createTrainingRun(trainingInstance, participantRefId);
            } catch (DataIntegrityViolationException exception) {
                accessedTrainingRun = coopTrainingRunService.findRunningTrainingRunOfUser(accessToken, participantRefId);
                if (accessedTrainingRun.isPresent()) {
                    trainingRun = coopTrainingRunService.resumeTrainingRun(accessedTrainingRun.get().getId());
                    return convertToAccessTrainingRunDTO(trainingRun);
                }
                throw new IllegalStateException("Team run is locked but cannot be found");
            }
            if (!trainingInstance.isLocalEnvironment()) {
                coopTrainingRunService.assignSandbox(trainingRun, trainingInstance.getPoolId());
            }
            coopTrainingRunService.auditTrainingRunStarted(trainingRun);
            return convertToAccessTrainingRunDTO(trainingRun);
        } catch (Exception e) {
            // delete/rollback acquisition lock when no training run either sandbox is assigned
            coopTrainingRunService.deleteTrAcquisitionLockToPreventManyRequestsFromSameUser(participantRefId, trainingInstance.getId());
            throw e;
        }
    }

    /**
     * Delete selected training runs together with teams
     *
     * @param trainingRunIds training runs to delete
     * @param forceDelete    indicates if this training run should be force deleted.
     */
    @IsOrganizerOrAdmin
    @Transactional
    @Override
    public void deleteTrainingRuns(List<Long> trainingRunIds, boolean forceDelete) {
        List<Long> teamsToDelete = trainingRunIds.stream().map(
                        coopTrainingRunService::findById
                ).filter(
                        run -> run.getTrainingInstance().getType() == TrainingType.COOP
                ).map(run -> run.getCoopRunTeam().getId())
                .toList();
        super.deleteTrainingRuns(trainingRunIds, forceDelete);
        teamsToDelete.forEach(trainingInstanceLobbyService::deleteTeam);
    }

    /**
     * Delete selected training run together with team.
     *
     * @param trainingRunId training run to delete
     * @param forceDelete   indicates if this training run should be force deleted.
     */
    @PreAuthorize("hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" +
            "or @securityService.isOrganizerOfGivenTrainingRun(#trainingRunId)")
    @Transactional
    public void deleteTrainingRun(Long trainingRunId, boolean forceDelete) {
        TrainingRun run = this.coopTrainingRunService.findById(trainingRunId);
        super.deleteTrainingRun(trainingRunId, forceDelete);
        if (run.getTrainingInstance().getType() == TrainingType.COOP) {
            this.trainingInstanceLobbyService.deleteTeam(run.getCoopRunTeam().getId());
        }
    }

    @PreAuthorize("hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" + "or @securityService.isTraineeOfGivenTrainingInstance(#instanceId)")
    @TransactionalRO
    public List<TeamScoreDTO> getScoreboard(Long instanceId) {
        return new ArrayList<>(scoreboardService.getScoreboard(instanceId).values());
    }
}
