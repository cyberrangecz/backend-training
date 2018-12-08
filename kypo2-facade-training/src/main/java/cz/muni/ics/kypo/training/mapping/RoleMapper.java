package cz.muni.ics.kypo.training.mapping;

import cz.muni.ics.kypo.training.api.PageResultResource;
import cz.muni.ics.kypo.training.api.dto.RoleDTO;
import cz.muni.ics.kypo.training.persistence.model.Role;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.*;

/**
 * @author Roman Oravec
 */
@Mapper(componentModel = "spring")
public interface RoleMapper extends ParentMapper {

    Role mapToRole(RoleDTO roleDTO);

    RoleDTO mapToRoleDTO(Role role);

    List<Role> mapToRoleList(Collection<RoleDTO> roles);

    List<RoleDTO> mapToRoleDTOList(Collection<Role> roles);

    Set<Role> mapToRoleSet(Collection<RoleDTO> roles);

    Set<RoleDTO> mapToRoleDTOSet(Collection<Role> roles);

    default Optional<Role> mapToRoleOptional(RoleDTO role){
        return Optional.ofNullable(mapToRole(role));
    }

    default Optional<RoleDTO> mapToRoleDTOOptional(Role role){
        return Optional.ofNullable(mapToRoleDTO(role));
    }

    default Page<Role> mapToRolePage(Page<RoleDTO> roles){
        List<Role> mappedRoles = mapToRoleList(roles.getContent());
        return new PageImpl<>(mappedRoles, roles.getPageable(), mappedRoles.size());
    }

    default Page<RoleDTO> mapToRolePageDTO(Page<Role> roles){
        List<RoleDTO> mappedRoles = mapToRoleDTOList(roles.getContent());
        return new PageImpl<>(mappedRoles, roles.getPageable(), mappedRoles.size());
    }

    default PageResultResource<RoleDTO> mapToPageResultRoleDTO(Page<Role> roles){
        List<RoleDTO> mappedRoles = new ArrayList<>();
        roles.forEach(role -> mappedRoles.add(mapToRoleDTO(role)));
        return new PageResultResource<>(mappedRoles, createPagination(roles));
    }

}
