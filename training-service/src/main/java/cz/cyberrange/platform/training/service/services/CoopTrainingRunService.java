package cz.cyberrange.platform.training.service.services;

import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.persistence.model.Team;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.model.enums.TRState;
import cz.cyberrange.platform.training.persistence.repository.AbstractLevelRepository;
import cz.cyberrange.platform.training.persistence.repository.HintRepository;
import cz.cyberrange.platform.training.persistence.repository.QuestionAnswerRepository;
import cz.cyberrange.platform.training.persistence.repository.SubmissionRepository;
import cz.cyberrange.platform.training.persistence.repository.TRAcquisitionLockRepository;
import cz.cyberrange.platform.training.persistence.repository.TeamRunLockRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingInstanceRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingRunRepository;
import cz.cyberrange.platform.training.persistence.repository.UserRefRepository;
import cz.cyberrange.platform.training.service.services.api.AnswersStorageApiService;
import cz.cyberrange.platform.training.service.services.api.ElasticsearchApiService;
import cz.cyberrange.platform.training.service.services.api.SandboxApiService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * The type Training run service.
 */
@Service
public class CoopTrainingRunService extends TrainingRunService {

    private static final Logger LOG = Logger.getLogger(CoopTrainingRunService.class.getName());
    private final UserService userService;

    public CoopTrainingRunService(TrainingRunRepository trainingRunRepository,
                                  AbstractLevelRepository abstractLevelRepository,
                                  TrainingInstanceRepository trainingInstanceRepository,
                                  UserRefRepository participantRefRepository,
                                  HintRepository hintRepository,
                                  AuditEventsService auditEventsService,
                                  ElasticsearchApiService elasticsearchApiService,
                                  AnswersStorageApiService answersStorageApiService,
                                  SecurityService securityService,
                                  QuestionAnswerRepository questionAnswerRepository,
                                  SandboxApiService sandboxApiService,
                                  TRAcquisitionLockRepository trAcquisitionLockRepository,
                                  SubmissionRepository submissionRepository, TeamRunLockRepository teamRunLockRepository, UserService userService) {
        super(trainingRunRepository, abstractLevelRepository, trainingInstanceRepository,
                participantRefRepository, hintRepository, auditEventsService, elasticsearchApiService,
                answersStorageApiService, securityService, questionAnswerRepository, sandboxApiService,
                trAcquisitionLockRepository, submissionRepository);
        this.userService = userService;
    }

    /**
     * Find running training run of user optional.
     *
     * @param accessToken      the access token
     * @param participantRefId the participant ref id
     * @return the optional
     */
    @Override
    public Optional<TrainingRun> findRunningTrainingRunOfUser(String accessToken, Long participantRefId) {
        TrainingInstance trainingInstance = this.getTrainingInstanceForParticularAccessToken(accessToken);
        UserRef userRef = userService.getUserByUserRefId(securityService.getUserRefIdFromUserAndGroup());
        Optional<Team> team = userRef.getTeamByInstance(trainingInstance.getId());
        if (team.isEmpty()) {
            return Optional.empty();
        }
        return trainingRunRepository.findByCoopRunOwnerAndState(team.get(), TRState.RUNNING);
    }

    public Team getRelatedTeam(Long trainingRunId) {
        TrainingRun run = findById(trainingRunId);
        if (run.getCoopRunOwner() == null) {
            throw new EntityNotFoundException(new EntityErrorDetail(
                    String.format("Training run with id %d does not have a team", trainingRunId)
            ));
        }
        return run.getCoopRunOwner();
    }

    public List<TrainingRun> getTeamRuns(Team team) {
        Set<Long> userIds = team.getMembers().stream()
                .map(UserRef::getUserRefId)
                .collect(Collectors.toSet());
        return trainingRunRepository.getAllByTrainingInstance(team.getTrainingInstance()).
                stream()
                .filter(run -> userIds.contains(run.getLinearRunOwner().getUserRefId()))
                .toList();
    }

}
