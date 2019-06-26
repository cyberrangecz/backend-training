package cz.muni.ics.kypo.training.persistence.repository;

import cz.muni.ics.kypo.training.persistence.model.SandboxInstanceRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * The JPA repository interface to manage {@link SandboxInstanceRef} instances.
 *
 * @author Dominik Pilar (445537)
 */
@Repository
public interface SandboxInstanceRefRepository extends JpaRepository<SandboxInstanceRef, Long>, QuerydslPredicateExecutor<SandboxInstanceRef> {

    /**
     * Find SandboxInstanceRef by sandbox instance ref ID.
     *
     * @param sandboxInstanceRefId ID of sandbox from Python API.
     * @return {@link SandboxInstanceRef}
     */
    @Query("SELECT sir FROM SandboxInstanceRef sir WHERE sir.sandboxInstanceRefId = :sandboxInstanceRefId")
    Optional<SandboxInstanceRef> findBySandboxInstanceRefId(@Param("sandboxInstanceRefId")Long sandboxInstanceRefId);
}
