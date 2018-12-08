package cz.muni.ics.kypo.training.service;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.exceptions.CommonsServiceException;
import cz.muni.ics.kypo.training.persistence.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author Pavel Seda
 */
public interface RoleService {

    /**
     * Returns role by id
     *
     * @param id of role
     * @return role with given id
     * @throws CommonsServiceException if role could not be found
     */
    Role getById(long id);

    /**
     * Return role by role type
     *
     * @param roleType of role
     * @return role with given roleType
     * @throws CommonsServiceException when role with given role type could not be found
     */
    Role getByRoleType(String roleType);

    /**
     * Returns all roles
     *
     * @return all roles
     */
    Page<Role> getAllRoles(Predicate predicate, Pageable pageable);

}
