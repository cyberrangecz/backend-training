package cz.muni.ics.kypo.training.api.dto.assessmentlevel.question;

import com.fasterxml.jackson.annotation.JsonInclude;
import cz.muni.ics.kypo.training.validation.Ordered;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.util.*;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public Integer getCorrectOptionOrder() {
        return correctOptionOrder;
    }

    public void setCorrectOptionOrder(Integer correctOptionOrder) {
        this.correctOptionOrder = correctOptionOrder;
    }

    @Override
    public String toString() {
        return "ExtendedMatchingStatementDTO{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", order=" + order +
                ", correctOptionOrder=" + correctOptionOrder +
                '}';
    }
}
