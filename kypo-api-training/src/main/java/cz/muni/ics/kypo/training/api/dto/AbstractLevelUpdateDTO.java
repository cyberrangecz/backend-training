package cz.muni.ics.kypo.training.api.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.imports.AssessmentLevelImportDTO;
import cz.muni.ics.kypo.training.api.dto.imports.InfoLevelImportDTO;
import cz.muni.ics.kypo.training.api.dto.imports.TrainingLevelImportDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.dto.snapshothook.SnapshotHookDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.api.dto.traininglevel.TrainingLevelDTO;
import cz.muni.ics.kypo.training.api.dto.traininglevel.TrainingLevelUpdateDTO;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Encapsulates information about abstract level.
 * Extended by {@link AssessmentLevelDTO}, {@link TrainingLevelDTO} and {@link InfoLevelDTO}
 *
 */
@ApiModel(value = "AbstractLevelUpdateDTO", subTypes = {TrainingLevelUpdateDTO.class, AssessmentLevelUpdateDTO.class, InfoLevelUpdateDTO.class},
        description = "Superclass for classes TrainingLevelUpdateDTO, AssessmentLevelUpdateDTO and InfoLevelUpdateDTO")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "level_type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TrainingLevelUpdateDTO.class, name = "TRAINING_LEVEL"),
        @JsonSubTypes.Type(value = TrainingLevelUpdateDTO.class, name = "GAME_LEVEL"),
        @JsonSubTypes.Type(value = AssessmentLevelUpdateDTO.class, name = "ASSESSMENT_LEVEL"),
        @JsonSubTypes.Type(value = InfoLevelUpdateDTO.class, name = "INFO_LEVEL")})
public abstract class AbstractLevelUpdateDTO {

    @ApiModelProperty(value = "Main identifier of level.", required = true, example = "1")
    @NotNull(message = "{abstractLevel.id.NotNull.message}")
    protected Long id;
    @ApiModelProperty(value = "Short textual description of the level.", required = true, example = "Training Level1")
    @NotEmpty(message = "{abstractLevel.title.NotEmpty.message}")
    protected String title;
    @ApiModelProperty(value = "Type of the level.", example = "TRAINING_LEVEL")
    protected LevelType levelType;

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
     * Gets level type.
     *
     * @return the {@link LevelType}
     */
    public LevelType getLevelType() {
        return levelType;
    }

    /**
     * Sets level type.
     *
     * @param levelType the {@link LevelType}
     */
    public void setLevelType(LevelType levelType) {
        this.levelType = levelType;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof AbstractLevelUpdateDTO))
            return false;
        AbstractLevelUpdateDTO other = (AbstractLevelUpdateDTO) obj;
        return Objects.equals(id, other.getId());
    }


    @Override
    public String toString() {
        return "AbstractLevelUpdateDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", levelType=" + levelType +
                '}';
    }
}

