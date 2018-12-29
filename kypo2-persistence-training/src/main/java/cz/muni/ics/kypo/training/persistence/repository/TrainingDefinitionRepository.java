package cz.muni.ics.kypo.training.persistence.repository;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;


/**
 * @author Pavel Seda (441048)
 */
@Repository
public interface TrainingDefinitionRepository
        extends JpaRepository<TrainingDefinition, Long>, QuerydslPredicateExecutor<TrainingDefinition> {

    @Query("SELECT td FROM TrainingDefinition td WHERE td.sandboxDefinitionRefId = :sandboxDefId")
    Page<TrainingDefinition> findAllBySandBoxDefinitionRefId(@Param("sandboxDefId") Long sandboxDefId, Pageable pageable);

    @EntityGraph(attributePaths = {"authors"})
    Page<TrainingDefinition> findAll(Predicate predicate, Pageable pageable);

    @Query(value = "SELECT td FROM TrainingDefinition td JOIN FETCH td.authors a WHERE a.userRefLogin = :userRefLogin",
            countQuery = "SELECT COUNT(td) FROM TrainingDefinition td INNER JOIN td.authors a WHERE a.userRefLogin = :userRefLogin")
    Page<TrainingDefinition> findAllByLoggedInUser(@Param("userRefLogin") String userRefLogin, Pageable pageable);

}
