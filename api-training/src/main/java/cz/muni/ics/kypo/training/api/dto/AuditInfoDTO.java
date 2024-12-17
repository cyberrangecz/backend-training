package cz.muni.ics.kypo.training.api.dto;

/**
 * Encapsulates information used for auditing.
 */
public class AuditInfoDTO {

    private long userRefId;
    private long sandboxId;
    private long poolId;
    private long trainingRunId;
    private long trainingDefinitionId;
    private long trainingInstanceId;
    private long trainingTime;
    private long level;
    private int totalScore;
    private int actualScoreInLevel;

    /**
     * Instantiates a new Audit info dto.
     */
    public AuditInfoDTO() {
    }

    /**
     * Gets user ref id.
     *
     * @return the user ref id
     */
    public long getUserRefId() {
        return userRefId;
    }

    /**
     * Sets user ref id.
     *
     * @param userRefId the user ref id
     */
    public void setUserRefId(long userRefId) {
        this.userRefId = userRefId;
    }

    /**
     * Gets sandbox id.
     *
     * @return the sandbox id
     */
    public long getSandboxId() {
        return sandboxId;
    }

    /**
     * Sets sandbox id.
     *
     * @param sandboxId the sandbox id
     */
    public void setSandboxId(long sandboxId) {
        this.sandboxId = sandboxId;
    }

    /**
     * Gets pool id.
     *
     * @return the pool id
     */
    public long getPoolId() {
        return poolId;
    }

    /**
     * Sets pool id.
     *
     * @param poolId the pool id
     */
    public void setPoolId(long poolId) {
        this.poolId = poolId;
    }

    /**
     * Gets training run id.
     *
     * @return the training run id
     */
    public long getTrainingRunId() {
        return trainingRunId;
    }

    /**
     * Sets training run id.
     *
     * @param trainingRunId the training run id
     */
    public void setTrainingRunId(long trainingRunId) {
        this.trainingRunId = trainingRunId;
    }

    /**
     * Gets training definition id.
     *
     * @return the training definition id
     */
    public long getTrainingDefinitionId() {
        return trainingDefinitionId;
    }

    /**
     * Sets training definition id.
     *
     * @param trainingDefinitionId the training definition id
     */
    public void setTrainingDefinitionId(long trainingDefinitionId) {
        this.trainingDefinitionId = trainingDefinitionId;
    }

    /**
     * Gets training instance id.
     *
     * @return the training instance id
     */
    public long getTrainingInstanceId() {
        return trainingInstanceId;
    }

    /**
     * Sets training instance id.
     *
     * @param trainingInstanceId the training instance id
     */
    public void setTrainingInstanceId(long trainingInstanceId) {
        this.trainingInstanceId = trainingInstanceId;
    }

    /**
     * Gets training time.
     *
     * @return the training time
     */
    public long getTrainingTime() {
        return trainingTime;
    }

    /**
     * Sets training time.
     *
     * @param trainingTime the training time
     */
    public void setTrainingTime(long trainingTime) {
        this.trainingTime = trainingTime;
    }

    /**
     * Gets level.
     *
     * @return the level
     */
    public long getLevel() {
        return level;
    }

    /**
     * Sets level.
     *
     * @param level the level
     */
    public void setLevel(long level) {
        this.level = level;
    }

    /**
     * Gets total score.
     *
     * @return the total score
     */
    public int getTotalScore() {
        return totalScore;
    }

    /**
     * Sets total score.
     *
     * @param totalScore the total score
     */
    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    /**
     * Gets actual score in level.
     *
     * @return the actual score in level
     */
    public int getActualScoreInLevel() {
        return actualScoreInLevel;
    }

    /**
     * Sets actual score in level.
     *
     * @param actualScoreInLevel the actual score in level
     */
    public void setActualScoreInLevel(int actualScoreInLevel) {
        this.actualScoreInLevel = actualScoreInLevel;
    }

    @Override
    public String toString() {
        return "AuditInfoDTO{" +
                "userRefId=" + userRefId +
                ", sandboxId=" + sandboxId +
                ", poolId=" + poolId +
                ", trainingRunId=" + trainingRunId +
                ", trainingDefinitionId=" + trainingDefinitionId +
                ", trainingInstanceId=" + trainingInstanceId +
                ", trainingTime=" + trainingTime +
                ", level=" + level +
                ", totalScore=" + totalScore +
                ", actualScoreInLevel=" + actualScoreInLevel +
                '}';
    }
}
