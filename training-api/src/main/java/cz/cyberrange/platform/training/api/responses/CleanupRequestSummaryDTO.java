package cz.cyberrange.platform.training.api.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Summary of a cleanup request (from sandbox-service).
 * Stages: IN_QUEUE, RUNNING, FINISHED, FAILED.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CleanupRequestSummaryDTO {

    private Integer id;
    @JsonProperty("allocation_unit_id")
    private Integer allocationUnitId;
    private List<String> stages;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAllocationUnitId() {
        return allocationUnitId;
    }

    public void setAllocationUnitId(Integer allocationUnitId) {
        this.allocationUnitId = allocationUnitId;
    }

    public List<String> getStages() {
        return stages;
    }

    public void setStages(List<String> stages) {
        this.stages = stages;
    }
}
