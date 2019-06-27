package cz.muni.ics.kypo.training.api.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import cz.muni.ics.kypo.training.api.dto.assessmentlevel.AssessmentLevelDTO;
import cz.muni.ics.kypo.training.api.dto.gamelevel.GameLevelDTO;
import cz.muni.ics.kypo.training.api.dto.infolevel.InfoLevelDTO;

import java.util.Objects;

import cz.muni.ics.kypo.training.api.dto.snapshothook.SnapshotHookDTO;
import cz.muni.ics.kypo.training.api.dto.trainingdefinition.TrainingDefinitionDTO;
import cz.muni.ics.kypo.training.api.enums.LevelType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Encapsulates information about abstract level.
 * Extended by {@link AssessmentLevelDTO}, {@link GameLevelDTO} and {@link InfoLevelDTO}
 *
 * @author Pavel Šeda (441048)
 */
@ApiModel(value = "AbstractLevelDTO", subTypes = {GameLevelDTO.class, InfoLevelDTO.class, AssessmentLevelDTO.class},
        description = "Superclass for classes GameLevelDTO, AssessmentLevelDTO and InfoLevelDTO")
@JsonSubTypes({
        @JsonSubTypes.Type(value = GameLevelDTO.class, name = "GameLevelDTO"),
        @JsonSubTypes.Type(value = AssessmentLevelDTO.class, name = "AssessmentLevelDTO"),
        @JsonSubTypes.Type(value = InfoLevelDTO.class, name = "InfoLevelDTO")})
public class AbstractLevelDTO {

    protected Long id;
    protected String title;
    protected int maxScore;
    protected SnapshotHookDTO snapshotHook;
    protected LevelType levelType;
    protected int estimatedDuration;
    protected TrainingDefinitionDTO trainingDefinition;
    protected int order;

    /**
     * Gets id.
     *
     * @return the id
     */
    @ApiModelProperty(value = "Main identifier of level.", example = "1")
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
    @ApiModelProperty(value = "Short textual description of the level.", example = "Game Level1")
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
     * Gets max score.
     *
     * @return the max score
     */
    @ApiModelProperty(value = "The maximum score a participant can achieve during a level.", example = "20")
    public int getMaxScore() {
        return maxScore;
    }

    /**
     * Sets max score.
     *
     * @param maxScore the max score
     */
    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    /**
     * Gets snapshot hook.
     *
     * @return the {@link SnapshotHookDTO}
     */
    public SnapshotHookDTO getSnapshotHook() {
        return snapshotHook;
    }

    /**
     * Sets snapshot hook.
     *
     * @param snapshotHook the {@link SnapshotHookDTO}
     */
    public void setSnapshotHook(SnapshotHookDTO snapshotHook) {
        this.snapshotHook = snapshotHook;
    }

    /**
     * Gets level type.
     *
     * @return the {@link LevelType}
     */
    @ApiModelProperty(value = "Type of the level.", example = "GAME")
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

    /**
     * Gets estimated duration.
     *
     * @return the estimated duration
     */
    @ApiModelProperty(value = "Estimated time taken by the player to resolve the level.", example = "5")
    public int getEstimatedDuration() {
        return estimatedDuration;
    }

    /**
     * Sets estimated duration.
     *
     * @param estimatedDuration the estimated duration
     */
    public void setEstimatedDuration(int estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    /**
     * Gets training definition.
     *
     * @return the {@link TrainingDefinitionDTO}
     */
    public TrainingDefinitionDTO getTrainingDefinition() {
        return trainingDefinition;
    }

    /**
     * Sets training definition.
     *
     * @param trainingDefinition the {@link TrainingDefinitionDTO}
     */
    public void setTrainingDefinition(TrainingDefinitionDTO trainingDefinition) {
        this.trainingDefinition = trainingDefinition;
    }

    /**
     * Gets order number of level that is compared with order numbers of other levels associated with same definition.
     * First level from definition has order of 0
     *
     * @return the order
     */
    public int getOrder() {
        return order;
    }

    /**
     * Sets order number of level that is compared with order numbers of other levels associated with same definition.
     * First level from definition has order of 0
     *
     * @param order the order
     */
    public void setOrder(int order) {
        this.order = order;
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
        if (!(obj instanceof AbstractLevelDTO))
            return false;
        AbstractLevelDTO other = (AbstractLevelDTO) obj;
        return Objects.equals(id, other.getId());
    }

    @Override public String toString() {
        return "AbstractLevelDTO{" + "id=" + id + ", title='" + title + '\'' + ", maxScore=" + maxScore + ", snapshotHook=" + snapshotHook
            + ", levelType=" + levelType + ", estimatedDuration=" + estimatedDuration + ", trainingDefinition=" + trainingDefinition
            + ", order=" + order + '}';
    }
}

