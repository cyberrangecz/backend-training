package cz.muni.ics.kypo.training.api.dto;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Pavel Seda
 */
public class IDMGroupRefDTO {

    private Long id;
    private long idmGroupRefId;
    private Set<RoleDTO> roles = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getIdmGroupId() {
        return idmGroupRefId;
    }

    public void setIdmGroupId(long idmGroupId) {
        this.idmGroupRefId = idmGroupId;
    }

    public Set<RoleDTO> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleDTO> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "IDMGroupRefDTO{" +
                "id=" + id +
                ", idmGroupId=" + idmGroupRefId +
                ", roles=" + roles +
                '}';
    }
}
