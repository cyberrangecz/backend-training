package cz.cyberrange.platform.training.api.dto.assessmentlevel.question;

import com.fasterxml.jackson.annotation.JsonInclude;
import cz.cyberrange.platform.training.api.validation.Ordered;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExtendedMatchingStatementDTO implements Ordered {

    @ApiModelProperty(value = "Main identifier of the extended matching statement.", example = "1")
    private Long id;
    @ApiModelProperty(value = "Text content of the extended matching statement.", example = "SSH")
    @NotEmpty(message = "{emiStatement.text.NotEmpty.message}")
    private String text;
    @ApiModelProperty(value = "The order of the statement in question of type EMI.", example = "0")
    @Min(value = 0, message = "{emiStatement.order.Min.message}")
    private int order;
    @ApiModelProperty(value = "The order of the correct option in the list of extended matching options.", example = "0")
    @Min(value = 0, message = "{emiStatement.correctOptionOrder.Min.message}")
    private Integer correctOptionOrder;
}
