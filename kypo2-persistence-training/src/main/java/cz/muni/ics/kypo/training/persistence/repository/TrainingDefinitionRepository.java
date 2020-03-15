package cz.muni.ics.kypo.training.persistence.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.StringPath;
import cz.muni.ics.kypo.training.persistence.model.QTrainingDefinition;
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

import cz.muni.ics.kypo.training.persistence.model.TrainingDefinition;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

/**
 * The JPA repository interface to manage {@link TrainingDefinition} instances.
 */
@Repository
public interface TrainingDefinitionRepository
        extends JpaRepository<TrainingDefinition, Long>, TrainingDefinitionRepositoryCustom, QuerydslPredicateExecutor<TrainingDefinition>, QuerydslBinderCustomizer<QTrainingDefinition> {

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
     * Find all training definitions
     *
     * @param predicate the predicate
     * @param pageable  the pageable
     * @return page of all {@link TrainingDefinition}
     */
    @EntityGraph(
            value = "TrainingDefinition.findAllAuthorsBetaTestingGroupOrganizers",
            type = EntityGraph.EntityGraphType.FETCH
    )
    Page<TrainingDefinition> findAll(Predicate predicate, Pageable pageable);

    /**
     * Find all training definitions
     *
     * @param pageable the pageable
     * @return page of all {@link TrainingDefinition}
     */
    @EntityGraph(
            value = "TrainingDefinition.findAllAuthorsBetaTestingGroupOrganizers",
            type = EntityGraph.EntityGraphType.FETCH
    )
    Page<TrainingDefinition> findAll(Pageable pageable);

    /**
     * Find all training definitions
     *
     * @param state    the state of training definition
     * @param pageable the pageable
     * @return page of all {@link TrainingDefinition}
     */
    Page<TrainingDefinition> findAllForOrganizers(@Param("state") TDState state, Pageable pageable);

    Page<TrainingDefinition> findAllForOrganizersUnreleased(@Param("userRefId") Long userRefId, Pageable pageable);

    Page<TrainingDefinition> findAllForDesignersAndOrganizersUnreleased(@Param("userRefId") Long userRefId, Pageable pageable);

    /**
     * Find training definition by id
     *
     * @param id the id of training definition
     * @return {@link TrainingDefinition}
     */
    @EntityGraph(
            value = "TrainingDefinition.findAllAuthorsBetaTestingGroupOrganizers",
            type = EntityGraph.EntityGraphType.FETCH
    )
    Optional<TrainingDefinition> findById(Long id);
}
