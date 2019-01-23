package cz.muni.ics.kypo.training.persistence.repository;

import cz.muni.ics.kypo.training.persistence.model.InfoLevel;
import cz.muni.ics.kypo.training.persistence.model.TDViewGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


/**
 * @author Dominik Pilar (445537)
 */
public interface TDViewGroupRepository extends JpaRepository<TDViewGroup, Long>, QuerydslPredicateExecutor<InfoLevel> {

    @Query("SELECT vg FROM TDViewGroup vg WHERE vg.title = :groupTitle")
    Optional<TDViewGroup> findByTitle(@Param("groupTitle") String groupTitle);

    @Query("SELECT (COUNT(vg) > 0) FROM TDViewGroup vg WHERE vg.title = :groupTitle")
    boolean existsTDViewGroupByTitle(@Param("groupTitle") String groupTitle);
}
