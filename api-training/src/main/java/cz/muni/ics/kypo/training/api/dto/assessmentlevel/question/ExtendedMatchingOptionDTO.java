package cz.muni.ics.kypo.training.api.dto.assessmentlevel.question;

import com.fasterxml.jackson.annotation.JsonInclude;
import cz.muni.ics.kypo.training.validation.Ordered;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.util.HashSet;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExtendedMatchingOptionDTO implements Ordered {

    @ApiModelProperty(value = "Main identifier of the extended matching option.", example = "1")
    private Long id;
    @ApiModelProperty(value = "Text content of the extended matching option.", example = "22")
    @NotEmpty(message = "{emiOption.text.NotEmpty.message}")
    private String text;
    @ApiModelProperty(value = "The order of the option in question of type EMI.", example = "0")
    @Min(value = 0, message = "{emiOption.order.Min.message}")
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
        return "EMIOption{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", order=" + order +
                '}';
    }
}
