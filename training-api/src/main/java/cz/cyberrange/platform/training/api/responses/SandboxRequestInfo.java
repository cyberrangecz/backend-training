package cz.cyberrange.platform.training.api.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * Deserialization target for the request that builds or removes the sandbox of an allocation unit,
 * as the sandbox service describes it. Each stage is reported as IN_QUEUE, RUNNING, FINISHED or
 * FAILED; a cancelled stage is reported as FAILED.
 */
@Data
@Schema(description = "Progress of the request that builds or removes a sandbox")
public class SandboxRequestInfo {

  @Schema(example = "3")
  private Integer id;

  @JsonProperty("allocation_unit_id")
  @Schema(example = "7")
  private Integer allocationUnitId;

  @Schema(
      description = "State of each stage, in order: IN_QUEUE, RUNNING, FINISHED or FAILED.",
      example = "[\"FINISHED\", \"RUNNING\", \"IN_QUEUE\"]")
  private List<String> stages = new ArrayList<>();
}
