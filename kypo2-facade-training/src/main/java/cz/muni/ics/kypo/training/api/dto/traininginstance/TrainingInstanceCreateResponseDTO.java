package cz.muni.ics.kypo.training.api.dto.traininginstance;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "NewTrainingInstanceDTO", description = "Newly created Training Instance.")
public class TrainingInstanceCreateResponseDTO {

    private Long id;
    private String keyword;

    @ApiModelProperty(value = "Main identifier of training instance.")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ApiModelProperty(value = "Generated keyword which will be used for accessing training run.")
    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public String toString() {
        return "TrainingInstanceCreateResponseDTO{" + "id=" + id + ", keyword='" + keyword + '\'' + '}';
    }
}
