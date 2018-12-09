package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.IDMGroupRefDTO;
import cz.muni.ics.kypo.training.api.dto.RoleDTO;
import cz.muni.ics.kypo.training.exception.CommonsFacadeException;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

/**
 * @author Pavel Seda
 */
public interface IDMGroupRefFacade {

    /**
     * Deletes given group ref from database.
     *
     * @param groupRefId group ref to be deleted
     * @throws CommonsFacadeException
     */
    void delete(Long groupRefId);

    PageResultResource<IDMGroupRefDTO> getAllGroups(Predicate predicate, Pageable pageable);

    /**
     * Assign role to group.
     *
     * @param idmGroupId id of idm group ref to assign roles to
     * @param roleId     type of role to be assigned to group
     * @throws CommonsFacadeException
     */
    void assignRoleToGroup(long roleId, long idmGroupId);

    /**
     * Returns set of roles of given groups
     *
     * @return roles
     */
    Set<RoleDTO> getRolesOfGroups(List<Long> groupsIds);

    void removeRoleFromGroup(long roleId, long idmGroupId);
}
