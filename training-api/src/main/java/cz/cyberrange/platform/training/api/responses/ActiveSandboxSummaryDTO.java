package cz.cyberrange.platform.training.api.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Summary of an active sandbox allocation unit (from sandbox-service by-creator listing).
 * Used for single-sandbox-per-user: list active sandboxes for a trainee.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActiveSandboxSummaryDTO {

    private Integer id;
    @JsonProperty("pool_id")
    private Long poolId;
    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
    @JsonProperty("created_by_sub")
    private String createdBySub;
    @JsonProperty("sandbox_id")
    private String sandboxId;
    @JsonProperty("allocation_request")
    private AllocationRequestSummaryDTO allocationRequest;
    @JsonProperty("cleanup_request")
    private CleanupRequestSummaryDTO cleanupRequest;

    /** When false, build or cleanup is in progress; show current stage. When true, show "Remove sandbox". */
    @JsonProperty("allow_remove")
    private boolean allowRemove;

    /** Training instance title for display (e.g. "Security Testing I - In-class test"). Filled when we have run info. */
    @JsonProperty("training_instance_title")
    private String trainingInstanceTitle;
    @JsonProperty("training_instance_id")
    private Long trainingInstanceId;
    /** When true, this sandbox belongs to a managed training instance; do not count toward single-allocation quota for non-managed. */
    @JsonProperty("training_instance_managed")
    private Boolean trainingInstanceManaged;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getPoolId() {
        return poolId;
    }

    public void setPoolId(Long poolId) {
        this.poolId = poolId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBySub() {
        return createdBySub;
    }

    public void setCreatedBySub(String createdBySub) {
        this.createdBySub = createdBySub;
    }

    public String getSandboxId() {
        return sandboxId;
    }

    public void setSandboxId(String sandboxId) {
        this.sandboxId = sandboxId;
    }

    public AllocationRequestSummaryDTO getAllocationRequest() {
        return allocationRequest;
    }

    public void setAllocationRequest(AllocationRequestSummaryDTO allocationRequest) {
        this.allocationRequest = allocationRequest;
    }

    public CleanupRequestSummaryDTO getCleanupRequest() {
        return cleanupRequest;
    }

    public void setCleanupRequest(CleanupRequestSummaryDTO cleanupRequest) {
        this.cleanupRequest = cleanupRequest;
    }

    public boolean isAllowRemove() {
        return allowRemove;
    }

    public void setAllowRemove(boolean allowRemove) {
        this.allowRemove = allowRemove;
    }

    public String getTrainingInstanceTitle() {
        return trainingInstanceTitle;
    }

    public void setTrainingInstanceTitle(String trainingInstanceTitle) {
        this.trainingInstanceTitle = trainingInstanceTitle;
    }

    public Long getTrainingInstanceId() {
        return trainingInstanceId;
    }

    public void setTrainingInstanceId(Long trainingInstanceId) {
        this.trainingInstanceId = trainingInstanceId;
    }

    public Boolean getTrainingInstanceManaged() {
        return trainingInstanceManaged;
    }

    public void setTrainingInstanceManaged(Boolean trainingInstanceManaged) {
        this.trainingInstanceManaged = trainingInstanceManaged;
    }
}
