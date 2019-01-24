package cz.muni.ics.kypo.training.utils;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class SandboxInfo {

    @NotNull
    private Long id;
    @NotEmpty
    private String status;
    @NotNull
    private Long pool;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getPool() {
        return pool;
    }

    public void setPool(Long pool) {
        this.pool = pool;
    }

    @Override
    public String toString() {
        return "SandboxInfo{" +
                "id=" + id +
                ", status='" + status + '\'' +
                ", pool=" + pool +
                '}';
    }
}
