package cz.muni.ics.kypo.training.api.responses;

import javax.validation.constraints.NotNull;

public class PoolInfoDto {
    @NotNull(message = "{poolInfoDTO.id.NotNull.message}")
    private Long id;
    @NotNull(message = "{poolInfoDTO.definition.NotNull.message}")
    private Long definition;
    @NotNull(message = "{poolInfoDTO.size.NotNull.message}")
    private Long size;
    @NotNull(message = "{poolInfoDTO.maxSize.NotNull.message}")
    private Long maxSize;
    @NotNull(message = "{poolInfoDTO.lock.NotNull.message}")
    private Long lock;

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

    public Long getLock() {
        return lock;
    }

    public void setLock(Long lock) {
        this.lock = lock;
    }

    @Override
    public String toString() {
        return "PoolInfoDto{" +
                "id=" + id +
                ", definition=" + definition +
                ", size=" + size +
                ", maxSize=" + maxSize +
                ", lock=" + lock +
                '}';
    }
}
