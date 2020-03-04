package cz.muni.ics.kypo.training.service;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.exceptions.EntityConflictException;
import cz.muni.ics.kypo.training.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.training.exceptions.TooManyRequestsException;
import cz.muni.ics.kypo.training.exceptions.BadRequestException;
import cz.muni.ics.kypo.training.persistence.model.AbstractLevel;
import cz.muni.ics.kypo.training.persistence.model.Hint;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * The interface for Training run service.
 *
 */
public interface TrainingRunService {

    /**
     * Finds specific Training Run by id.
     *
     * @param id of a Training Run that would be returned
     * @return specific {@link TrainingRun} by id
     * @throws EntityNotFoundException training run is not found.
     */
    TrainingRun findById(Long id);

    /**
     * Finds specific Training Run by id including current level.
     *
     * @param id of a Training Run with level that would be returned
     * @return specific {@link TrainingRun} by id
     * @throws EntityNotFoundException training run is not found.
     */
    TrainingRun findByIdWithLevel(Long id);

    /**
     * Find all Training Runs.
     *
     * @param predicate specifies query to the database.
     * @param pageable  pageable parameter with information about pagination.
     * @return all {@link TrainingRun}s
     */
    Page<TrainingRun> findAll(Predicate predicate, Pageable pageable);

    /**
     * Delete selected training runs.
     *
     * @param trainingRunIds training runs to delete
     */
    void deleteTrainingRuns(List<Long> trainingRunIds);

    /**
     * Delete selected training run.
     *
     * @param trainingRunId training run to delete
     */
    void deleteTrainingRun(Long trainingRunId);

    /**
     * Finds all Training Runs of logged in user.
     *
     * @param pageable pageable parameter with information about pagination.
     * @return {@link TrainingRun}s of logged in user.
     */
    Page<TrainingRun> findAllByParticipantRefUserRefId(Pageable pageable);

    /**
     * Finds all Training Runs of specific Training Definition of logged in user.
     *
     * @param trainingDefinitionId id of Training Definition
     * @param pageable             pageable parameter with information about pagination.
     * @return {@link TrainingRun}s of specific Training Definition of logged in user
     */
    Page<TrainingRun> findAllByTrainingDefinitionAndParticipant(Long trainingDefinitionId, Pageable pageable);

    /**
     * Finds all Training Runs of specific training definition.
     *
     * @param trainingDefinitionId id of Training Definition whose Training Runs would be returned.
     * @param pageable             pageable parameter with information about pagination.
     * @return {@link TrainingRun}s of specific Training Definition
     */
    Page<TrainingRun> findAllByTrainingDefinition(Long trainingDefinitionId, Pageable pageable);

    /**
     * Gets next level of given Training Run and set new current level.
     *
     * @param trainingRunId id of Training Run whose next level should be returned.
     * @return {@link AbstractLevel}
     * @throws EntityNotFoundException training run or level is not found.
     */
    AbstractLevel getNextLevel(Long trainingRunId);

    /**
     * Access training run based on given accessToken.
     *
     * @param accessToken of Training Instance.
     * @return accessed {@link TrainingRun}
     * @throws EntityNotFoundException no active training instance for given access token, no starting level in training definition.
     * @throws EntityConflictException pool of sandboxes is not created for training instance.
     * @throws TooManyRequestsException training run has been already accessed.
     */
    TrainingRun accessTrainingRun(String accessToken);

    /**
     * Gets list of all levels in Training Definition.
     *
     * @param levelId must be id of first level of some Training Definition.
     * @return List of {@link AbstractLevel}s
     * @throws EntityNotFoundException one of the levels is not found.
     */
    List<AbstractLevel> getLevels(Long levelId);

    /**
     * Check given flag of given Training Run.
     *
     * @param trainingRunId id of Training Run to check flag.
     * @param flag          string which player submit.
     * @return true if flag is correct, false if flag is wrong.
     * @throws EntityNotFoundException training run is not found.
     * @throws BadRequestException the current level of training run is not game level.
     */
    boolean isCorrectFlag(Long trainingRunId, String flag);

    /**
     * Gets solution of current level of given Training Run.
     *
     * @param trainingRunId id of Training Run which current level gets solution for.
     * @return solution of current level.
     * @throws EntityNotFoundException training run is not found.
     * @throws BadRequestException the current level of training run is not game level.
     */
    String getSolution(Long trainingRunId);

    /**
     * Gets hint of given current level of given Training Run.
     *
     * @param trainingRunId id of Training Run which current level gets hint for.
     * @param hintId        id of hint to be returned.
     * @return {@link Hint}
     * @throws EntityNotFoundException training run or hint is not found.
     * @throws BadRequestException the current level of training run is not game level.
     */
    Hint getHint(Long trainingRunId, Long hintId);

    /**
     * Gets max level order of levels from definition.
     *
     * @param definitionId id of training definition.
     * @return max order of levels.
     */
    int getMaxLevelOrder(Long definitionId);

    /**
     * Gets remaining attempts to solve current level of training run.
     *
     * @param trainingRunId the training run id
     * @return the remaining attempts
     */
    int getRemainingAttempts(Long trainingRunId);

    /**
     * Resume previously closed training run.
     *
     * @param trainingRunId id of training run to be resumed.
     * @return {@link TrainingRun}
     * @throws EntityNotFoundException training run is not found.
     */
    TrainingRun resumeTrainingRun(Long trainingRunId);

    /**
     * Finish training run.
     *
     * @param trainingRunId id of training run to be finished.
     * @throws EntityNotFoundException training run is not found.
     */
    void finishTrainingRun(Long trainingRunId);

    /**
     * Evaluate and store responses to assessment.
     *
     * @param trainingRunId     id of training run to be finished.
     * @param responsesAsString response to assessment to be evaluated
     * @throws EntityNotFoundException training run is not found.
     */
    void evaluateResponsesToAssessment(Long trainingRunId, String responsesAsString);

    /**
     * Connects available sandbox with given Training run.
     *
     * @param trainingRun that will be connected with sandbox
     * @return Training run with assigned sandbox
     */
    TrainingRun assignSandbox(TrainingRun trainingRun);
}
