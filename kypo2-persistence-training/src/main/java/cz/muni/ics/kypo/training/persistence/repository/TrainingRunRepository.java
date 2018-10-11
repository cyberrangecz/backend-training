package cz.muni.ics.kypo.training.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import cz.muni.ics.kypo.training.persistence.model.SandboxInstanceRef;
import cz.muni.ics.kypo.training.persistence.model.TrainingRun;
import java.util.Set;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Repository
public interface TrainingRunRepository extends JpaRepository<TrainingRun, Long>, QuerydslPredicateExecutor<TrainingRun> {

	@Query("SELECT DISTINCT tr FROM TrainingRun tr INNER JOIN tr.participantRef pr WHERE pr.participantRefLogin = :participantRef")
	Page<TrainingRun> findAllByParticipantRefLogin(@Param("participantRef") String participantRefLogin, Pageable pageable);

	@Query("SELECT tr FROM TrainingRun tr JOIN FETCH tr.currentLevel WHERE tr.id= :trainingRunId")
	Optional<TrainingRun> findByIdWithLevel(@Param("trainingRunId") Long trainingRunId);

	@Query("SELECT tr FROM TrainingRun tr INNER JOIN tr.participantRef pr INNER JOIN tr.trainingInstance ti INNER JOIN "
			+ "ti.trainingDefinition td WHERE td.id = :trainingDefinitionId AND pr.participantRefLogin = :participantRefLogin")
	Page<TrainingRun> findAllByTrainingDefinitionIdAndParticipantRefLogin(@Param("trainingDefinitionId") Long trainingDefinitionId,
			@Param("participantRefLogin") String participantRefLogin, Pageable pageable);

	Page<TrainingRun> findAllByTrainingInstanceId(Long trainingInstanceId, Pageable pageable);

	@Query("SELECT tr FROM TrainingRun tr INNER JOIN tr.trainingInstance ti INNER JOIN ti.trainingDefinition td WHERE td.id = :trainingDefinitionId")
	Page<TrainingRun> findAllByTrainingDefinitionId(@Param("trainingDefinitionId") Long trainingDefinitionId, Pageable pageable);

	@Query("SELECT sir FROM TrainingRun tr INNER JOIN tr.sandboxInstanceRef sir INNER JOIN tr.trainingInstance ti WHERE ti.id = :trainingInstanceId")
	Set<SandboxInstanceRef> findSandboxInstanceRefsOfTrainingInstance(@Param("trainingInstanceId") Long trainingInstanceId);
}
