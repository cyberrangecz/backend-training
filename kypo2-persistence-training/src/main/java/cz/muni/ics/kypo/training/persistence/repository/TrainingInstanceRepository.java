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
 * @author Pavel Seda (441048)
 */
@Repository
public interface TrainingInstanceRepository extends JpaRepository<TrainingInstance, Long>, QuerydslPredicateExecutor<TrainingInstance> {

    @Query("SELECT ti FROM TrainingInstance ti JOIN FETCH ti.trainingDefinition td WHERE td.id = :trainingDefId")
    List<TrainingInstance> findAllByTrainingDefinitionId(@Param("trainingDefId") Long trainingDefId);

    @EntityGraph(attributePaths = {"trainingDefinition.authors", "organizers", "sandboxInstanceRefs", "trainingDefinition.betaTestingGroup", "trainingDefinition.betaTestingGroup.organizers"})
    Page<TrainingInstance> findAll(Predicate predicate, Pageable pageable);

    @Query("SELECT ti FROM TrainingInstance ti JOIN FETCH ti.trainingDefinition WHERE ti.startTime < :date AND ti.endTime > :date ")
    List<TrainingInstance> findAllByStartTimeAfterAndEndTimeBefore(@Param("date") LocalDateTime time);

    @EntityGraph(attributePaths = {"trainingDefinition.authors", "organizers", "sandboxInstanceRefs"})
    Optional<TrainingInstance> findById(Long id);

    @Query("SELECT (COUNT(ti) > 0) FROM TrainingInstance ti INNER JOIN ti.trainingDefinition td WHERE ti.id = :trainingInstanceId")
    boolean existsAnyForTrainingDefinition(@Param("trainingInstanceId") Long trainingInstanceId);

    @Query("SELECT ti FROM TrainingInstance ti LEFT OUTER JOIN FETCH ti.organizers LEFT OUTER JOIN FETCH ti.sandboxInstanceRefs JOIN FETCH"
        + " ti.trainingDefinition td JOIN FETCH td.authors JOIN FETCH td.betaTestingGroup btg JOIN FETCH btg.organizers WHERE ti.id = :instanceId")
    Optional<TrainingInstance> findByIdIncludingDefinition(@Param("instanceId") Long instanceId);

}
