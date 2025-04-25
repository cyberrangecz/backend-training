package cz.cyberrange.platform.training.persistence.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "team_sandbox_lock")
public class TeamSandboxLock {

    @Id
    @Column(name = "team_id")
    private Long teamId;

    @Setter
    @Column(name = "sandbox_instance_id")
    private String sandboxInstanceRefId;

    @Setter
    @Column(name = "sandbox_instance_allocation_id")
    private Integer sandboxInstanceAllocationId;

    public TeamSandboxLock(Long teamId) {
        this.teamId = teamId;
    }

}
