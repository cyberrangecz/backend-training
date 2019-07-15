package cz.muni.ics.kypo.training.api.dto.traininginstance;

import io.swagger.annotations.ApiModel;

/**
 * Encapsulates the information stating if training instance has finished.
 */
@ApiModel(value = "TrainingInstanceIsFinishedInfoDTO", description = "Information stating if training instance has finished.")
public class TrainingInstanceIsFinishedInfoDTO {

    private boolean hasFinished;
    private String message;

    /**
     * Get if instance has finished.
     *
     * @return the boolean
     */
    public boolean getHasFinished() {
        return hasFinished;
    }

    /**
     * Sets if instance has finished.
     *
     * @param hasFinished the has finished
     */
    public void setHasFinished(boolean hasFinished) {
        this.hasFinished = hasFinished;
    }

    /**
     * Gets message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets message.
     *
     * @param message the message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "TrainingInstanceIsFinishedInfoDTO{" +
                "hasFinished=" + hasFinished +
                ", message='" + message + '\'' +
                '}';
    }
}
