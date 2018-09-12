package cz.muni.ics.kypo.training.api.dto;

public class IsCorrectFlagDTO {
    private boolean isCorrect;
    private int remainingAttempts;

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    public void setRemainingAttempts(int remainingAttempts) {
        this.remainingAttempts = remainingAttempts;
    }

}
