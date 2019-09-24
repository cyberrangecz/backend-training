package cz.muni.ics.kypo.training.api.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

/**
 * Represents basic info about Sandbox pools.
 */
public class SandboxPoolInfo {
    @NotNull
    private Long id;
    @NotNull
    private Long definition;
    @NotNull
    private Long size;
    @NotNull
    @JsonProperty("max_size")
    private Long maxSize;

    /**
     * Gets id.
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets definition.
     *
     * @return the definition
     */
    public Long getDefinition() {
        return definition;
    }

    /**
     * Sets definition.
     *
     * @param definition the definition
     */
    public void setDefinition(Long definition) {
        this.definition = definition;
    }

    /**
     * Gets size.
     *
     * @return the size
     */
    public Long getSize() {
        return size;
    }

    /**
     * Sets size.
     *
     * @param size the size
     */
    public void setSize(Long size) {
        this.size = size;
    }

    /**
     * Gets max size.
     *
     * @return the max size
     */
    public Long getMaxSize() {
        return maxSize;
    }

    /**
     * Sets max size.
     *
     * @param maxSize the max size
     */
    public void setMaxSize(Long maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public String toString() {
        return "SandboxPoolInfo{" +
                "id=" + id +
                ", definition=" + definition +
                ", size='" + size + '\'' +
                ", maxSize=" + maxSize +
                '}';
    }
}
