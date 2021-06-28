package cz.muni.ics.kypo.training.api.dto.run;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.muni.ics.kypo.training.api.enums.Actions;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Encapsulates information about already accessed training run.
 *
 */
@ApiModel(value = "AccessedTrainingRunDTO", description = "Already accessed training run by some participant.")
public class AccessedTrainingRunDTO {

    @ApiModelProperty(value = "Main identifier of training run.", example = "1")
    private Long id;
    @ApiModelProperty(value = "Short textual description of the training instance.", example = "Concluded Instance")
    private String title;
    @ApiModelProperty(value = "Start date of training instance for which the training run was created.", example = "2016-10-19T10:23:54")
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime trainingInstanceStartDate;
    @ApiModelProperty(value = "End date of training instance for which the training run was created.", example = "2017-10-19T10:23:54")
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime trainingInstanceEndDate;
    @ApiModelProperty(value = "Current level order of training run.", example = "1")
    private int currentLevelOrder;
    @ApiModelProperty(value = "The number of levels in the training instance.", example = "3")
    private int numberOfLevels;
    @ApiModelProperty(value = "Possible action which can be executed with training Run.", example = "RESULTS")
    private Actions possibleAction;
    @ApiModelProperty(value = "Id of associated training instance", example = "1")
    private Long instanceId;

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
     * Gets start date of training instance.
     *
     * @return the training instance start date
     */
    public LocalDateTime getTrainingInstanceStartDate() {
        return trainingInstanceStartDate;
    }

    /**
     * Sets start date of training instance.
     *
     * @param trainingInstanceStartDate the training instance start date
     */
    public void setTrainingInstanceStartDate(LocalDateTime trainingInstanceStartDate) {
        this.trainingInstanceStartDate = trainingInstanceStartDate;
    }

    /**
     * Gets end date of training instance.
     *
     * @return the training instance end date
     */
    public LocalDateTime getTrainingInstanceEndDate() {
        return trainingInstanceEndDate;
    }

    /**
     * Sets end date of training instance.
     *
     * @param trainingInstanceEndDate the training instance end date
     */
    public void setTrainingInstanceEndDate(LocalDateTime trainingInstanceEndDate) {
        this.trainingInstanceEndDate = trainingInstanceEndDate;
    }

    /**
     * Gets current level order.
     *
     * @return the current level order
     */
    public int getCurrentLevelOrder() {
        return currentLevelOrder;
    }

    /**
     * Sets current level order.
     *
     * @param currentLevelOrder the current level order
     */
    public void setCurrentLevelOrder(int currentLevelOrder) {
        this.currentLevelOrder = currentLevelOrder;
    }

    /**
     * Gets number of levels.
     *
     * @return the number of levels
     */
    public int getNumberOfLevels() {
        return numberOfLevels;
    }

    /**
     * Sets number of levels.
     *
     * @param numberOfLevels the number of levels
     */
    public void setNumberOfLevels(int numberOfLevels) {
        this.numberOfLevels = numberOfLevels;
    }

    /**
     * Gets possible action.
     *
     * @return the possible {@link Actions}
     */
    public Actions getPossibleAction() {
        return possibleAction;
    }

    /**
     * Sets possible action.
     *
     * @param possibleAction the possible {@link Actions}
     */
    public void setPossibleAction(Actions possibleAction) {
        this.possibleAction = possibleAction;
    }

    /**
     * Gets instance id.
     *
     * @return the instance id
     */
    public Long getInstanceId() {
        return instanceId;
    }

    /**
     * Sets instance id.
     *
     * @param instanceId the instance id
     */
    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public String toString() {
        return "AccessedTrainingRunDTO{" + "id=" + id + ", title='" + title + '\'' + ", trainingInstanceStartDate="
            + trainingInstanceStartDate + ", trainingInstanceEndDate=" + trainingInstanceEndDate + ", currentLevelOrder="
            + currentLevelOrder + ", numberOfLevels=" + numberOfLevels + ", possibleAction=" + possibleAction + ", instanceId=" + instanceId
            + '}';
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof AccessedTrainingRunDTO)) return false;
        AccessedTrainingRunDTO that = (AccessedTrainingRunDTO) object;
        return Objects.equals(getCurrentLevelOrder(), that.getCurrentLevelOrder()) &&
                Objects.equals(getNumberOfLevels(), that.getNumberOfLevels()) &&
                Objects.equals(getId(), that.getId()) &&
                Objects.equals(getTitle(), that.getTitle()) &&
                Objects.equals(getInstanceId(), that.getInstanceId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTitle(), getCurrentLevelOrder(), getNumberOfLevels(), getInstanceId());
    }
}
