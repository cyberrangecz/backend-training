package cz.muni.ics.kypo.training.api.dto;

import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * @author Pavel Seda
 */
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

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof IsCorrectFlagDTO)) return false;
        IsCorrectFlagDTO that = (IsCorrectFlagDTO) object;
        return isCorrect() == that.isCorrect() &&
                getRemainingAttempts() == that.getRemainingAttempts();
    }

    @Override
    public int hashCode() {
        return Objects.hash(isCorrect(), getRemainingAttempts());
    }


    @Override
    public String toString() {
        return "IsCorrectFlagDTO{" +
                "isCorrect=" + isCorrect +
                ", remainingAttempts=" + remainingAttempts +
                '}';
    }
}
