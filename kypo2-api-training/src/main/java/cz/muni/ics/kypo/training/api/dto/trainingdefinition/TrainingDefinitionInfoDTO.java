package cz.muni.ics.kypo.training.api.dto.trainingdefinition;

import cz.muni.ics.kypo.training.api.enums.TDState;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

public class TrainingDefinitionInfoDTO {

    private Long id;
    private String title;
    private TDState state;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @ApiModelProperty(value = "Current state of training definition.", example = "UNRELEASED")
    public TDState getState() {
        return state;
    }

    public void setState(TDState state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrainingDefinitionInfoDTO that = (TrainingDefinitionInfoDTO) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(title, that.title) &&
                state == that.state;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, state);
    }

    @Override
    public String toString() {
        return "TrainingDefinitionInfoDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", state=" + state +
                '}';
    }
}
