package cz.muni.ics.kypo.training.api.dto.run;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import cz.muni.ics.kypo.training.api.enums.Actions;
import cz.muni.ics.kypo.training.converters.LocalDateTimeUTCSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

/**
 * @author Dominik Pilar & Pavel Seda
 */
@ApiModel(value = "AccessedTrainingRunDTO", description = "Already accessed training run by some participant.")
public class AccessedTrainingRunDTO {

    private Long id;
    private String title;
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime trainingInstanceStartDate;
    @JsonSerialize(using = LocalDateTimeUTCSerializer.class)
    private LocalDateTime trainingInstanceEndDate;
    private int currentLevelOrder;
    private int numberOfLevels;
    private Actions possibleAction;
    private Long instanceId;

    @ApiModelProperty(value = "Main identifier of training run.", example = "1")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ApiModelProperty(value = "Short textual description of the training instance.", example = "Concluded Instance")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @ApiModelProperty(value = "Start date of training instance for which the training run was created.", example = "2016-10-19T10:23:54")
    public LocalDateTime getTrainingInstanceStartDate() {
        return trainingInstanceStartDate;
    }

    public void setTrainingInstanceStartDate(LocalDateTime trainingInstanceStartDate) {
        this.trainingInstanceStartDate = trainingInstanceStartDate;
    }

    @ApiModelProperty(value = "End date of training instance for which the training run was created.", example = "2017-10-19T10:23:54")
    public LocalDateTime getTrainingInstanceEndDate() {
        return trainingInstanceEndDate;
    }

    public void setTrainingInstanceEndDate(LocalDateTime trainingInstanceEndDate) {
        this.trainingInstanceEndDate = trainingInstanceEndDate;
    }

    @ApiModelProperty(value = "Current level order of training run.", example = "1")
    public int getCurrentLevelOrder() {
        return currentLevelOrder;
    }

    public void setCurrentLevelOrder(int currentLevelOrder) {
        this.currentLevelOrder = currentLevelOrder;
    }

    @ApiModelProperty(value = "The number of levels in the training instance.", example = "3")
    public int getNumberOfLevels() {
        return numberOfLevels;
    }

    public void setNumberOfLevels(int numberOfLevels) {
        this.numberOfLevels = numberOfLevels;
    }

    @ApiModelProperty(value = "Possible action which can be executed with training Run.", example = "RESULTS")
    public Actions getPossibleAction() {
        return possibleAction;
    }

    public void setPossibleAction(Actions possibleAction) {
        this.possibleAction = possibleAction;
    }

    @ApiModelProperty(value = "Id of associated training instance", example = "1")
    public Long getInstanceId() {
        return instanceId;
    }

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
}
