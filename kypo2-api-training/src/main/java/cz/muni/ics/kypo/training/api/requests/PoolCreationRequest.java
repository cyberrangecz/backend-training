package cz.muni.ics.kypo.training.api.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PoolCreationRequest {
    @JsonProperty("definition")
    private Long sandboxDefinitionId;
    @JsonProperty("max_size")
    private int poolSize;

    public PoolCreationRequest() {
    }

    public Long getSandboxDefinitionId() {
        return sandboxDefinitionId;
    }

    public void setSandboxDefinitionId(Long sandboxDefinitionId) {
        this.sandboxDefinitionId = sandboxDefinitionId;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    @Override
    public String toString() {
        return "PoolCreationRequest{" +
                "sandboxDefinitionId=" + sandboxDefinitionId +
                ", poolSize=" + poolSize +
                '}';
    }
}
