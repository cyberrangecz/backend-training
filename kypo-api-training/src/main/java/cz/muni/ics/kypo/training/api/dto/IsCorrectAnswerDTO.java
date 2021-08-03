package cz.muni.ics.kypo.training.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * Response to attempt of answer input
 *
 */
@ApiModel(value = "IsCorrectAnswerDTO", description = "A response for the request about the validation of the answer. May also " +
        "include solution if remaining attempts reach 0.")
public class IsCorrectAnswerDTO {

    @ApiModelProperty(value = "True/false if answer has been correct/incorrect.", example = "false")
    private boolean isCorrect;
    @ApiModelProperty(value = "Number of attempts to submit a bad answer.", example = "3")
    private int remainingAttempts;
    @ApiModelProperty(value = "Instruction how to get answer in training.", example = "This is how you do it")
    private String solution;

    /**
     * Is correct boolean.
     *
     * @return True if answer is correct
     */
    public boolean isCorrect() {
        return isCorrect;
    }

    /**
     * Sets correct.
     *
     * @param correct True if answer is correct
     */
    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    /**
     * Gets remaining attempts.
     *
     * @return the remaining attempts
     */
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

    /**
     * Gets solution.
     *
     * @return the solution
     */
    public String getSolution() {
        return solution;
    }

    /**
     * Sets solution.
     *
     * @param solution the solution
     */
    public void setSolution(String solution) {
        this.solution = solution;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof IsCorrectAnswerDTO)) return false;
        IsCorrectAnswerDTO that = (IsCorrectAnswerDTO) object;
        return isCorrect() == that.isCorrect() &&
                getRemainingAttempts() == that.getRemainingAttempts() &&
                Objects.equals(getSolution(), that.getSolution());
    }

    @Override
    public int hashCode() {
        return Objects.hash(isCorrect(), getRemainingAttempts(), getSolution());
    }

    @Override
    public String toString() {
        return "IsCorrectAnswerDTO{" +
                "isCorrect=" + isCorrect +
                ", remainingAttempts=" + remainingAttempts +
                '}';
    }
}
