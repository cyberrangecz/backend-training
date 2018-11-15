package cz.muni.ics.kypo.training.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;

import java.util.List;

/**
 * @author Pavel Seda (441048)
 */
@Repository
public interface TrainingInstanceRepository extends JpaRepository<TrainingInstance, Long>, QuerydslPredicateExecutor<TrainingInstance> {

	@Query("SELECT ti FROM TrainingInstance ti INNER JOIN ti.trainingDefinition td WHERE td.id = :trainingDefId")
	List<TrainingInstance> findAllByTrainingDefinitionId(@Param("trainingDefId") Long trainingDefId);
}
