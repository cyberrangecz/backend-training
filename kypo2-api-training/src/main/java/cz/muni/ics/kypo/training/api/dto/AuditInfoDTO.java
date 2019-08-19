package cz.muni.ics.kypo.training.api.dto;

public class AuditInfoDTO {

    private long userRefId;
    private String fullNameWithoutTitles;
    private String fullName;
    private String playerLogin;
    private long sandboxId;
    private long trainingRunId;
    private long trainingDefinitionId;
    private long trainingInstanceId;
    private long gameTime;
    private long level;
    private int totalScore;
    private int actualScoreInLevel;
    private String iss;

    public AuditInfoDTO() {
    }

    public long getUserRefId() {
        return userRefId;
    }

    public void setUserRefId(long userRefId) {
        this.userRefId = userRefId;
    }

    public String getFullNameWithoutTitles() {
        return fullNameWithoutTitles;
    }

    public void setFullNameWithoutTitles(String fullNameWithoutTitles) {
        this.fullNameWithoutTitles = fullNameWithoutTitles;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPlayerLogin() {
        return playerLogin;
    }

    public void setPlayerLogin(String playerLogin) {
        this.playerLogin = playerLogin;
    }

    public long getSandboxId() {
        return sandboxId;
    }

    public void setSandboxId(long sandboxId) {
        this.sandboxId = sandboxId;
    }

    public long getTrainingRunId() {
        return trainingRunId;
    }

    public void setTrainingRunId(long trainingRunId) {
        this.trainingRunId = trainingRunId;
    }

    public long getTrainingDefinitionId() {
        return trainingDefinitionId;
    }

    public void setTrainingDefinitionId(long trainingDefinitionId) {
        this.trainingDefinitionId = trainingDefinitionId;
    }

    public long getTrainingInstanceId() {
        return trainingInstanceId;
    }

    public void setTrainingInstanceId(long trainingInstanceId) {
        this.trainingInstanceId = trainingInstanceId;
    }

    public long getGameTime() {
        return gameTime;
    }

    public void setGameTime(long gameTime) {
        this.gameTime = gameTime;
    }

    public long getLevel() {
        return level;
    }

    public void setLevel(long level) {
        this.level = level;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public int getActualScoreInLevel() {
        return actualScoreInLevel;
    }

    public void setActualScoreInLevel(int actualScoreInLevel) {
        this.actualScoreInLevel = actualScoreInLevel;
    }

    public String getIss() {
        return iss;
    }

    public void setIss(String iss) {
        this.iss = iss;
    }

    @Override
    public String toString() {
        return "AuditInfoDTO{" +
                "userRefId=" + userRefId +
                ", fullNameWithoutTitles='" + fullNameWithoutTitles + '\'' +
                ", fullName='" + fullName + '\'' +
                ", playerLogin='" + playerLogin + '\'' +
                ", sandboxId=" + sandboxId +
                ", trainingRunId=" + trainingRunId +
                ", trainingDefinitionId=" + trainingDefinitionId +
                ", trainingInstanceId=" + trainingInstanceId +
                ", gameTime=" + gameTime +
                ", level=" + level +
                ", totalScore=" + totalScore +
                ", actualScoreInLevel=" + actualScoreInLevel +
                ", iss='" + iss + '\'' +
                '}';
    }
}
