package cz.muni.ics.kypo.training.persistence.model;

import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Class represents Training run.
 * Training runs can be created based on instances.
 * Training runs are accessed by trainees
 */
@Entity
@Table(name = "training_run")
@NamedEntityGraphs({
        @NamedEntityGraph(
                name = "TrainingRun.findAllParticipantRef",
                attributeNodes = @NamedAttributeNode(value = "participantRef")
        ),
        @NamedEntityGraph(
                name = "TrainingRun.findByIdParticipantRefTrainingInstance",
                attributeNodes = {
                        @NamedAttributeNode(value = "participantRef"),
                        @NamedAttributeNode(value = "trainingInstance")
                }
        )
})
@NamedQueries({
        @NamedQuery(
                name = "TrainingRun.findRunningTrainingRunOfUser",
                query = "SELECT tr FROM TrainingRun tr " +
                        "JOIN FETCH tr.trainingInstance ti " +
                        "JOIN FETCH tr.participantRef pr " +
                        "JOIN FETCH tr.currentLevel cl " +
                        "WHERE ti.accessToken = :accessToken AND pr.userRefId = :userRefId AND tr.sandboxInstanceRefId IS NOT NULL AND tr.state NOT LIKE 'FINISHED'"
        ),
        @NamedQuery(
                name = "TrainingRun.findByIdWithLevel",
                query = "SELECT tr FROM TrainingRun tr " +
                        "JOIN FETCH tr.currentLevel " +
                        "JOIN FETCH tr.trainingInstance ti " +
                        "JOIN FETCH ti.trainingDefinition " +
                        "WHERE tr.id= :trainingRunId",
                lockMode = LockModeType.PESSIMISTIC_WRITE
        ),
        @NamedQuery(
                name = "TrainingRun.deleteTrainingRunsByTrainingInstance",
                query = "DELETE FROM TrainingRun tr WHERE tr.trainingInstance.id = :trainingInstanceId"
        ),
        @NamedQuery(
                name = "TrainingRun.existsAnyForTrainingInstance",
                query = "SELECT (COUNT(tr) > 0) FROM TrainingRun tr INNER JOIN tr.trainingInstance ti WHERE ti.id = :trainingInstanceId"
        ),
        @NamedQuery(
                name = "TrainingRun.findAllByParticipantRefId",
                query = "SELECT tr FROM TrainingRun tr " +
                        "INNER JOIN tr.participantRef pr " +
                        "INNER JOIN tr.trainingInstance ti " +
                        "INNER JOIN ti.trainingDefinition " +
                        "WHERE pr.userRefId = :userRefId"
        ),
        @NamedQuery(
                name = "TrainingRun.findAllByTrainingDefinitionIdAndParticipantUserRefId",
                query = "SELECT tr FROM TrainingRun tr " +
                        "INNER JOIN tr.participantRef pr " +
                        "INNER JOIN tr.trainingInstance ti " +
                        "INNER JOIN ti.trainingDefinition td " +
                        "WHERE td.id = :trainingDefinitionId AND pr.userRefId = :userRefId"
        ),
        @NamedQuery(
                name = "TrainingRun.findAllActiveByTrainingInstanceId",
                query = "SELECT tr FROM TrainingRun tr " +
                        "INNER JOIN tr.trainingInstance ti " +
                        "WHERE ti.id = :trainingInstanceId AND tr.state <> 'ARCHIVED'"
        ),
        @NamedQuery(
                name = "TrainingRun.findAllInactiveByTrainingInstanceId",
                query = "SELECT tr FROM TrainingRun tr " +
                        "INNER JOIN tr.trainingInstance ti " +
                        "WHERE ti.id = :trainingInstanceId AND tr.state = 'ARCHIVED'"
        ),
        @NamedQuery(
                name = "TrainingRun.findAllByTrainingDefinitionId",
                query = "SELECT tr FROM TrainingRun tr " +
                        "INNER JOIN tr.trainingInstance ti " +
                        "INNER JOIN ti.trainingDefinition td " +
                        "WHERE td.id = :trainingDefinitionId"
        )
})
public class TrainingRun extends AbstractEntity<Long> {

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
    @Column(name = "event_log_reference", nullable = true)
    private String eventLogReference;
    @Column(name = "state", length = 128, nullable = false)
    @Enumerated(EnumType.STRING)
    private TRState state;
    @Column(name = "incorrect_flag_count", nullable = false)
    private int incorrectFlagCount;
    @Column(name = "solution_taken", nullable = false)
    private boolean solutionTaken;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private AbstractLevel currentLevel;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private TrainingInstance trainingInstance;
    @Column(name = "sandbox_instance_ref_id")
    private Long sandboxInstanceRefId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_ref_id", nullable = false)
    private UserRef participantRef;
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "assessment_responses", nullable = true)
    private String assessmentResponses;
    @Column(name = "total_game_score")
    private int totalGameScore;
    @Column(name = "total_assessment_score")
    private int totalAssessmentScore;
    @Column(name = "max_level_score")
    private int maxLevelScore;
    @Column(name = "level_answered")
    private boolean levelAnswered;
    @ElementCollection(targetClass = HintInfo.class, fetch = FetchType.LAZY)
    @CollectionTable(name = "hint_info", joinColumns = @JoinColumn(name = "training_run_id"))
    private Set<HintInfo> hintInfoList = new HashSet<>();
    @Column(name = "previous_sandbox_instance_ref_id")
    private Long previousSandboxInstanceRefId;
    @Column(name = "current_penalty")
    private int currentPenalty;

    /**
     * Gets unique identification number of Training run
     *
     * @return the id
     */
    public Long getId() {
        return super.getId();
    }

    /**
     * Sets unique identification number of Training run
     *
     * @param id the id
     */
    public void setId(Long id) {
        super.setId(id);
    }

    /**
     * Gets initiation time of Training run
     *
     * @return the start time
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * Sets initiation time of Training run
     *
     * @param startTime the start time
     */
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    /**
     * Gets finish time of Training run
     *
     * @return the end time
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }

    /**
     * Sets finish time of Training run
     *
     * @param endTime the end time
     */
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    /**
     * Gets event log reference.
     *
     * @return the event log reference
     */
    public String getEventLogReference() {
        return eventLogReference;
    }

    /**
     * Sets event log reference.
     *
     * @param eventLogReference the event log reference
     */
    public void setEventLogReference(String eventLogReference) {
        this.eventLogReference = eventLogReference;
    }

    /**
     * Gets completion state of Training run
     * States are RUNNING, FINISHED, ARCHIVED
     *
     * @return the state
     */
    public TRState getState() {
        return state;
    }

    /**
     * Sets completion state of Training run
     * States are RUNNING, FINISHED, ARCHIVED
     *
     * @param state the state
     */
    public void setState(TRState state) {
        this.state = state;
    }

    /**
     * Gets level that is currently being displayed to the trainee
     *
     * @return the current level
     */
    public AbstractLevel getCurrentLevel() {
        return currentLevel;
    }

    /**
     * Sets level that is currently being displayed to the trainee
     * Sets default data about level to training run
     *
     * @param currentLevel the current level
     */
    public void setCurrentLevel(AbstractLevel currentLevel) {
        this.currentPenalty = 0;
        this.maxLevelScore = currentLevel.getMaxScore();
        this.levelAnswered = currentLevel instanceof InfoLevel;
        this.solutionTaken = false;
        this.currentLevel = currentLevel;
    }

    /**
     * Gets Training instance associated to Training run
     *
     * @return the training instance
     */
    public TrainingInstance getTrainingInstance() {
        return trainingInstance;
    }

    /**
     * Sets Training instance associated to Training run
     *
     * @param trainingInstance the training instance
     */
    public void setTrainingInstance(TrainingInstance trainingInstance) {
        this.trainingInstance = trainingInstance;
    }

    /**
     * Gets id of sandbox instance associated with Training run
     *
     * @return the sandbox instance ref id
     */
    public Long getSandboxInstanceRefId() {
        return sandboxInstanceRefId;
    }

    /**
     * Sets id of sandbox instance associated with Training run
     *
     * @param sandboxInstanceRefId the sandbox instance ref id
     */
    public void setSandboxInstanceRefId(Long sandboxInstanceRefId) {
        this.sandboxInstanceRefId = sandboxInstanceRefId;
    }

    /**
     * Gets number of failed attempts by trainee to submit correct flag on current level
     *
     * @return the incorrect flag count
     */
    public int getIncorrectFlagCount() {
        return incorrectFlagCount;
    }

    /**
     * Sets number of failed attempts trainee can submit on current level
     *
     * @param incorrectFlagCount the incorrect flag count
     */
    public void setIncorrectFlagCount(int incorrectFlagCount) {
        this.incorrectFlagCount = incorrectFlagCount;
    }

    /**
     * Gets solution was taken on current level
     *
     * @return the boolean
     */
    public boolean isSolutionTaken() {
        return solutionTaken;
    }

    /**
     * Sets solution was taken on current level
     *
     * @param solutionTaken the solution taken
     */
    public void setSolutionTaken(boolean solutionTaken) {
        this.solutionTaken = solutionTaken;
    }

    /**
     * Gets responses of current assessment level
     *
     * @return the assessment responses
     */
    public String getAssessmentResponses() {
        return assessmentResponses;
    }

    /**
     * Sets responses of current assessment level
     *
     * @param assessmentResponses the assessment responses
     */
    public void setAssessmentResponses(String assessmentResponses) {
        this.assessmentResponses = assessmentResponses;
    }

    /**
     * Gets DB reference of trainee
     *
     * @return the participant ref
     */
    public UserRef getParticipantRef() {
        return participantRef;
    }

    /**
     * Sets DB reference of trainee
     *
     * @param participantRef the participant ref
     */
    public void setParticipantRef(UserRef participantRef) {
        this.participantRef = participantRef;
    }

    /**
     * Gets maximal game level score that can be gathered by trainee
     *
     * @return the total score
     */
    public int getTotalGameScore() {
        return totalGameScore;
    }

    /**
     * Sets maximal game level score that can be gathered by trainee
     *
     * @param totalGameScore the total score
     */
    public void setTotalGameScore(int totalGameScore) {
        this.totalGameScore = totalGameScore;
    }

    public void decreaseTotalGameScore(int penalty) {
        this.totalGameScore -= penalty;
    }

    /**
     * Gets maximal assessment level score that can be gathered by trainee
     *
     * @return the total score
     */
    public int getTotalAssessmentScore() {
        return totalAssessmentScore;
    }

    /**
     * Sets maximal assessment level score that can be gathered by trainee
     *
     * @param totalAssessmentScore the total score
     */
    public void setTotalAssessmentScore(int totalAssessmentScore) {
        this.totalAssessmentScore = totalAssessmentScore;
    }

    /**
     * Takes away points from total assessment level score
     *
     * @param penalty the penalty
     */
    public void decreaseTotalAssessmentScore(int penalty) {
        this.totalAssessmentScore -= penalty;
    }

    /**
     * Increase total game level score.
     *
     * @param points the points
     */
    public void increaseTotalGameScore(int points) {
        this.totalGameScore += points;
    }

    /**
     * Increase total assessment score.
     *
     * @param points the points
     */
    public void increaseTotalAssessmentScore(int points) {
        this.totalAssessmentScore += points;
    }


    /**
     * Gets max level score.
     *
     * @return the max level score
     */
    public int getMaxLevelScore() {
        return maxLevelScore;
    }

    /**
     * Sets max level score.
     *
     * @param maxLevelScore the max level score
     */
    public void setMaxLevelScore(int maxLevelScore) {
        this.maxLevelScore = maxLevelScore;
    }

    /**
     * Increase current penalty.
     *
     * @param penalty the penalty
     */
    public void increaseCurrentPenalty(int penalty) {
        this.currentPenalty += penalty;
    }

    /**
     * Gets if level was answered
     *
     * @return the boolean
     */
    public boolean isLevelAnswered() {
        return levelAnswered;
    }

    /**
     * Sets if level was answered
     *
     * @param levelAnswered the level answered
     */
    public void setLevelAnswered(boolean levelAnswered) {
        this.levelAnswered = levelAnswered;
    }

    /**
     * Gets hints associated to current game level
     *
     * @return the hint info list
     */
    public Set<HintInfo> getHintInfoList() {
        return hintInfoList;
    }

    /**
     * Adds hint to current game level
     *
     * @param hintInfo the hint info
     */
    public void addHintInfo(HintInfo hintInfo) {
        this.hintInfoList.add(hintInfo);
    }

    /**
     * Removes hint to current game level
     *
     * @param hintInfo the hint info
     */
    public void removeHintInfo(HintInfo hintInfo) {
        this.hintInfoList.remove(hintInfo);
    }


    /**
     * Gets id of previous sandbox instance ref assigned by training run.
     *
     * @return the id of previous sandbox instance ref
     */
    public Long getPreviousSandboxInstanceRefId() {
        return previousSandboxInstanceRefId;
    }

    /**
     * Sets previous sandbox instance ref ID
     *
     * @param previousSandboxInstanceRefId the id of previous sandbox instance ref
     */
    public void setPreviousSandboxInstanceRefId(Long previousSandboxInstanceRefId) {
        this.previousSandboxInstanceRefId = previousSandboxInstanceRefId;
    }

    /**
     * Sets hint info list.
     *
     * @param hintInfoList the hint info list
     */
    public void setHintInfoList(Set<HintInfo> hintInfoList) {
        this.hintInfoList = hintInfoList;
    }

    /**
     * Gets current penalty.
     *
     * @return the current penalty
     */
    public int getCurrentPenalty() {
        return currentPenalty;
    }

    /**
     * Sets current penalty.
     *
     * @param currentPenalty the current penalty
     */
    public void setCurrentPenalty(int currentPenalty) {
        this.currentPenalty = currentPenalty;
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentLevel, eventLogReference, startTime, endTime, state, trainingInstance, incorrectFlagCount);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof TrainingRun))
            return false;
        TrainingRun other = (TrainingRun) obj;
        return Objects.equals(currentLevel, other.getCurrentLevel())
                && Objects.equals(eventLogReference, other.getEventLogReference())
                && Objects.equals(startTime, other.getStartTime())
                && Objects.equals(endTime, other.getEndTime())
                && Objects.equals(state, other.getState())
                && Objects.equals(incorrectFlagCount, other.getIncorrectFlagCount())
                && Objects.equals(trainingInstance, other.getTrainingInstance())
                && Objects.equals(participantRef, other.getParticipantRef())
                && Objects.equals(solutionTaken, other.isSolutionTaken());
    }

    @Override
    public String toString() {
        return "TrainingRun{" +
                "id=" + super.getId() +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", eventLogReference='" + eventLogReference + '\'' +
                ", state=" + state +
                ", incorrectFlagCount=" + incorrectFlagCount +
                ", solutionTaken=" + solutionTaken +
                ", currentLevel=" + currentLevel +
                ", sandboxInstanceRefId=" + sandboxInstanceRefId +
                ", totalGameScore=" + totalGameScore +
                ", totalAssessmentScore=" + totalAssessmentScore +
                ", maxLevelScore=" + maxLevelScore +
                ", levelAnswered=" + levelAnswered +
                '}';
    }
}
