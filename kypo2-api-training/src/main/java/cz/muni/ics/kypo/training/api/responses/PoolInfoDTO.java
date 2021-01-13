package cz.muni.ics.kypo.training.api.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class PoolInfoDTO {
    @NotNull(message = "{poolInfo.id.NotNull.message}")
    private Long id;
    @NotNull(message = "{poolInfo.definitionId.NotNull.message}")
    @JsonProperty(value = "definition_id")
    private Long definitionId;
    @NotNull(message = "{poolInfo.size.NotNull.message}")
    private Long size;
    @NotNull(message = "{poolInfo.maxSize.NotNull.message}")
    @JsonProperty(value = "max_size")
    private Long maxSize;
    @NotNull(message = "{poolInfo.lockId.NotNull.message}")
    @JsonProperty(value = "lock_id")
    private Long lockId;
    @JsonProperty(value = "rev")
    private String rev;
    @JsonProperty(value = "rev_sha")
    private String revSha;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDefinitionId() {
        return definitionId;
    }

    public void setDefinitionId(Long definitionId) {
        this.definitionId = definitionId;
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

    public Long getLockId() {
        return lockId;
    }

    public void setLockId(Long lockId) {
        this.lockId = lockId;
    }

    public String getRev() {
        return rev;
    }

    public void setRev(String rev) {
        this.rev = rev;
    }

    public String getRevSha() {
        return revSha;
    }

    public void setRevSha(String revSha) {
        this.revSha = revSha;
    }

    @Override
    public String toString() {
        return "PoolInfoDto{" +
                "id=" + id +
                ", definitionId=" + definitionId +
                ", size=" + size +
                ", maxSize=" + maxSize +
                ", lockId=" + lockId +
                ", rev=" + rev +
                ", revSha=" + revSha +
                '}';
    }
}
