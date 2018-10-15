package cz.muni.ics.kypo.training.persistence.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;

import java.util.List;

/**
 * 
 * @author Pavel Seda (441048)
 *
 */
@Repository
public interface TrainingDefinitionRepository
		extends JpaRepository<TrainingDefinition, Long>, QuerydslPredicateExecutor<TrainingDefinition> {

//	@Query("SELECT al.id FROM TrainingDefinition td JOIN AbstractLevel al ON td.id = al.id")
//	List<Long> findAllIdsFromAbstractLevel();

	@Query("SELECT td FROM TrainingDefinition td INNER JOIN td.sandBoxDefinitionRef sbd WHERE sbd.sandboxDefinitionRef = :sandboxDefId")
	Page<TrainingDefinition> findAllBySandBoxDefinitionRefId(@Param("sandboxDefId") Long sandboxDefId, Pageable pageable);
}
