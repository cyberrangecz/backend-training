package cz.muni.ics.kypo.training.api.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class LockedPoolInfo {

    @NotNull(message = "{lockedPoolInfo.id.NotNull.message}")
    private long id;
    @NotNull(message = "{lockedPoolInfo.poolId.NotNull.message}")
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
        return "LockedPoolInfo{" +
                "id=" + id +
                ", poolId=" + poolId +
                '}';
    }
}
