package cz.muni.ics.kypo.training.persistence.model;

import cz.muni.ics.kypo.training.persistence.model.enums.TRState;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Class represents Training run.
 * Training runs can be created based on instances.
 * Training runs are accessed by trainees
 *
 * @author Pavel Seda (441048)
 */
@Entity(name = "TrainingRun")
@Table(name = "training_run")
public class TrainingRun implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false, insertable = false)
    private Long id;
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
    @Column(name = "total_score")
    private int totalScore;
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
        return id;
    }

    /**
     * Sets unique identification number of Training run
     *
     * @param id the id
     */
    public void setId(Long id) {
        this.id = id;
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
     * Gets maximal score that can be gathered by trainee
     *
     * @return the total score
     */
    public int getTotalScore() {
        return totalScore;
    }

    /**
     * Sets maximal score that can be gathered by trainee
     *
     * @param totalScore the total score
     */
    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    /**
     * Takes away points from total score
     *
     * @param penalty the penalty
     */
    public void decreaseTotalScore(int penalty) {
        this.totalScore -= penalty;
    }

    public int getMaxLevelScore() {
        return maxLevelScore;
    }

    public void setMaxLevelScore(int maxLevelScore) {
        this.maxLevelScore = maxLevelScore;
    }

    public void increaseCurrentPenalty(int penalty) {
        this.currentPenalty += penalty;
    }

    public void increaseTotalScore(int points) {
        this.totalScore += points;
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

    public void setHintInfoList(Set<HintInfo> hintInfoList) {
        this.hintInfoList = hintInfoList;
    }

    public int getCurrentPenalty() {
        return currentPenalty;
    }

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
                "id=" + id +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", eventLogReference='" + eventLogReference + '\'' +
                ", state=" + state +
                ", incorrectFlagCount=" + incorrectFlagCount +
                ", solutionTaken=" + solutionTaken +
                ", currentLevel=" + currentLevel +
                ", trainingInstance=" + trainingInstance +
                ", sandboxInstanceRefId=" + sandboxInstanceRefId +
                ", participantRef=" + participantRef +
                ", assessmentResponses='" + assessmentResponses + '\'' +
                ", totalScore=" + totalScore +
                ", maxLevelScore=" + maxLevelScore +
                ", levelAnswered=" + levelAnswered +
                '}';
    }
}
