package cz.muni.ics.kypo.training.api.responses;

import javax.validation.constraints.NotNull;

public class LockedPoolInfo {

    @NotNull(message = "{lockedPoolInfo.id.NotNull.message}")
    private long id;
    @NotNull(message = "{lockedPoolInfo.pool.NotNull.message}")
    private long pool;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPool() {
        return pool;
    }

    public void setPool(long pool) {
        this.pool = pool;
    }
}
