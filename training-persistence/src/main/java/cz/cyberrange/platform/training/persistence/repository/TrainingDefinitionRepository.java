package cz.cyberrange.platform.training.persistence.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.StringPath;
import cz.cyberrange.platform.training.persistence.model.QTrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.TrainingDefinition;
import cz.cyberrange.platform.training.persistence.model.enums.TDState;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

/** The JPA repository interface to manage {@link TrainingDefinition} instances. */
@Repository
public interface TrainingDefinitionRepository
    extends JpaRepository<TrainingDefinition, Long>,
        TrainingDefinitionRepositoryCustom,
        QuerydslPredicateExecutor<TrainingDefinition>,
        QuerydslBinderCustomizer<QTrainingDefinition> {

  /**
   * That method is used to make the query dsl string values case insensitive and also it supports
   * partial matches in the database.
   *
   * @param querydslBindings
   * @param qTrainingDefinition
   */
  @Override
  default void customize(
      QuerydslBindings querydslBindings, QTrainingDefinition qTrainingDefinition) {
    querydslBindings
        .bind(String.class)
        .all(
            (StringPath path, Collection<? extends String> values) -> {
              BooleanBuilder predicate = new BooleanBuilder();
              values.forEach(value -> predicate.and(path.containsIgnoreCase(value)));
              return Optional.ofNullable(predicate);
            });
  }

  /**
   * Find all training definitions
   *
   * @param predicate the predicate
   * @param pageable the pageable
   * @return page of all {@link TrainingDefinition}
   */
  @EntityGraph(
      value = "TrainingDefinition.findAllAuthorsBetaTestingGroupOrganizers",
      type = EntityGraph.EntityGraphType.FETCH)
  Page<TrainingDefinition> findAll(Predicate predicate, Pageable pageable);

  /**
   * Find all training definitions
   *
   * @param pageable the pageable
   * @return page of all {@link TrainingDefinition}
   */
  @EntityGraph(
      value = "TrainingDefinition.findAllAuthorsBetaTestingGroupOrganizers",
      type = EntityGraph.EntityGraphType.FETCH)
  Page<TrainingDefinition> findAll(Pageable pageable);

  /**
   * Find all training definitions
   *
   * @param state the state of training definition
   * @param pageable the pageable
   * @return page of all {@link TrainingDefinition}
   */
  Page<TrainingDefinition> findAllByState(@Param("state") TDState state, Pageable pageable);

  /**
   * Find all for organizers unreleased page.
   *
   * @param userRefId the user ref id
   * @param pageable the pageable
   * @return the page
   */
  Page<TrainingDefinition> findAllForOrganizersUnreleased(
      @Param("userRefId") Long userRefId, Pageable pageable);

  /**
   * Find all for designers and organizers unreleased page.
   *
   * @param userRefId the user ref id
   * @param pageable the pageable
   * @return the page
   */
  Page<TrainingDefinition> findAllForDesignersAndOrganizersUnreleased(
      @Param("userRefId") Long userRefId, Pageable pageable);

  /**
   * Find training definition by id
   *
   * @param id the id of training definition
   * @return {@link TrainingDefinition}
   */
  @EntityGraph(
      value = "TrainingDefinition.findAllAuthorsBetaTestingGroupOrganizers",
      type = EntityGraph.EntityGraphType.FETCH)
  Optional<TrainingDefinition> findById(Long id);

  /**
   * Find ids of all training definitions the given user has a training run in.
   *
   * @param userRefId the user ref id
   * @return the ids of played training definitions
   */
  Set<Long> findPlayedDefinitionIdsByUser(@Param("userRefId") Long userRefId);

  /**
   * Find every MITRE technique key used by a training level of a training definition in the given
   * state, as one row per definition and technique key.
   *
   * @param state the state of training definition
   * @return the MITRE technique usages, ordered by definition title, definition id and technique
   *     key
   */
  @Query(
      "SELECT DISTINCT definition.id AS definitionId, "
          + "definition.title AS title, "
          + "technique.techniqueKey AS techniqueKey "
          + "FROM TrainingLevel trainingLevel "
          + "JOIN trainingLevel.trainingDefinition definition "
          + "JOIN trainingLevel.mitreTechniques technique "
          + "WHERE definition.state = :state "
          + "ORDER BY definition.title, definition.id, technique.techniqueKey")
  List<MitreTechniqueUsage> findMitreTechniqueUsagesByState(@Param("state") TDState state);

  List<TrainingDefinition> findAllByIdIn(Collection<Long> ids);

  /** A single MITRE technique key used by a training definition. */
  interface MitreTechniqueUsage {

    /**
     * Gets the id of the training definition using the technique.
     *
     * @return the training definition id
     */
    Long getDefinitionId();

    /**
     * Gets the title of the training definition using the technique.
     *
     * @return the training definition title
     */
    String getTitle();

    /**
     * Gets the key of the used MITRE technique.
     *
     * @return the MITRE technique key
     */
    String getTechniqueKey();
  }
}
