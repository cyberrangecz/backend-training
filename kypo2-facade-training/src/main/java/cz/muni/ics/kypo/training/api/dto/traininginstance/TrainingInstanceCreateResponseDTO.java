package cz.muni.ics.kypo.training.api.dto.traininginstance;

import io.swagger.annotations.ApiModel;

@ApiModel(value = "NewTrainingInstanceDTO", description = "Newly created Training Instance.")
public class TrainingInstanceCreateResponseDTO {

    private Long id;
    private String keyword;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
