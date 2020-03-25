package cz.muni.ics.kypo.training.api.responses;

import javax.validation.constraints.NotNull;

/**
 * Represents basic information about Sandbox.
 */
public class SandboxInfo {

    @NotNull(message = "{sandboxInfo.id.NotNull.message}")
    private Long id;
    @NotNull(message = "{sandboxInfo.locked.NotNull.message}")
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
                ", locked=" + locked +
                '}';
    }
}
