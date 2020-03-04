package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.responses.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.IsCorrectFlagDTO;
import cz.muni.ics.kypo.training.api.dto.UserRefDTO;
import cz.muni.ics.kypo.training.api.dto.hint.HintDTO;
import cz.muni.ics.kypo.training.api.dto.run.AccessTrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.run.AccessedTrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunByIdDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * The interface Training run facade.
 *
 */
public interface TrainingRunFacade {

    /**
     * Finds specific Training Run by id
     *
     * @param id of a Training Run that would be returned
     * @return specific {@link TrainingRunByIdDTO}
     */
    TrainingRunByIdDTO findById(Long id);

    /**
     * Find all Training Runs.
     *
     * @param predicate specifies query to the database.
     * @param pageable  pageable parameter with information about pagination.
     * @return page of all {@link TrainingRunDTO}
     */
    PageResultResource<TrainingRunDTO> findAll(Predicate predicate, Pageable pageable);

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
     * @param pageable  pageable parameter with information about pagination.
     * @param sortByTitle optional parameter. "asc" for ascending sort, "desc" for descending and null if sort is not wanted
     * @return Page of all {@link AccessedTrainingRunDTO} of logged in user.
     */
    PageResultResource<AccessedTrainingRunDTO> findAllAccessedTrainingRuns(Pageable pageable, String sortByTitle);

    /**
     * Access Training Run by logged in user based on given accessToken.
     *
     * @param accessToken of one training instance
     * @return {@link AccessTrainingRunDTO} response
     */
    AccessTrainingRunDTO accessTrainingRun(String accessToken);

    /**
     * Finds all Training Runs by specific Training Definition and logged in user.
     *
     * @param trainingDefinitionId id of Training Definition
     * @param pageable  pageable parameter with information about pagination.
     * @return Page of all {@link AccessedTrainingRunDTO} of logged in user and given definition.
     */
    PageResultResource<TrainingRunDTO> findAllByTrainingDefinitionAndParticipant(Long trainingDefinitionId, Pageable pageable);

    /**
     * Finds all Training Runs of specific training definition.
     *
     * @param trainingDefinitionId id of Training Definition whose Training Runs would be returned.
     * @param pageable  pageable parameter with information about pagination.
     * @return Page of all {@link AccessedTrainingRunDTO} of given definition.
     */
    PageResultResource<TrainingRunDTO> findAllByTrainingDefinition(Long trainingDefinitionId, Pageable pageable);

    /**
     * Gets next level of given Training Run and set new current level.
     *
     * @param trainingRunId id of Training Run whose next level should be returned.
     * @return {@link AbstractLevelDTO}
     */
    AbstractLevelDTO getNextLevel(Long trainingRunId);

    /**
     * Gets solution of current level of given Training Run.
     *
     * @param trainingRunId id of Training Run which current level gets solution for.
     * @return solution of current level.
     */
    String getSolution(Long trainingRunId);

    /**
     * Gets hint of given current level of given Training Run.
     *
     * @param trainingRunId id of Training Run which current level gets hint for.
     * @param hintId        id of hint to be returned.
     * @return {@link HintDTO}
     */
    HintDTO getHint(Long trainingRunId, Long hintId);

    /**
     * Check given flag of given Training Run.
     *
     * @param trainingRunId id of Training Run to check flag.
     * @param flag          string which player submit.
     * @return true if flag is correct, false if flag is wrong.
     */
    IsCorrectFlagDTO isCorrectFlag(Long trainingRunId, String flag);

    /**
     * Resume given training run.
     *
     * @param trainingRunId id of Training Run to be resumed.
     * @return {@link AccessTrainingRunDTO} response
     */
    AccessTrainingRunDTO resumeTrainingRun(Long trainingRunId);

    /**
     * Finish training run.
     *
     * @param trainingRunId id of Training Run to be finished.
     */
    void finishTrainingRun(Long trainingRunId);

    /**
     * Evaluate and store responses to assessment.
     *
     * @param trainingRunId     id of Training Run to be finish.
     * @param responsesAsString responses to assessment
     */
    void evaluateResponsesToAssessment(Long trainingRunId, String responsesAsString);

    /**
    * Retrieve info about the participant of the given training run.
    *
    * @param trainingRunId id of the training run whose participant you want to get.
    * @return returns participant of the given training run.
    */
    UserRefDTO getParticipant(Long trainingRunId);

}
