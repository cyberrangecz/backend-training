package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.AbstractLevelDTO;
import cz.muni.ics.kypo.training.api.dto.IsCorrectFlagDTO;
import cz.muni.ics.kypo.training.api.dto.hint.HintDTO;
import cz.muni.ics.kypo.training.api.dto.run.AccessTrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.run.AccessedTrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import cz.muni.ics.kypo.training.exception.FacadeLayerException;
import org.springframework.data.domain.Pageable;

/**
 * 
 * @author Dominik Pilar (445537)
 *
 */
public interface TrainingRunFacade {

  /**
   * Finds specific Training Run by id
   * 
   * @param id of a Training Run that would be returned
   * @return specific Training Run by id
   */
  TrainingRunDTO findById(Long id);

  /**
   * Find all Training Runs.
   * 
   * @return all Training Runs
   */
  PageResultResource<TrainingRunDTO> findAll(Predicate predicate, Pageable pageable);

  /**
   * Finds all Training Runs of logged in user.
   *
   * @return Training Runs of logged in user.
   */
  PageResultResource<AccessedTrainingRunDTO> findAllAccessedTrainingRuns(Pageable pageable);
  /**
   * Access Training Run by logged in user based on given password.
   *
   * @param password of one training instance
   * @return first level of training run and info about all levels in training definition.
   */
  AccessTrainingRunDTO accessTrainingRun(String password);

  /**
   * Finds all Training Runs by specific Training Definition and logged in user.
   *
   * @param trainingDefinitionId id of Training Definition
   * @return Training Runs
   */
  PageResultResource<TrainingRunDTO> findAllByTrainingDefinitionAndParticipant(Long trainingDefinitionId, Pageable pageable);

  /**
   * Finds all Training Runs of specific training definition.
   *
   * @param trainingDefinitionId id of Training Definition whose Training Runs would be returned.
   * @return Training Runs of specific Training Definition
   */
  PageResultResource<TrainingRunDTO> findAllByTrainingDefinition(Long trainingDefinitionId, Pageable pageable);

  /**
   * Gets next level of given Training Run and set new current level.
   *
   * @param trainingRunId id of Training Run whose next level should be returned.
   * @return Abstract Level
   * @throws FacadeLayerException if something wrong happened in service.
   */
  AbstractLevelDTO getNextLevel(Long trainingRunId);

  /**
   * Gets solution of current level of given Training Run.
   *
   * @param trainingRunId id of Training Run which current level gets solution for.
   * @return solution of current level.
   * @throws  FacadeLayerException if something wrong happened in service.
   */
  String getSolution(Long trainingRunId);

  /**
   * Gets hint of given current level of given Training Run.
   *
   * @param trainingRunId id of Training Run which current level gets hint for.
   * @param hintId id of hint to be returned.
   * @return HintDTO
   * @throws FacadeLayerException if something wrong happened in service.
   */
  HintDTO getHint(Long trainingRunId, Long hintId);

  /**
   * Check given flag of given Training Run.
   *
   * @param trainingRunId id of Training Run to check flag.
   * @param flag string which player submit.
   * @return true if flag is correct, false if flag is wrong.
   * @throws FacadeLayerException if something wrong happened in service.

   */
  IsCorrectFlagDTO isCorrectFlag(Long trainingRunId, String flag);

}
