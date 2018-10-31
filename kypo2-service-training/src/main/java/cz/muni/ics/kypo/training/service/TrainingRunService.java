package cz.muni.ics.kypo.training.service;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.exceptions.ServiceLayerException;
import cz.muni.ics.kypo.training.persistence.model.AbstractLevel;
import cz.muni.ics.kypo.training.persistence.model.Hint;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 
 * @author Dominik Pilar (445537)
 *
 */
public interface TrainingRunService {

  /**
   * Finds specific Training Run by id.
   * 
   * @param id of a Training Run that would be returned
   * @return specific Training Run by id
   * @throws  ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND training run is not found in DB.
   *
   */
  TrainingRun findById(Long id);

  /**
   * Find all Training Runs.
   * 
   * @return all Training Runs
   */
  Page<TrainingRun> findAll(Predicate predicate, Pageable pageable);

  /**
   * Finds all Training Runs of logged in user.
   *
   * @return Training Runs of logged in user.
   */
  Page<TrainingRun> findAllByParticipantRefLogin(Pageable pageable);

  /**
   * Finds all Training Runs of specific Training Definition of logged in user.
   *
   * @param trainingDefinitionId id of Training Definition
   * @return Training Runs
   */
  Page<TrainingRun> findAllByTrainingDefinitionAndParticipant(Long trainingDefinitionId, Pageable pageable);

  /**
   * Finds all Training Runs of specific training definition.
   *
   * @param trainingDefinitionId id of Training Definition whose Training Runs would be returned.
   * @return Training Runs of specific Training Definition
   */
  Page<TrainingRun> findAllByTrainingDefinition(Long trainingDefinitionId, Pageable pageable);

  /**
   * Finds all Training Runs of specific Training Instance.
   *
   * @param trainingInstanceId id of Training Instance whose Training Runs would be returned.
   * @return Training Runs of specific Training Instance
   */
  Page<TrainingRun> findAllByTrainingInstance(Long trainingInstanceId, Pageable pageable);

  /**
   * Create given Training Run.
   *
   * @param trainingRun to be created in database.
   * @return created Training Run
   */
  TrainingRun create(TrainingRun trainingRun);

  /**
   * Gets next level of given Training Run and set new current level.
   *
   * @param trainingRunId id of Training Run whose next level should be returned.
   * @return Abstract Level
   * @throws  ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND hint is not found in DB.
   *                                                NO_NEXT_LEVEL there is no next level.
   */
  AbstractLevel getNextLevel(Long trainingRunId);
  /**
   * Access training run based on given password.
   *
   * @param password of Training Instance.
   * @return Abstract Level
   * @throws  ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND cannot find training instance with given id or the password is wrong.
   *                                                UNEXPECTED_ERROR there is error while getting info about sandboxes.
   *                                                NO_AVAILABLE_SANDBOX there is no free or ready sandbox
   */
  AbstractLevel accessTrainingRun(String password);
  /**
   * Gets list of all levels in Training Definition.
   *
   * @param levelId must be id of first level of some Training Definition.
   * @return List of Abstract Levels
   * @throws  ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND one of the levels is not found in DB.
   *
   */
  List<AbstractLevel> getLevels(Long levelId);

  /**
   * Check given flag of given Training Run.
   *
   * @param trainingRunId id of Training Run to check flag.
   * @param flag string which player submit.
   * @return true if flag is correct, false if flag is wrong.
   * @throws  ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND training run is not found in DB.
   *                                                WRONG_LEVEL_TYPE the level is not game level.
   */
  boolean isCorrectFlag(Long trainingRunId, String flag);

  /**
   * Gets solution of current level of given Training Run.
   *
   * @param trainingRunId id of Training Run which current level gets solution for.
   * @return solution of current level.
   * @throws  ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND training run is not found in DB.
   *                                                WRONG_LEVEL_TYPE the level is not game level.
   */
  String getSolution(Long trainingRunId);

  /**
   * Gets hint of given current level of given Training Run.
   *
   * @param trainingRunId id of Training Run which current level gets hint for.
   * @param hintId id of hint to be returned.
   * @return Hint
   * @throws ServiceLayerException with ErrorCode: RESOURCE_NOT_FOUND when hint is not found in DB.
   *                                               WRONG_LEVEL_TYPE when the level is not game level.
   */
  Hint getHint(Long trainingRunId, Long hintId);

  /**
   * Gets level order of given level.
   *
   * @param idOfFirstLevel must be the first level in training definition.
   * @param actualLevel id of actual level to get order.
   * @return order of actual level.
   * @throws IllegalArgumentException id of first level or actual level is wrong.
   * @throws ServiceLayerException one of the level cannot be found.
   */
  int getLevelOrder(Long idOfFirstLevel, Long actualLevel);

	int getRemainingAttempts(Long trainingRunId);

	TrainingRun findByIdWithLevel(Long trainingRunId);



}
