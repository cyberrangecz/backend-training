package cz.cyberrange.platform.training.api.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * Deserialization target for the lock the sandbox service creates over a pool. Both identifiers are
 * numbered by that service, not by this one, and neither is stored here.
 */
public class LockedPoolInfo {

  /** Identifies the lock itself, as the sandbox service numbers its locks */
  private long id;

  /** Identifies the pool the lock was placed on, in the sandbox service's pool numbering */
  @JsonProperty(value = "pool_id")
  private long poolId;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getPoolId() {
    return poolId;
  }

  public void setPoolId(long poolId) {
    this.poolId = poolId;
  }

  @Override
  public String toString() {
    return "LockedPoolInfo{" + "id=" + id + ", poolId=" + poolId + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof LockedPoolInfo)) return false;
    LockedPoolInfo that = (LockedPoolInfo) o;
    return getId() == that.getId() && getPoolId() == that.getPoolId();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getPoolId());
  }
}
