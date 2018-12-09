package cz.muni.ics.kypo.training.service.impl;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.exceptions.CommonsServiceException;
import cz.muni.ics.kypo.training.persistence.model.Role;
import cz.muni.ics.kypo.training.persistence.repository.RoleRepository;
import cz.muni.ics.kypo.training.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Optional;

/**
 * @author Pavel Seda
 */
@Service
public class RoleServiceImpl implements RoleService {

    private static Logger LOG = LoggerFactory.getLogger(RoleServiceImpl.class);

    private RoleRepository roleRepository;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Role getById(long id) {
        LOG.info("getById({})", id);
        Optional<Role> optionalRole = roleRepository.findById(id);
        return optionalRole.orElseThrow(() -> new CommonsServiceException("Role with id " + id + " could not be found"));

    }

    @Override
    public Role getByRoleType(String roleType) {
        LOG.info("getByRoleType({})", roleType);
        Assert.hasLength(roleType, "Input role type must not be null");
        Optional<Role> optionalRole = roleRepository.findByRoleType(roleType);
        return optionalRole.orElseThrow(() -> new CommonsServiceException("Role with role type " + roleType + " could not be found"));

    }

    @Override
    public Page<Role> getAllRoles(Predicate predicate, Pageable pageable) {
        LOG.info("getAllRoles()");
        return roleRepository.findAll(predicate, pageable);
    }

}
