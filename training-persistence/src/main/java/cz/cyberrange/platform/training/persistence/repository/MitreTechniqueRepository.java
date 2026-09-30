package cz.cyberrange.platform.training.persistence.repository;

import cz.cyberrange.platform.training.persistence.model.MitreTechnique;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** The JPA repository interface to manage {@link MitreTechnique} instances */
@Repository
public interface MitreTechniqueRepository
    extends JpaRepository<MitreTechnique, Long>, QuerydslPredicateExecutor<MitreTechnique> {

  /**
   * Find MITRE technique by its key.
   *
   * @param techniqueKey the MITRE technique key
   * @return the {@link MitreTechnique}
   */
  Optional<MitreTechnique> findByTechniqueKey(@Param("techniqueKey") String techniqueKey);

  /**
   * Finds all MITRE techniques whose key is one of the given keys, derived from the method name
   * with no {@code @NamedQuery} override, so it matches on the {@code technique_key} column. It
   * carries no {@code JOIN FETCH} or {@code @EntityGraph}, so each technique's training levels load
   * on demand. The result carries no guaranteed order.
   *
   * @param techniqueKeys the technique keys to match
   * @return the matching {@link MitreTechnique} instances, or an empty set if none match
   */
  Set<MitreTechnique> findAllByTechniqueKeyIn(@Param("techniqueKeys") Set<String> techniqueKeys);
}
