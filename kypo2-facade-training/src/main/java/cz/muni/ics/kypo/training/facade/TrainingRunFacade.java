package cz.muni.ics.kypo.training.facade;

import cz.muni.ics.kypo.training.api.dto.*;
import cz.muni.ics.kypo.training.api.dto.run.AccessTrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.run.AccessedTrainingRunDTO;
import cz.muni.ics.kypo.training.api.dto.run.TrainingRunDTO;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;

import cz.muni.ics.kypo.training.api.PageResultResource;

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
  public TrainingRunDTO findById(Long id);

  /**
   * Find all Training Runs.
   * 
   * @return all Training Runs
   */
  public PageResultResource<TrainingRunDTO> findAll(Predicate predicate, Pageable pageable);

  public PageResultResource<AccessedTrainingRunDTO> findAllAccessedTrainingRuns(Pageable pageable);

  public AccessTrainingRunDTO accessTrainingRun(String password);

  public PageResultResource<TrainingRunDTO> findAllByTrainingDefinitionAndParticipant(Long trainingDefinitionId, Pageable pageable);

  public PageResultResource<TrainingRunDTO> findAllByTrainingDefinition(Long trainingDefinitionId, Pageable pageable);

  public PageResultResource<TrainingRunDTO> findAllByTrainingInstance(Long trainingInstanceId, Pageable pageable);

  public AbstractLevelDTO getNextLevel(Long trainingRunId);

  public String getSolution(Long trainingRunId);

  public HintDTO getHint(Long trainingRunId, Long hintId);

  public IsCorrectFlagDTO isCorrectFlag(Long trainingRunId, String flag, boolean solutionTaken);

}
