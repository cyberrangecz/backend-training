package cz.muni.ics.kypo.training.api.responses;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Represents basic information about Sandbox.
 */
public class SandboxInfo {

    @NotNull
    private Long id;
    @NotEmpty
    private String status;
    @NotNull
    private Long pool;
    @NotNull
    private boolean locked;

    /**
     * Gets id of sandbox.
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets id of sandbox.
     *
     * @param id the id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets status.
     *
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets status.
     *
     * @param status the status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Gets pool.
     *
     * @return the pool
     */
    public Long getPool() {
        return pool;
    }

    /**
     * Sets pool.
     *
     * @param pool the pool
     */
    public void setPool(Long pool) {
        this.pool = pool;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @Override
    public String toString() {
        return "SandboxInfo{" +
                "id=" + id +
                ", status='" + status + '\'' +
                ", pool=" + pool +
                ", locked=" + locked +
                '}';
    }
}
