package cz.cyberrange.platform.training.api.dto.run;

import cz.cyberrange.platform.training.api.responses.SandboxRequestInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** Encapsulates a sandbox a trainee allocated for themselves, as listed on their run overview */
@Data
@Schema(description = "A sandbox the caller allocated for themselves, and how far it is built")
public class TraineeSandboxDTO {

  @Schema(
      description = "Id of the sandbox's allocation unit in the sandbox service.",
      example = "7")
  private Integer id;

  @Schema(example = "4")
  private Long poolId;

  @Schema(
      description = "UUID of the sandbox; absent while it is being built or removed.",
      example = "d2f6b1c4-9a3e-4c07-8b52-1e7a5c9d3f80")
  private String sandboxId;

  @Schema(
      description = "When the sandbox was allocated.",
      example = "2026-10-12T10:15:30.123456+02:00")
  private String createdAt;

  private SandboxRequestInfo allocationRequest;

  @Schema(description = "Absent until the sandbox's removal is requested.")
  private SandboxRequestInfo cleanupRequest;

  @Schema(
      description =
          "True when the sandbox is built, or building or removing it failed, so the caller may"
              + " remove it.",
      example = "true")
  private boolean allowRemove;

  @Schema(description = "Id of the training instance whose pool holds the sandbox.", example = "1")
  private Long trainingInstanceId;

  @Schema(example = "Linux basics")
  private String trainingInstanceTitle;
}
