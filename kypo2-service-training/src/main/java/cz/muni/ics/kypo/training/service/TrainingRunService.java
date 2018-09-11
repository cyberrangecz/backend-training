package cz.muni.ics.kypo.training.service;

import java.util.List;
import java.util.Optional;

import cz.muni.ics.kypo.training.exceptions.NoAvailableSandboxException;
import cz.muni.ics.kypo.training.model.AbstractLevel;
import cz.muni.ics.kypo.training.model.Hint;
import cz.muni.ics.kypo.training.model.TrainingInstance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.training.model.TrainingRun;

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
   *
   */
  public TrainingRun findById(Long id);

  /**
   * Find all Training Runs.
   * 
   * @return all Training Runs
   */
  public Page<TrainingRun> findAll(Predicate predicate, Pageable pageable);

  /**
   * Finds all Training Runs of logged in user.
   *
   * @return Training Runs of logged in user.
   */
  public Page<TrainingRun> findAllByParticipantRefLogin(Pageable pageable);

  /**
   * Finds all Training Runs of specific Training Definition of logged in user.
   *
   * @param trainingDefinitionId id of Training Definition
   * @return Training Runs
   */
  public Page<TrainingRun> findAllByTrainingDefinitionAndParticipant(Long trainingDefinitionId, Pageable pageable);

  /**
   * Finds all Training Runs of specific training definition.
   *
   * @param trainingDefinitionId id of Training Definition whose Training Runs would be returned.
   * @return Training Runs of specific Training Definition
   */
  public Page<TrainingRun> findAllByTrainingDefinition(Long trainingDefinitionId, Pageable pageable);

  /**
   * Finds all Training Runs of specific Training Instance.
   *
   * @param trainingInstanceId id of Training Instance whose Training Runs would be returned.
   * @return Training Runs of specific Training Instance
   */
  public Page<TrainingRun> findAllByTrainingInstance(Long trainingInstanceId, Pageable pageable);

  /**
   * Create given Training Run.
   *
   * @param trainingRun to be created in database.
   * @return created Training Run
   */
  public TrainingRun create(TrainingRun trainingRun);

  /**
   * Gets next level of given Training Run and set new current level.
   *
   * @param trainingRunId id of Training Run whose next level should be returned.
   * @return Abstract Level
   */
  public AbstractLevel getNextLevel(Long trainingRunId);
  /**
   * Access training run based on given password.
   *
   * @param password of Training Instance.
   * @return Abstract Level
   */
  public AbstractLevel accessTrainingRun(String password) throws NoAvailableSandboxException;
  /**
   * Gets list of all levels in Training Definition.
   *
   * @param levelId must be id of first level of some Training Definition.
   * @return List of Abstract Levels
   */
  public List<AbstractLevel> getLevels(Long levelId);

  /**
   * Check given flag of given Training Run.
   *
   * @param trainingRunId id of Training Run to check flag.
   * @param flag string which player submit.
   * @return -1 if flag is correct, >0 remaining attempts, 0 all attempts exhausted.
   */
  public boolean isCorrectFlag(Long trainingRunId, String flag);

  /**
   * Gets solution of current level of given Training Run.
   *
   * @param trainingRunId id of Training Run which current level gets solution for.
   * @return solution of current level.
   */
  public String getSolution(Long trainingRunId);

  /**
   * Gets hint of given current level of given Training Run.
   *
   * @param trainingRunId id of Training Run which current level gets hint for.
   * @param hintId id of hint to be returned.
   * @return Hint
   */
  public Hint getHint(Long trainingRunId, Long hintId);

  public int getLevelOrder(Long idOfFirstLevel, Long actualLevel);

  public int getRemainingAttempts(Long trainingRunId);

}
