package cz.muni.ics.kypo.training.service;

import cz.muni.ics.kypo.training.persistence.model.AbstractLevel;
import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import cz.muni.ics.kypo.training.exceptions.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * The interface for export/import service.
 *
 */
public interface ExportImportService {

    /**
     * Finds training definition with given id.
     *
     * @param trainingDefinitionId the id of definition to be found.
     * @return the {@link TrainingDefinition} with the given id.
     * @throws EntityNotFoundException if training definition was not found.
     */
    TrainingDefinition findById(Long trainingDefinitionId);

    /**
     * Find all levels associated with training definition with given id.
     *
     * @param definitionId the id of definition which levels are to be found.
     * @return the list of all {@link AbstractLevel} that are associated with the {@link TrainingDefinition}.
     */
    List<AbstractLevel> findAllLevelsFromDefinition(Long definitionId);

    /**
     * Creates a level and connects it with training definition.
     *
     * @param level      the {@link AbstractLevel} to be created.
     * @param definition the {@link TrainingDefinition} to associate level with.
     */
    void createLevel(AbstractLevel level, TrainingDefinition definition);

    /**
     * Finds training instance with given id.
     *
     * @param trainingInstanceId the id of instance to be found.
     * @return the {@link TrainingInstance} with the given id.
     * @throws EntityNotFoundException if training instance was not found.
     */
    TrainingInstance findInstanceById(Long trainingInstanceId);

    /**
     * Finds training runs associated with training instance with given id.
     *
     * @param trainingInstanceId the id of instance which runs are to be found.
     * @return the set off all {@link TrainingRun}
     */
    Set<TrainingRun> findRunsByInstanceId(Long trainingInstanceId);
}