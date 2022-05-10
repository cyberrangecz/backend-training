package cz.muni.ics.kypo.training.api.dto.assessmentlevel.preview;

import com.fasterxml.jackson.annotation.JsonInclude;
import cz.muni.ics.kypo.training.validation.Ordered;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionChoicePreviewDTO implements Serializable, Ordered {

    @ApiModelProperty(value = "Main identifier of the question choice.", example = "1")
    private Long id;
    @ApiModelProperty(value = "Text content of the question choice.", example = "Yes")
    private String text;
    @ApiModelProperty(value = "The order of the choice in question of type MCQ or FFQ", example = "1")
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

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return "QuestionChoicePreviewDTO{" +
                "id=" + this.getId() +
                ", text='" + this.getText() + '\'' +
                ", order=" + this.getOrder() +
                '}';
    }
}
