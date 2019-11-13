package cz.muni.ics.kypo.training.persistence.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPQLQuery;
import cz.muni.ics.kypo.training.persistence.model.QTrainingDefinition;
import cz.muni.ics.kypo.training.persistence.model.TrainingInstance;
import cz.muni.ics.kypo.training.persistence.model.enums.TDState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * The JPA repository interface to manage {@link TrainingDefinition} instances.
 *
 * @author Pavel Seda (441048)
 */
@Repository
public interface TrainingDefinitionRepository
        extends JpaRepository<TrainingDefinition, Long>, QuerydslPredicateExecutor<TrainingDefinition>, QuerydslBinderCustomizer<QTrainingDefinition> {

    /**
     * That method is used to make the query dsl string values case insensitive and also it supports partial matches in the database.
     *
     * @param querydslBindings
     * @param qTrainingDefinition
     */
    @Override
    default void customize(QuerydslBindings querydslBindings, QTrainingDefinition qTrainingDefinition) {
        querydslBindings.bind(String.class).all((StringPath path, Collection<? extends String> values) -> {
            BooleanBuilder predicate = new BooleanBuilder();
            values.forEach(value -> predicate.and(path.containsIgnoreCase(value)));
            return Optional.ofNullable(predicate);
        });
    }

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
     * Find training definition by id
     *
     * @param id the id of training definition
     * @return {@link TrainingDefinition}
     */
    @EntityGraph(attributePaths = {"authors", "betaTestingGroup", "betaTestingGroup.organizers"})
    Optional<TrainingDefinition> findById(Long id);

}
