package cz.muni.ics.kypo.training.api.dto.assessmentlevel.question;

import com.fasterxml.jackson.annotation.JsonInclude;
import cz.muni.ics.kypo.training.validation.Ordered;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionChoiceDTO implements Serializable, Ordered {

    @ApiModelProperty(value = "Main identifier of the question choice.", example = "1")
    private Long id;
    @ApiModelProperty(value = "Text content of the question choice.", example = "Yes")
    @NotEmpty(message = "{questionChoice.text.NotEmpty.message}")
    private String text;
    @ApiModelProperty(value = "Sign if the choice is correct.", example = "true")
    @NotNull(message = "{questionChoice.correct.NotNull.message}")
    private Boolean correct;
    @ApiModelProperty(value = "The order of the choice in question of type MCQ or FFQ", example = "1")
    @Min(value = 0, message = "{questionChoice.order.Min.message}")
    private int order;

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

    public Boolean getCorrect() {
        return correct;
    }

    public void setCorrect(Boolean correct) {
        this.correct = correct;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return "QuestionChoice{" +
                "id=" + this.getId() +
                ", text='" + this.getText() + '\'' +
                ", correct=" + this.getCorrect() +
                ", order=" + this.getOrder() +
                '}';
    }
}
