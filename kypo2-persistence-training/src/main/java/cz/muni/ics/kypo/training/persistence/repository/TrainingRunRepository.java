package cz.muni.ics.kypo.training.persistence.repository;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import cz.muni.ics.kypo.training.persistence.model.SandboxInstanceRef;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;

import java.util.Set;

/**
 * @author Pavel Seda (441048)
 */
@Repository
public interface TrainingRunRepository extends JpaRepository<TrainingRun, Long>, QuerydslPredicateExecutor<TrainingRun> {

    @EntityGraph(attributePaths = {"participantRef", "sandboxInstanceRef"})
    Page<TrainingRun> findAll(Predicate predicate, Pageable pageable);

    @EntityGraph(attributePaths = {"participantRef", "sandboxInstanceRef"})
    Optional<TrainingRun> findById(Long id);

    @Query(value = "SELECT tr FROM TrainingRun tr JOIN FETCH tr.participantRef pr JOIN FETCH tr.trainingInstance ti " +
            "JOIN FETCH ti.trainingDefinition WHERE pr.userRefLogin = :participantRefLogin",
            countQuery = "SELECT COUNT(tr) FROM TrainingRun tr INNER JOIN tr.participantRef pr INNER JOIN tr.trainingInstance ti " +
                    "INNER JOIN ti.trainingDefinition WHERE pr.userRefLogin = :participantRefLogin")
    Page<TrainingRun> findAllByParticipantRefLogin(@Param("participantRefLogin") String participantRefLogin, Pageable pageable);

    @Query("SELECT tr FROM TrainingRun tr JOIN FETCH tr.currentLevel JOIN FETCH tr.trainingInstance ti JOIN FETCH ti.trainingDefinition WHERE tr.id= :trainingRunId")
    Optional<TrainingRun> findByIdWithLevel(@Param("trainingRunId") Long trainingRunId);

    @Query(value = "SELECT tr FROM TrainingRun tr JOIN FETCH tr.participantRef pr JOIN FETCH tr.trainingInstance ti JOIN FETCH "
            + "ti.trainingDefinition td WHERE td.id = :trainingDefinitionId AND pr.userRefLogin = :participantRefLogin",
            countQuery = "SELECT tr FROM TrainingRun tr INNER JOIN tr.participantRef pr INNER JOIN tr.trainingInstance ti INNER JOIN " +
                    "ti.trainingDefinition td WHERE td.id = :trainingDefinitionId AND pr.userRefLogin = :participantRefLogin")
    Page<TrainingRun> findAllByTrainingDefinitionIdAndParticipantRefLogin(@Param("trainingDefinitionId") Long trainingDefinitionId,
                                                                          @Param("participantRefLogin") String participantRefLogin, Pageable pageable);

    @EntityGraph(attributePaths = {"participantRef", "sandboxInstanceRef"})
    Page<TrainingRun> findAllByTrainingInstanceId(Long trainingInstanceId, Pageable pageable);

    @Query(value = "SELECT tr FROM TrainingRun tr JOIN FETCH tr.trainingInstance ti JOIN FETCH ti.trainingDefinition td WHERE td.id = :trainingDefinitionId",
    countQuery = "SELECT tr FROM TrainingRun tr INNER JOIN tr.trainingInstance ti INNER JOIN ti.trainingDefinition td WHERE td.id = :trainingDefinitionId")
    Page<TrainingRun> findAllByTrainingDefinitionId(@Param("trainingDefinitionId") Long trainingDefinitionId, Pageable pageable);

    @Query("SELECT sir FROM SandboxInstanceRef sir JOIN FETCH sir.trainingInstance ti WHERE ti.id = :trainingInstanceId AND sir NOT IN " +
            "(SELECT si FROM TrainingRun tr INNER JOIN tr.sandboxInstanceRef si WHERE tr.trainingInstance.id = :trainingInstanceId)")
    Set<SandboxInstanceRef> findFreeSandboxesOfTrainingInstance(@Param("trainingInstanceId") Long trainingInstanceId);

    @Modifying
    @Query("DELETE FROM TrainingRun tr WHERE tr.trainingInstance.id = :trainingInstanceId")
    void deleteTrainingRunsByTrainingInstance(@Param("trainingInstanceId") Long trainingInstanceId);
}
