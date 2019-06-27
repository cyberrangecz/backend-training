package cz.muni.ics.kypo.training.api.dto;

import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * Response to attempt of flag input
 *
 * @author Pavel Seda
 */
public class IsCorrectFlagDTO {

    private boolean isCorrect;
    private int remainingAttempts;

    /**
     * Is correct boolean.
     *
     * @return True if flag is correct
     */
    @ApiModelProperty(value = "True/false if flag has been correct/incorrect.", example = "false")
    public boolean isCorrect() {
        return isCorrect;
    }

    /**
     * Sets correct.
     *
     * @param correct True if flag is correct
     */
    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    /**
     * Gets remaining attempts.
     *
     * @return the remaining attempts
     */
    @ApiModelProperty(value = "Number of attempts to submit a bad flag.", example = "3")
    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    /**
     * Sets remaining attempts.
     *
     * @param remainingAttempts the remaining attempts
     */
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
