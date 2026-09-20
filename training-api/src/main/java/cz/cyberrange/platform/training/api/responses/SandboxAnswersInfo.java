package cz.cyberrange.platform.training.api.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Deserialization target for the answer set the answer-storage service holds for one sandbox. The
 * sandbox is addressed either by its own reference or, where the environment is local and no
 * sandbox reference exists, by the pair of a training instance access token and a trainee.
 */
public class SandboxAnswersInfo {

  @Schema(
      description = "UUID of the sandbox the answers belong to.",
      example = "d2f6b1c4-9a3e-4c07-8b52-1e7a5c9d3f80")
  @JsonProperty("sandbox_ref_id")
  private String sandboxRefId;

  @Schema(
      description = "Access token of the training instance, used when there is no sandbox.",
      example = "token-1234")
  @JsonProperty("access_token")
  private String accessToken;

  @Schema(
      description = "The id the trainee is known by across the platform's services.",
      example = "12")
  @JsonProperty("user_id")
  private Long userId;

  @JsonProperty("sandbox_answers")
  private List<VariantAnswer> variantAnswers;

  public String getSandboxRefId() {
    return sandboxRefId;
  }

  public void setSandboxRefId(String sandboxRefId) {
    this.sandboxRefId = sandboxRefId;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public List<VariantAnswer> getVariantAnswers() {
    return variantAnswers;
  }

  public void setVariantAnswers(List<VariantAnswer> variantAnswers) {
    this.variantAnswers = variantAnswers;
  }

  @Override
  public String toString() {
    return "SandboxAnswersInfo{"
        + "sandboxRefId="
        + sandboxRefId
        + ", accessToken='"
        + accessToken
        + '\''
        + ", userId="
        + userId
        + ", variantAnswers="
        + variantAnswers
        + '}';
  }
}
