package cz.muni.ics.kypo.training.facade;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.RoleDTO;
import cz.muni.ics.kypo.training.exception.CommonsFacadeException;
import org.springframework.data.domain.Pageable;

/**
 * @author Pavel Seda
 */
public interface RoleFacade {
    /**
     * Returns role by id
     *
     * @param id of role
     * @return role with given id
     * @throws CommonsFacadeException if role could not be found
     */
    RoleDTO getById(long id);

    /**
     * Return role by role type
     *
     * @param roleType of role
     * @return role with given roleType
     * @throws CommonsFacadeException when role with given role type could not be found
     */
    RoleDTO getByRoleType(String roleType);

    /**
     * Returns all roles
     *
     * @return all roles
     */
    PageResultResource<RoleDTO> getAllRoles(Predicate predicate, Pageable pageable);


}
