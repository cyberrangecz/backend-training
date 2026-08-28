package cz.cyberrange.platform.training.api.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Deserialization target for a single sandbox as the sandbox service describes it. The identifiers
 * it carries are that service's own; the sandbox and its allocation are kept on a training run as
 * opaque references to the environment the trainee works in, while the lock reference is read by
 * nothing.
 */
public class SandboxInfo {

  /** Identifies the sandbox in the sandbox service; textual rather than numeric */
  private String id;

  @JsonProperty(value = "lock_id")
  private Integer lockId;

  /** Identifies the allocation the sandbox was created by, in the sandbox service's numbering */
  @JsonProperty(value = "allocation_unit_id")
  private Integer allocationUnitId;

  public String getId() {
    return id;
  }

  public void setId(String id) {
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
    return "SandboxInfo{"
        + "id="
        + id
        + ", lockId="
        + lockId
        + ", allocationUnitId="
        + allocationUnitId
        + '}';
  }
}
