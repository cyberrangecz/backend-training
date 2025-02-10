package cz.cyberrange.platform.training.api.dto.hint;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Encapsulates information about Hint.
 */
@EqualsAndHashCode
@Getter
@Setter
@ToString
@ApiModel(value = "HintDTO", description = "A brief textual description to aid the participant.")
public class HintDTO {

    @ApiModelProperty(value = "Main identifier of hint.", example = "1")
    private Long id;
    @ApiModelProperty(value = "Short textual description of the hint.", example = "Hint1")
    @NotEmpty(message = "{hint.title.NotEmpty.message}")
    private String title;
    @ApiModelProperty(value = "The information and experiences that are directed towards a participant.", example = "Very good advice")
    @NotEmpty(message = "{hint.content.NotEmpty.message}")
    private String content;
    @NotNull(message = "{hint.hintPenalty.NotNull.message}")
    @Min(value = 0, message = "{hint.hintPenalty.Min.message}")
    @Max(value = 100, message = "{hint.hintPenalty.Max.message}")
    @ApiModelProperty(value = "The number of points the participant loses after receiving the hint.", example = "10")
    private Integer hintPenalty;
    @ApiModelProperty(value = "The order of hint in training level", example = "1")
    @Min(value = 0, message = "{hint.order.Min.message}")
    private int order;
}
