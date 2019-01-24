package cz.muni.ics.kypo.training.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDefinition() {
        return definition;
    }

    public void setDefinition(Long definition) {
        this.definition = definition;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Long getMaxSize() {
        return maxSize;
    }

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
