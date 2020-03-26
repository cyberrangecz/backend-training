package cz.muni.ics.kypo.training.api.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

/**
 * Represents basic information about Sandbox.
 */
public class SandboxInfo {

    @NotNull(message = "{sandboxInfo.id.NotNull.message}")
    private Long id;
    @JsonProperty(value = "lock_id")
    private Integer lockId;
    @JsonProperty(value = "allocation_unit_id")
    private Integer allocationUnitId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getLockId() {
        return lockId;
    }

    public void setLockId(Integer lockId) {
        this.lockId = lockId;
    }

    public Integer getAllocationUnitId() {
        return allocationUnitId;
    }

    public void setAllocationUnitId(Integer allocationUnitId) {
        this.allocationUnitId = allocationUnitId;
    }

    @Override
    public String toString() {
        return "SandboxInfo{" +
                "id=" + id +
                ", lockId=" + lockId +
                ", allocationUnitId=" + allocationUnitId +
                '}';
    }
}
