package cz.muni.ics.kypo.training.api.dto.trainingdefinition;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;
import java.util.Set;

@ApiModel(value = "TrainingDefinitionMitreTechniqueDTO", description = "Represent training definition and all used mitre techniques.")
public class TrainingDefinitionMitreTechniquesDTO {

    @ApiModelProperty(value = "Unique identifier of the training definition.", example = "Delta")
    private Long id;
    @ApiModelProperty(value = "Title of the training definition.", example = "Delta")
    private String title;
    @ApiModelProperty(value = "Indicates if the training definition has been played by user.", example = "true")
    private boolean played;
    @ApiModelProperty(value = "List of MITRE technique keys.", example = "[TA0043.T1595, TA0042.T1588.006]")
    private Set<String> mitreTechniques;

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

    public boolean isPlayed() {
        return played;
    }

    public void setPlayed(boolean played) {
        this.played = played;
    }

    public Set<String> getMitreTechniques() {
        return mitreTechniques;
    }

    public void setMitreTechniques(Set<String> mitreTechniques) {
        this.mitreTechniques = mitreTechniques;
    }

    @Override
    public String toString() {
        return "TrainingDefinitionMitreTechniquesDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", played=" + played +
                ", mitreTechniques=" + mitreTechniques +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrainingDefinitionMitreTechniquesDTO that = (TrainingDefinitionMitreTechniquesDTO) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getTitle(), that.getTitle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTitle());
    }
}
