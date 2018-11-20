package cz.muni.ics.kypo.training.persistence.repository;

import cz.muni.ics.kypo.training.persistence.model.SandboxDefinitionRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

/**
 * @author Pavel Seda
 */
@Repository
public interface SandboxDefinitionRefRepository extends JpaRepository<SandboxDefinitionRef, Long>, QuerydslPredicateExecutor<SandboxDefinitionRef> {

}
