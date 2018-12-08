package cz.muni.ics.kypo.training.persistence.model;

import javax.persistence.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Pavel Seda
 */
@Entity
@Table(name = "idm_group_ref")
public class IDMGroupRef {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", unique = true, nullable = false)
    private long idmGroupId;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "idm_group_role", joinColumns = @JoinColumn(name = "idm_group_ref_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getIdmGroupId() {
        return idmGroupId;
    }

    public void setIdmGroupId(long idmGroupId) {
        this.idmGroupId = idmGroupId;
    }

    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IDMGroupRef groupRef = (IDMGroupRef) o;

        if (idmGroupId != groupRef.getIdmGroupId()) return false;
        if (id != null ? !id.equals(groupRef.getId()) : groupRef.getId() != null) return false;
        return roles != null ? roles.equals(groupRef.getRoles()) : groupRef.getRoles() == null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, idmGroupId, roles);
    }

    @Override
    public String toString() {
        return "IDMGroupRef{" +
                "id=" + id +
                ", idmGroupId=" + idmGroupId +
                ", roles=" + roles +
                '}';
    }
}
