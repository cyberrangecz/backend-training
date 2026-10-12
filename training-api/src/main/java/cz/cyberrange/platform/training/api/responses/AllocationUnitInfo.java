package cz.cyberrange.platform.training.api.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Deserialization target for an allocation unit as the sandbox service describes it: one sandbox of
 * a pool together with the requests that build and remove it. The sandbox id is absent while the
 * sandbox is being built and once its removal has started.
 */
@Data
public class AllocationUnitInfo {

  private Integer id;

  @JsonProperty("pool_id")
  private Long poolId;

  @JsonProperty("sandbox_id")
  private String sandboxId;

  /** When the unit was created, as an ISO 8601 string passed through unchanged */
  @JsonProperty("created_at")
  private String createdAt;

  @JsonProperty("allocation_request")
  private SandboxRequestInfo allocationRequest;

  /** Absent until someone asks for the sandbox to be removed */
  @JsonProperty("cleanup_request")
  private SandboxRequestInfo cleanupRequest;
}
