package cz.muni.ics.kypo.training.api.dto;

import io.swagger.annotations.ApiModelProperty;

public class IsCorrectFlagDTO {

    private boolean isCorrect;
    private int remainingAttempts;

    @ApiModelProperty(value = "True/false if flag has been correct/incorrect.", example = "false")
    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    @ApiModelProperty(value = "Number of attempts to submit a bad flag.", example = "3")
    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    public void setRemainingAttempts(int remainingAttempts) {
        this.remainingAttempts = remainingAttempts;
    }

}
