package cz.cyberrange.platform.training.service.facade;

import cz.cyberrange.platform.training.api.dto.run.AccessTrainingRunDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.lobby.team.TeamRunInfoDTO;
import cz.cyberrange.platform.training.api.dto.traininginstance.lobby.team.TeamScoreDTO;
import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.training.api.exceptions.ResourceNotFoundException;
import cz.cyberrange.platform.training.api.exceptions.ResourceNotReadyException;
import cz.cyberrange.platform.training.persistence.model.HintInfo;
import cz.cyberrange.platform.training.persistence.model.SolutionInfo;
import cz.cyberrange.platform.training.persistence.model.Team;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.model.enums.TrainingType;
import cz.cyberrange.platform.training.persistence.repository.TeamRunLockRepository;
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

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The type Training run facade.
 */
@Service
public class CoopTrainingRunFacade extends TrainingRunFacade {

    private static final Logger LOG = LoggerFactory.getLogger(CoopTrainingRunFacade.class);
    private final TrainingInstanceLobbyService trainingInstanceLobbyService;
    private final CoopTrainingRunService coopTrainingRunService;
    private final ScoreboardService scoreboardService;
    private final TeamRunLockRepository teamRunLockRepository;

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
            ScoreboardService scoreboardService,
            TeamRunLockRepository teamRunLockRepository) {
        super(trainingRunService, trainingDefinitionService, answersStorageApiService, securityService, userService, trainingFeedbackApiService, trainingRunMapper, levelMapper, hintMapper);
        this.trainingInstanceLobbyService = trainingInstanceLobbyService;
        this.coopTrainingRunService = coopTrainingRunService;
        this.scoreboardService = scoreboardService;
        this.teamRunLockRepository = teamRunLockRepository;
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

        if (trainingRunService.findRunningTrainingRunOfUser(accessToken, participantRefId).isPresent() || trainingInstanceLobbyService.isInLockedTeam(trainingInstance.getId(), participantRefId)) {
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
    public AccessTrainingRunDTO accessTrainingRun(String accessToken) {
        TrainingInstance trainingInstance = coopTrainingRunService.getTrainingInstanceForParticularAccessToken(accessToken);
        // checking if the user is not accessing to his existing training run (resume action)
        Long participantRefId = securityService.getUserRefIdFromUserAndGroup();
        UserRef userRef = userService.createOrGetUserRef(participantRefId);

        if (trainingInstance.notStarted()) {
            throw new ResourceNotReadyException(new EntityErrorDetail("The training instance has not started yet."));
        }

        Optional<TrainingRun> accessedTrainingRun = coopTrainingRunService.findRunningTrainingRunOfUser(accessToken, participantRefId);
        if (accessedTrainingRun.isPresent()) {
            TrainingRun trainingRun = coopTrainingRunService.resumeTrainingRun(accessedTrainingRun.get().getId());
            return convertToAccessTrainingRunDTO(trainingRun);
        }
        // Check if the user already clicked access training run, in that case, it returns an exception (it prevents concurrent accesses).
        coopTrainingRunService.trAcquisitionLockToPreventManyRequestsFromSameUser(participantRefId, trainingInstance.getId(), accessToken);
        try {
            TrainingRun trainingRun;
            try {
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

    @PreAuthorize("hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" + "or @securityService.isTraineeOfGivenTrainingInstance(#instanceId)")
    @TransactionalRO
    public TeamRunInfoDTO getCoopRunInfo(Long instanceId) {
        TeamRunInfoDTO teamRunInfoDTO = new TeamRunInfoDTO();
        UserRef currentUser = userService.getUserByUserRefId(securityService.getUserRefIdFromUserAndGroup());

        Team team = currentUser.getTeams().stream().filter(searchedTeam -> Objects.equals(searchedTeam.getTrainingInstance().getId(), instanceId)).findFirst().orElseThrow(() -> new ResourceNotFoundException("No team found in the current instance"));

        teamRunInfoDTO.setCurrentLevels(coopTrainingRunService.getTeamRuns(team).stream().collect(Collectors.toMap((run) -> run.getLinearRunOwner().getId(), (run) -> run.getCurrentLevel().getId())));
        teamRunInfoDTO.setUsedHints(coopTrainingRunService.getAllHintsTakenByTeam(team).stream().map(HintInfo::getHintId).collect(Collectors.toSet()));

        teamRunInfoDTO.setShownSolutions(coopTrainingRunService.getAllSolutionsTakenByTeam(team).stream().map(SolutionInfo::getTrainingLevelId).collect(Collectors.toSet()));


        return teamRunInfoDTO;
    }

    @PreAuthorize("hasAuthority(T(cz.cyberrange.platform.training.service.enums.RoleTypeSecurity).ROLE_TRAINING_ADMINISTRATOR)" + "or @securityService.isTraineeOfGivenTrainingInstance(#instanceId)")
    @TransactionalRO
    public Map<Long, TeamScoreDTO> getScoreboard(Long instanceId) {
        return scoreboardService.getScoreboard(instanceId);
    }
}
