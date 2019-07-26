package cz.muni.ics.kypo.training.persistence.repository;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;

import java.util.List;
import java.util.Optional;

/**
 * The JPA repository interface to manage {@link TrainingDefinition} instances.
 *
 * @author Pavel Seda (441048)
 */
@Repository
public interface TrainingDefinitionRepository
        extends JpaRepository<TrainingDefinition, Long>, QuerydslPredicateExecutor<TrainingDefinition> {

    /**
     * Find all training definitions by their associated sand box definition ref.
     *
     * @param sandboxDefId the sandbox def id
     * @param pageable     the pageable
     * @return page of {@link TrainingDefinition}s associated to given sandbox definition
     */
    @Query("SELECT td FROM TrainingDefinition td WHERE td.sandboxDefinitionRefId = :sandboxDefId")
    Page<TrainingDefinition> findAllBySandBoxDefinitionRefId(@Param("sandboxDefId") Long sandboxDefId, Pageable pageable);

    /**
     * Find all training definitions
     *
     * @param predicate the predicate
     * @param pageable  the pageable
     * @return page of all {@link TrainingDefinition}
     */
    @EntityGraph(attributePaths = {"authors", "betaTestingGroup", "betaTestingGroup.organizers"})
    Page<TrainingDefinition> findAll(Predicate predicate, Pageable pageable);

    /**
     * Find all training definitions accessible to logged in user.
     *
     * @param userRefId the user ref id
     * @param pageable     the pageable
     * @return the page of {@link TrainingDefinition} accessible to logged in user
     */
    @Query(value = "SELECT td FROM TrainingDefinition td INNER JOIN td.authors a WHERE a.userRefId = :userRefId",
            countQuery = "SELECT COUNT(td) FROM TrainingDefinition td INNER JOIN td.authors a WHERE a.userRefId = :userRefId")
    Page<TrainingDefinition> findAllByLoggedInUser(@Param("userRefId") Long userRefId, Pageable pageable);

    /**
     * Find all training definitions accessible to organizer.
     *
     * @param userRefId the user ref id
     * @param pageable     the pageable
     * @return the page of {@link TrainingDefinition} accessible to organizer
     */
    @Query(value = "SELECT DISTINCT td FROM TrainingDefinition td LEFT JOIN td.betaTestingGroup bt LEFT JOIN bt.organizers org WHERE org.userRefId = :userRefId OR td.state = 'RELEASED'",
            countQuery = "SELECT COUNT(DISTINCT td) FROM TrainingDefinition td LEFT JOIN td.betaTestingGroup bt LEFT JOIN bt.organizers org WHERE org.userRefId = :userRefId OR td.state = 'RELEASED'")
    Page<TrainingDefinition> findAllForOrganizers(@Param("userRefId") Long userRefId, Pageable pageable);

    /**
     * Find all training definitions accessible to organizer and designer.
     *
     * @param userRefId the user ref id
     * @param pageable     the pageable
     * @return the page of {@link TrainingDefinition} accessible to organizer and designer
     */
    @Query(value = "SELECT DISTINCT td FROM TrainingDefinition td LEFT JOIN td.betaTestingGroup bt LEFT JOIN bt.organizers org " +
            "LEFT JOIN td.authors aut WHERE aut.userRefId = :userRefId OR org.userRefId = :userRefId OR td.state = 'RELEASED'",
            countQuery = "SELECT COUNT(DISTINCT td) FROM TrainingDefinition td LEFT JOIN td.betaTestingGroup bt LEFT JOIN bt.organizers org " +
                    "LEFT JOIN td.authors aut WHERE aut.userRefId = :userRefId OR org.userRefId = :userRefId OR td.state = 'RELEASED'")
    Page<TrainingDefinition> findAllForDesignersAndOrganizers(@Param("userRefId") Long userRefId, Pageable pageable);

    /**
     * Find training definition by id
     *
     * @param id the id of training definition
     * @return {@link TrainingDefinition}
     */
    @EntityGraph(attributePaths = {"authors", "betaTestingGroup", "betaTestingGroup.organizers"})
    Optional<TrainingDefinition> findById(Long id);

}
