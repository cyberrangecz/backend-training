package cz.muni.ics.kypo.training.service.impl;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.exceptions.CommonsServiceException;
import cz.muni.ics.kypo.training.persistence.model.IDMGroupRef;
import cz.muni.ics.kypo.training.persistence.model.Role;
import cz.muni.ics.kypo.training.persistence.repository.IDMGroupRefRepository;
import cz.muni.ics.kypo.training.persistence.repository.RoleRepository;
import cz.muni.ics.kypo.training.service.IDMGroupRefService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author Pavel Seda & Dominik Pilar
 */
@Service
public class IDMGroupRefServiceImpl implements IDMGroupRefService {

    private static Logger LOG = LoggerFactory.getLogger(IDMGroupRefServiceImpl.class);

    private IDMGroupRefRepository idmGroupRefRepository;
    private RoleRepository roleRepository;

    @Autowired
    public IDMGroupRefServiceImpl(IDMGroupRefRepository idmGroupRefRepository, RoleRepository roleRepository) {
        this.idmGroupRefRepository = idmGroupRefRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public void delete(long id) {
        LOG.info("delete({})", id);
        IDMGroupRef idmGroupRef = idmGroupRefRepository.findByIdmGroupId(id).orElseThrow(() -> new CommonsServiceException("Idm group ref with id: " + id + " not found."));
        idmGroupRefRepository.delete(idmGroupRef);
    }

    @Override
    public Page<IDMGroupRef> getAllGroups(Predicate predicate, Pageable pageable) {
        return idmGroupRefRepository.findAll(predicate, pageable);
    }

    @Override
    public void assignRoleToGroup(long roleId, long idmGroupId) {
        LOG.info("assignRoleToGroup({}, {})", roleId, idmGroupId);
        Optional<Role> optionalRoleToBeAssigned = roleRepository.findById(roleId);
        Role role = optionalRoleToBeAssigned.orElseThrow(() -> new CommonsServiceException("Input role with id " + roleId + " cannot be found"));

        Optional<IDMGroupRef> optIdmGroupRef = idmGroupRefRepository.findByIdmGroupId(idmGroupId);
        IDMGroupRef idmGroupRef = optIdmGroupRef.orElse(new IDMGroupRef());
        idmGroupRef.addRole(role);
        idmGroupRef.setIdmGroupId(idmGroupId);
        idmGroupRefRepository.save(idmGroupRef);
    }

    @Override
    public void removeRoleFromGroup(long roleId, long idmGroupId) {
        LOG.info("removeRoleFromGroup({},{})", roleId, idmGroupId);
        Optional<Role> optionalRoleToBeRemoved = roleRepository.findById(roleId);
        Role role = optionalRoleToBeRemoved.orElseThrow(() -> new CommonsServiceException("Input role with id " + roleId + " cannot be found"));

        Optional<IDMGroupRef> optIdmGroupRef = idmGroupRefRepository.findByIdmGroupId(idmGroupId);
        IDMGroupRef idmGroupRef = optIdmGroupRef.orElseThrow(() -> new CommonsServiceException("Idm group with id: " + idmGroupId + " cannot be found."));
        idmGroupRef.removeRole(role);
        if (idmGroupRef.getRoles().isEmpty()) {
            idmGroupRefRepository.delete(idmGroupRef);
        }
    }

    @Override
    public Set<Role> getRolesOfGroups(List<Long> groupsIds) {
        LOG.info("getRolesOfGroups({})", groupsIds);
        Assert.notEmpty(groupsIds, "Input list of groups ids must not be empty.");
        Set<Role> roles = new HashSet<>();
        for (Long id : groupsIds) {
            Optional<IDMGroupRef> groupRef = idmGroupRefRepository.findByIdmGroupId(id);
            groupRef.ifPresent(group -> roles.addAll(group.getRoles()));
        }
        return roles;
    }

}
