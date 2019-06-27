package cz.muni.ics.kypo.training.api.dto.trainingdefinition;

import cz.muni.ics.kypo.training.api.enums.TDState;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * Encapsulates basic information about Training Definition.
 */
public class TrainingDefinitionInfoDTO {

    private Long id;
    private String title;
    private TDState state;

    /**
     * Gets id.
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets title.
     *
     * @param title the title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets state.
     *
     * @return the {@link TDState}
     */
    @ApiModelProperty(value = "Current state of training definition.", example = "UNRELEASED")
    public TDState getState() {
        return state;
    }

    /**
     * Sets state.
     *
     * @param state the {@link TDState}
     */
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
