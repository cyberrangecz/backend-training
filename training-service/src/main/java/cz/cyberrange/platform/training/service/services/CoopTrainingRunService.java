package cz.cyberrange.platform.training.service.services;

import cz.cyberrange.platform.training.api.exceptions.EntityErrorDetail;
import cz.cyberrange.platform.training.api.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.training.persistence.model.AbstractLevel;
import cz.cyberrange.platform.training.persistence.model.HintInfo;
import cz.cyberrange.platform.training.persistence.model.Team;
import cz.cyberrange.platform.training.persistence.model.TeamSandboxLock;
import cz.cyberrange.platform.training.persistence.model.TrainingInstance;
import cz.cyberrange.platform.training.persistence.model.TrainingRun;
import cz.cyberrange.platform.training.persistence.model.UserRef;
import cz.cyberrange.platform.training.persistence.repository.AbstractLevelRepository;
import cz.cyberrange.platform.training.persistence.repository.HintRepository;
import cz.cyberrange.platform.training.persistence.repository.QuestionAnswerRepository;
import cz.cyberrange.platform.training.persistence.repository.SubmissionRepository;
import cz.cyberrange.platform.training.persistence.repository.TRAcquisitionLockRepository;
import cz.cyberrange.platform.training.persistence.repository.TeamSandboxLockRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingInstanceRepository;
import cz.cyberrange.platform.training.persistence.repository.TrainingRunRepository;
import cz.cyberrange.platform.training.persistence.repository.UserRefRepository;
import cz.cyberrange.platform.training.service.services.api.AnswersStorageApiService;
import cz.cyberrange.platform.training.service.services.api.ElasticsearchApiService;
import cz.cyberrange.platform.training.service.services.api.SandboxApiService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * The type Training run service.
 */
@Service
public class CoopTrainingRunService extends TrainingRunService {


    private final TeamSandboxLockRepository teamSandboxLockRepository;

    private static final Logger LOG = Logger.getLogger(CoopTrainingRunService.class.getName());

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
                                  SubmissionRepository submissionRepository, TeamSandboxLockRepository teamSandboxLockRepository) {
        super(trainingRunRepository, abstractLevelRepository, trainingInstanceRepository,
                participantRefRepository, hintRepository, auditEventsService, elasticsearchApiService,
                answersStorageApiService, securityService, questionAnswerRepository, sandboxApiService,
                trAcquisitionLockRepository, submissionRepository);
        this.teamSandboxLockRepository = teamSandboxLockRepository;
    }

    /**
     * Assigns single sandbox
     *
     * @param trainingRun that will be connected with sandbox
     * @param poolId      the pool id
     * @return
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public TrainingRun assignSandbox(TrainingRun trainingRun, long poolId) {
        UserRef userRef = trainingRun.getParticipantRef();
        TrainingInstance instance = trainingRun.getTrainingInstance();

        Team team = instance.getTrainingInstanceLobby()
                .getTeams()
                .stream()
                .filter(t -> t.getMembers().contains(userRef))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(
                        String.format("Team of user %d not found", userRef.getUserRefId())
                )));

        try {
            teamSandboxLockRepository.createLock(team.getId());
            TeamSandboxLock lock = teamSandboxLockRepository.findById(team.getId())
                    .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(
                            String.format("Lock for team %d not found", team.getId())
                    )));

            TrainingRun modifiedRun = super.assignSandbox(trainingRun, poolId);

            lock.setSandboxInstanceRefId(modifiedRun.getSandboxInstanceRefId());
            lock.setSandboxInstanceAllocationId(modifiedRun.getSandboxInstanceAllocationId());
            teamSandboxLockRepository.save(lock);

            return modifiedRun;
        } catch (DataIntegrityViolationException ex) {
            LOG.warning(String.format("Lock already exists for team %d, assigning sandbox from existing lock...", team.getId()));
            return waitForOtherInstanceToAcquireLock(team, trainingRun);
        }
    }

    private TrainingRun waitForOtherInstanceToAcquireLock(Team team, TrainingRun trainingRun) {
        TeamSandboxLock existingLock = teamSandboxLockRepository.findById(team.getId())
                .orElseThrow(() -> new IllegalStateException("Expected existing lock not found"));

        long timeoutMillis = 5000;
        long sleepMillis = 100;
        long waited = 0;

        while (existingLock.getSandboxInstanceRefId() == null && waited < timeoutMillis) {
            try {
                Thread.sleep(sleepMillis);
                waited += sleepMillis;
                existingLock = teamSandboxLockRepository.findById(team.getId())
                        .orElseThrow(() -> new IllegalStateException("Lock disappeared while waiting for sandbox to be assigned"));
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for sandbox assignment", ie);
            }
        }

        if (existingLock.getSandboxInstanceRefId() == null) {
            throw new IllegalStateException("Timed out waiting for sandbox assignment");
        }

        trainingRun.setSandboxInstanceRefId(existingLock.getSandboxInstanceRefId());
        trainingRun.setSandboxInstanceAllocationId(existingLock.getSandboxInstanceAllocationId());
        return trainingRun;
    }


    public Team getRelatedTeam(Long trainingRunId) {
        TrainingRun run = findById(trainingRunId);
        UserRef userRef = run.getParticipantRef();
        return userRef.getTeams().stream().filter(
                team -> team.getTrainingInstance().getId().equals(run.getTrainingInstance().getId())
        ).findFirst().orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(
                String.format("Team of a training run %d not found", trainingRunId)
        )));

    }

    public List<TrainingRun> getTeamRuns(Team team) {
        Set<Long> userIds = team.getMembers().stream()
                .map(UserRef::getUserRefId)
                .collect(Collectors.toSet());
        return trainingRunRepository.getAllByTrainingInstance(team.getTrainingInstance()).
                stream()
                .filter(run -> userIds.contains(run.getParticipantRef().getUserRefId()))
                .toList();
    }

    /**
     * Gets all hints taken by team.
     *
     * @param team the team
     * @return the set of hints taken by the team
     * @implNote This method returns all hints taken by the team, regardless of the level and order,
     * the hints are a superset of hints accounted for when calculating the score.
     */
    public Set<HintInfo> getAllHintsTakenByTeam(Team team) {
        Set<HintInfo> hints = new HashSet<>();
        getTeamRuns(team).forEach(run -> hints.addAll(run.getHintInfoList()));
        return hints;
    }

    public boolean isLevelAnsweredByTeam(Team team, Long levelId) {
        AbstractLevel level = abstractLevelRepository.findById(levelId)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(
                        String.format("Level with id %d not found", levelId)
                )));
        return getTeamRuns(team).stream().anyMatch(
                run -> run.getCurrentLevel().getOrder() + (run.isLevelAnswered() ? 1 : 0) > level.getOrder()
        );
    }

}
