package cz.muni.ics.kypo.training.persistence.repository;

import cz.muni.ics.kypo.training.persistence.model.IDMGroupRef;
import cz.muni.ics.kypo.training.persistence.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author Pavel Seda
 */
@Repository
public interface IDMGroupRefRepository extends JpaRepository<IDMGroupRef, Long>, QuerydslPredicateExecutor<IDMGroupRef> {

    Optional<IDMGroupRef> findByIdmGroupId(Long id);

    @Query("SELECT g FROM IDMGroupRef AS g INNER JOIN g.roles AS r WHERE r.roleType = :roleType")
    List<IDMGroupRef> findAllByRoleType(@Param("roleType") String roleType);

    @Query("SELECT r FROM IDMGroupRef g INNER JOIN g.roles r WHERE g.id = :groupId")
    Set<Role> getRolesOfGroup(@Param("groupId") Long id);
}
