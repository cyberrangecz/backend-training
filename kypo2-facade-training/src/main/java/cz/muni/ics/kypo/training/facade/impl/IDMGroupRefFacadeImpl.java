package cz.muni.ics.kypo.training.facade.impl;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.training.annotations.TransactionalRO;
import cz.muni.ics.kypo.training.annotations.TransactionalWO;
import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.IDMGroupRefDTO;
import cz.muni.ics.kypo.training.api.dto.RoleDTO;
import cz.muni.ics.kypo.training.exception.CommonsFacadeException;
import cz.muni.ics.kypo.training.exceptions.CommonsServiceException;
import cz.muni.ics.kypo.training.facade.IDMGroupRefFacade;
import cz.muni.ics.kypo.training.mapping.IDMGroupRefMapper;
import cz.muni.ics.kypo.training.mapping.RoleMapper;
import cz.muni.ics.kypo.training.persistence.model.IDMGroupRef;
import cz.muni.ics.kypo.training.persistence.model.Role;
import cz.muni.ics.kypo.training.service.IDMGroupRefService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * @author Pavel Seda
 */
@Service
@Transactional
public class IDMGroupRefFacadeImpl implements IDMGroupRefFacade {

    private static final Logger LOG = LoggerFactory.getLogger(IDMGroupRefFacade.class);
    private IDMGroupRefService groupRefService;
    private IDMGroupRefMapper idmGroupRefMapper;
    private RoleMapper roleMapper;

    @Autowired
    public IDMGroupRefFacadeImpl(IDMGroupRefService groupRefService, IDMGroupRefMapper idmGroupRefMapper, RoleMapper roleMapper) {
        this.groupRefService = groupRefService;
        this.idmGroupRefMapper = idmGroupRefMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    @TransactionalWO
    public void delete(Long idmGroupId) {
        groupRefService.delete(idmGroupId);
    }

    @Override
    @TransactionalRO
    public PageResultResource<IDMGroupRefDTO> getAllGroups(Predicate predicate, Pageable pageable) {
        try {
            Page<IDMGroupRef> groups = groupRefService.getAllGroups(predicate, pageable);
            return idmGroupRefMapper.mapToPageResultGroupDTO(groups);
        } catch (CommonsServiceException ex) {
            throw new CommonsFacadeException(ex);
        }
    }

    @Override
    @TransactionalRO
    public Set<RoleDTO> getRolesOfGroups(List<Long> groupsIds) {
        try {
            Set<Role> roles = groupRefService.getRolesOfGroups(groupsIds);
            LOG.info("Roles of given groups with ids: {} have been loaded.", groupsIds);
            return roleMapper.mapToRoleDTOSet(roles);
        } catch (CommonsServiceException ex) {
            throw new CommonsFacadeException(ex.getMessage());
        }
    }

    @Override
    @TransactionalWO
    public void assignRoleToGroup(long roleId, long idmGroupId) {
        try {
            groupRefService.assignRoleToGroup(roleId, idmGroupId);
            LOG.info("Role with id: {} has been assigned to group with id: {}.", roleId, idmGroupId);
        } catch (CommonsServiceException ex) {
            throw new CommonsFacadeException(ex.getMessage());
        }

    }

    @Override
    @TransactionalWO
    public void removeRoleFromGroup(long roleId, long idmGroupId) {
        try {
            groupRefService.removeRoleFromGroup(roleId, idmGroupId);
            LOG.info("Role with id: {} has been removed.", roleId);
        } catch (CommonsServiceException ex) {
            throw new CommonsFacadeException(ex.getMessage());
        }
    }


}
