package cz.muni.ics.kypo.training.persistence.repository;

import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import com.querydsl.core.types.Predicate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


/**
 * The JPA repository interface to manage {@link TrainingInstance} instances.
 *
 * @author Pavel Seda (441048)
 */
@Repository
public interface TrainingInstanceRepository extends JpaRepository<TrainingInstance, Long>, QuerydslPredicateExecutor<TrainingInstance> {

    /**
     * Find all training instances by id of associated training definition.
     *
     * @param trainingDefId the training def id
     * @return the list of {@link TrainingInstance}s associated to {@link TrainingDefinition}
     */
    @Query("SELECT ti FROM TrainingInstance ti JOIN FETCH ti.trainingDefinition td WHERE td.id = :trainingDefId")
    List<TrainingInstance> findAllByTrainingDefinitionId(@Param("trainingDefId") Long trainingDefId);

    /**
     * Find all training instances
     *
     * @param predicate the predicate
     * @param pageable  the pageable
     * @return page of all {@link TrainingInstance}
     */
    @EntityGraph(attributePaths = {"trainingDefinition.authors", "organizers", "sandboxInstanceRefs", "trainingDefinition.betaTestingGroup", "trainingDefinition.betaTestingGroup.organizers"})
    Page<TrainingInstance> findAll(Predicate predicate, Pageable pageable);

    /**
     * Find training instance with start time in the past, end time in the future and by corresponding access token.
     *
     * @param time        the current time
     * @param accessToken the access token
     * @return {@link TrainingInstance} with start time in the past, end time in the future and by corresponding access token
     */
    @Query("SELECT ti FROM TrainingInstance ti JOIN FETCH ti.trainingDefinition WHERE ti.startTime < :date AND ti.endTime > :date AND ti.accessToken = :accessToken ")
    Optional<TrainingInstance> findByStartTimeAfterAndEndTimeBeforeAndAccessToken(@Param("date") LocalDateTime time, @Param("accessToken") String accessToken);

    /**
     * Find training instance by id
     *
     * @param id id of training instance
     * @return {@link TrainingInstance}
     */
    @EntityGraph(attributePaths = {"trainingDefinition.authors", "organizers", "sandboxInstanceRefs"})
    Optional<TrainingInstance> findById(Long id);


    /**
     * Check if any training instances are associated with training definition
     *
     * @param trainingDefinitionId the training definition id
     * @return True if there are any instances associated with training definition
     */
    @Query("SELECT (COUNT(ti) > 0) FROM TrainingInstance ti INNER JOIN ti.trainingDefinition td WHERE td.id = :trainingDefinitionId")
    boolean existsAnyForTrainingDefinition(@Param("trainingDefinitionId") Long trainingDefinitionId);

    /**
     * Find training instance by id including its associated training definition.
     *
     * @param instanceId the instance id
     * @return {@link TrainingInstance} including its associated {@link TrainingDefinition}
     */
    @Query("SELECT ti FROM TrainingInstance ti LEFT OUTER JOIN FETCH ti.organizers LEFT OUTER JOIN FETCH ti.sandboxInstanceRefs JOIN FETCH"
            + " ti.trainingDefinition td LEFT OUTER JOIN FETCH td.authors LEFT OUTER JOIN FETCH td.betaTestingGroup btg LEFT OUTER JOIN FETCH btg.organizers WHERE ti.id = :instanceId")
    Optional<TrainingInstance> findByIdIncludingDefinition(@Param("instanceId") Long instanceId);


    /**
     * Checks if training instance finished.
     *
     * @param currentTime       the current time
     * @param instanceId the instance id
     * @return true if instance is finished, false if not
     */
    @Query("SELECT (COUNT(ti) > 0) FROM TrainingInstance ti WHERE ti.id = :instanceId AND ti.endTime < :currentTime")
    boolean isFinished(@Param("instanceId") Long instanceId, @Param("currentTime") LocalDateTime currentTime);
}
