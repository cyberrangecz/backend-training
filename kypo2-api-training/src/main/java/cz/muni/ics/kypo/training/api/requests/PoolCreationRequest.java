package cz.muni.ics.kypo.training.api.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Information about Pool creation request.
 */
public class PoolCreationRequest {
    @JsonProperty("definition")
    private Long sandboxDefinitionId;
    @JsonProperty("max_size")
    private int poolSize;

    /**
     * Instantiates a new Pool creation request.
     */
    public PoolCreationRequest() {
    }

    /**
     * Gets sandbox definition id.
     *
     * @return the sandbox definition id
     */
    public Long getSandboxDefinitionId() {
        return sandboxDefinitionId;
    }

    /**
     * Sets sandbox definition id.
     *
     * @param sandboxDefinitionId the sandbox definition id
     */
    public void setSandboxDefinitionId(Long sandboxDefinitionId) {
        this.sandboxDefinitionId = sandboxDefinitionId;
    }

    /**
     * Gets pool size.
     *
     * @return the pool size
     */
    public int getPoolSize() {
        return poolSize;
    }

    /**
     * Sets pool size.
     *
     * @param poolSize the pool size
     */
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
