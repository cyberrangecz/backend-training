package cz.muni.ics.kypo.training.api.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class SandboxAnswersInfo {

    @ApiModelProperty(value = "The identifier of a sandbox for that we store the answers", example = "12")
    @JsonProperty("sandbox_ref_id")
    private Long sandboxRefId;
    @ApiModelProperty(value = "The answers for given sandbox")
    @JsonProperty("sandbox_answers")
    private List<VariantAnswer> variantAnswers;

    public Long getSandboxRefId() {
        return sandboxRefId;
    }

    public void setSandboxRefId(Long sandboxRefId) {
        this.sandboxRefId = sandboxRefId;
    }

    public List<VariantAnswer> getVariantAnswers() {
        return variantAnswers;
    }

    public void setVariantAnswers(List<VariantAnswer> variantAnswers) {
        this.variantAnswers = variantAnswers;
    }

    @Override
    public String toString() {
        return "SandboxAnswersInfo{" +
                "sandboxRefId=" + sandboxRefId +
                ", variantAnswers=" + variantAnswers +
                '}';
    }
}
