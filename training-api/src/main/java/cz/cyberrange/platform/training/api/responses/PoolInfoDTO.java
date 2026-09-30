package cz.cyberrange.platform.training.api.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Deserialization target for one sandbox pool as the sandbox service describes it. This service
 * reads only the lock reference from it, to release a lock it holds; the remaining properties are
 * accepted so that the response parses.
 */
public class PoolInfoDTO {
  private Long id;

  @JsonProperty(value = "definition_id")
  private Long definitionId;

  private Long size;

  @JsonProperty(value = "max_size")
  private Long maxSize;

  /** Identifies the lock currently held over the pool; null when the pool is not locked */
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
    return "PoolInfoDto{"
        + "id="
        + id
        + ", definitionId="
        + definitionId
        + ", size="
        + size
        + ", maxSize="
        + maxSize
        + ", lockId="
        + lockId
        + ", rev="
        + rev
        + ", revSha="
        + revSha
        + '}';
  }
}
