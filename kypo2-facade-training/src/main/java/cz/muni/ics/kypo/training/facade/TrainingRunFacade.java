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
  TrainingRunDTO findById(Long id);

  /**
   * Find all Training Runs.
   * 
   * @return all Training Runs
   */
  PageResultResource<TrainingRunDTO> findAll(Predicate predicate, Pageable pageable);

  PageResultResource<AccessedTrainingRunDTO> findAllAccessedTrainingRuns(Pageable pageable);

  AccessTrainingRunDTO accessTrainingRun(String password);

  PageResultResource<TrainingRunDTO> findAllByTrainingDefinitionAndParticipant(Long trainingDefinitionId, Pageable pageable);

  PageResultResource<TrainingRunDTO> findAllByTrainingDefinition(Long trainingDefinitionId, Pageable pageable);

  PageResultResource<TrainingRunDTO> findAllByTrainingInstance(Long trainingInstanceId, Pageable pageable);

  AbstractLevelDTO getNextLevel(Long trainingRunId);

  String getSolution(Long trainingRunId);

  HintDTO getHint(Long trainingRunId, Long hintId);

  boolean isCorrectFlag(Long trainingRunId, String flag);

}
