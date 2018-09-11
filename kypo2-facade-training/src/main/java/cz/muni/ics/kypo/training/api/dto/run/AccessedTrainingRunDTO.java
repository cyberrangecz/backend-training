package cz.muni.ics.kypo.training.api.dto.run;

import cz.muni.ics.kypo.training.api.enums.Actions;
import io.swagger.annotations.ApiModel;

import java.time.LocalDateTime;

@ApiModel(value = "AccessedTrainingRunDTO", description = ".")
public class AccessedTrainingRunDTO {
    private Long id;
    private String title;
    private LocalDateTime trainingInstanceStartDate;
    private LocalDateTime trainingInstanceEndDate;
    private int currentLevelOrder;
    private int numberOfLevels;
    private Actions possibleAction;

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

    public LocalDateTime getTrainingInstanceStartDate() {
        return trainingInstanceStartDate;
    }

    public void setTrainingInstanceStartDate(LocalDateTime trainingInstanceStartDate) {
        this.trainingInstanceStartDate = trainingInstanceStartDate;
    }

    public LocalDateTime getTrainingInstanceEndDate() {
        return trainingInstanceEndDate;
    }

    public void setTrainingInstanceEndDate(LocalDateTime trainingInstanceEndDate) {
        this.trainingInstanceEndDate = trainingInstanceEndDate;
    }

    public int getCurrentLevelOrder() {
        return currentLevelOrder;
    }

    public void setCurrentLevelOrder(int currentLevelOrder) {
        this.currentLevelOrder = currentLevelOrder;
    }

    public int getNumberOfLevels() {
        return numberOfLevels;
    }

    public void setNumberOfLevels(int numberOfLevels) {
        this.numberOfLevels = numberOfLevels;
    }

    public Actions getPossibleAction() {
        return possibleAction;
    }

    public void setPossibleAction(Actions possibleAction) {
        this.possibleAction = possibleAction;
    }
}
